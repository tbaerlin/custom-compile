/*
 * AbstractIstarRequest.java
 *
 * Created on 02.03.2005 14:21:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.nioframework;


import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.Lifecycle;

/**
 * Event queue for I/O events raised by a selector. This class receives the
 * lower level events raised by a Selector and dispatches them to the
 * appropriate handler. It also manages all other operations on the selector,
 * like registering and unregistering channels, or updating the events of
 * interest for each monitored socket.
 * <br><br>
 * This class is inspired on the java.awt.EventQueue and follows a similar
 * model. The EventQueue class is responsible for making sure that all
 * operations on AWT objects are performed on a single thread, the one managed
 * internally by EventQueue. The SelectorThread class performs a similar
 * task. In particular:
 * <ul>
 *     <li>
 *         Only the thread created by instances of this class should be allowed
 *         to access the selector and all sockets managed by it. This means that
 *         all I/O operations on the sockets should be peformed on the corresponding
 *         selector's thread. If some other thread wants to access objects managed
 *         by this selector, then it should use <code>invokeLater()</code> or the
 *         <code>invokeAndWait()</code> to dispatch a runnable to this thread.
 *     </li>
 *     <li>
 *         This thread should not be used to perform lenghty operations. In
 *         particular, it should never be used to perform blocking I/O operations.
 *         To perform a time consuming task use a worker thread.
 *     </li>
 * </ul>
 *
 * This architecture makes synchronization in the objects of a connection
 * unnecessary. This is good for performance and essential for keeping
 * the complexity low. Getting synchronization right within the objects
 * of a connection would be extremely tricky. This class does not contain
 * any synchronized blocks or methods, instead it relies on the features
 * of the <code>java.util.concurrent</code> package.
 *
 * @author Oliver Flege
 */
public final class SelectorThread implements Lifecycle, SelectorThreadMBean, BeanNameAware {

    public interface NopListener {
        /**
         * ack the fact that no operation has been processed in the last second, will
         * be called by the selector thread.
         */
        void ackNop();
    }

    private static final int SECOND_IN_MILLIS = 1000;

    private final static AtomicInteger INSTANCE_COUNT = new AtomicInteger();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Selector used for I/O multiplexing
     */
    private Selector selector;

    private volatile boolean done;

    /**
     * The thread associated with this selector
     */
    private final Thread selectorThread = new Thread(() -> {
        try {
            doRun();
        } catch (Throwable t) {
            logger.error("<doRun> failed", t);
        } finally {
            done = true;
        }
        logger.info("<run> finished");
        closeSelectorAndChannels();
    }, "SelectorThread-" + INSTANCE_COUNT.incrementAndGet());

    private int timeout = SECOND_IN_MILLIS;

    /**
     * Flag telling if this object should terminate, that is,
     * if it should close the selector and kill the associated
     * thread. Used for graceful termination.
     */
    private AtomicBoolean closeRequested = new AtomicBoolean(false);

    /**
     * List of tasks to be executed in the selector thread.
     * Submitted using invokeLater() and executed in the main
     * select loop.
     */
    private final Queue<Runnable> pendingInvocations;

    /**
     * Listeners that will be called whenever the SelectorThread has been idle for about one second.
     * Can for example be used to detect that a server has stopped sending.
     */
    private final CopyOnWriteArrayList<NopListener> nopListeners = new CopyOnWriteArrayList<>();

    /**
     * Creates a new selector; the associated thread will be created and started in
     * {@link #start()}.
     * @throws IOException on error
     */
    public SelectorThread() throws IOException {
        this(1024);
    }

    public SelectorThread(int pendingInvocationQueueSize) throws IOException {
        this.pendingInvocations = new ArrayBlockingQueue<>(pendingInvocationQueueSize);
        this.selector = Selector.open();
    }

    @Override
    public void setBeanName(String s) {
        this.selectorThread.setName(s);
    }

    public boolean addListener(NopListener nopListener) {
        return this.nopListeners.addIfAbsent(nopListener);
    }

    public boolean removeListener(NopListener nopListener) {
        return this.nopListeners.remove(nopListener);
    }

    public void setDaemon(boolean daemon) {
        this.selectorThread.setDaemon(daemon);
    }

    @Override
    public void start() {
        this.selectorThread.start();
        this.logger.info("<start> started " + selectorThread.getName()
                + ", daemon=" + this.selectorThread.isDaemon());
    }

    @Override
    /**
     * Raises an internal flag that will result on this thread dying
     * the next time it goes through the dispatch loop. The thread
     * executes all pending tasks before dying.
     */
    public void stop() {
        this.logger.info("<stop> ...");
        if (this.closeRequested.compareAndSet(false, true)) {
            this.selector.wakeup();
        }
        try {
            this.selectorThread.join(30 * 1000);
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        if (this.selectorThread.isAlive()) {
            this.logger.error("<stop> failed to join selectorThread, returning");
        }
        else {
            this.logger.info("<stop> done");
        }
    }

    @Override
    public boolean isRunning() {
        return this.selectorThread.isAlive();
    }

    /**
     * Modifies the default select timeout of 1s for the following channel select. In contrast
     * to {@link java.nio.channels.Selector#select(long)}, a value of 0 will lead to the invocation
     * of {@link java.nio.channels.Selector#selectNow()} (i.e., no timeout/waiting at all).
     * Any value larger than 1s will be ignored.
     * <p>
     * This method is helpful to do some work on the selector thread as soon as possible if no
     * i/o stuff is to be done immediately. That work would then have to happen in
     * {@link de.marketmaker.istar.common.nioframework.SelectorThread.NopListener#ackNop()} and
     * the respective listener needs to have been registered with this selector thread before.
     * <p>
     * This method should only be called on the selector thread. Otherwise
     * an exception is thrown.
     * @param ms select timeout in milliseconds.
     * @throws IOException
     */
    public void setNextSelectTimeout(int ms) throws IOException {
        if (ms < 0) {
            throw new IllegalArgumentException(Integer.toString(ms) + " < 0");
        }
        if (isClosed()) {
            return;
        }
        checkSelectorThread();
        this.timeout = Math.min(ms, SECOND_IN_MILLIS);
    }

    /**
     * Removes the association between this selector and the given channel, does nothing
     * if no such association exists.
     * @param channel to be removed
     * @throws IOException  on error
     */
    public void removeChannelNow(SelectableChannel channel) throws IOException {
        if (isClosed()) {
            return;
        }
        checkSelectorThread();
        final SelectionKey sk = channel.keyFor(this.selector);
        if (sk != null) {
            sk.cancel();
        }
    }

    /**
     * Removes the association between this selector and the given channel, waits until
     * this action has happened; does nothing if no such association exists.
     * @param channel to be removed
     * @param close whether the channel should be closed after disassociating
     * @param errorHandler to be informed about errors
     * @throws InterruptedException on error
     */
    public void removeChannelAndWait(final SelectableChannel channel, final boolean close,
            final CallbackErrorHandler errorHandler) throws InterruptedException {
        invokeAndWait(new Runnable() {
            public void run() {
                try {
                    removeChannelNow(channel);
                    if (close) {
                        channel.close();
                    }
                } catch (IOException e) {
                    errorHandler.handleError("failed to remove channel", e);
                }
            }

            @Override
            public String toString() {
                return "close " + channel;
            }
        });
    }

    /**
     * Adds a new interest to the list of events where a channel is
     * registered. This means that the associated event handler will
     * start receiving events for the specified interest.
     *
     * This method should only be called on the selector thread. Otherwise
     * an exception is thrown. Use the addChannelInterestLater() when calling
     * from another thread.
     * @param channel The channel to be updated. Must be registered.
     * @param interest The interest to add. Should be one of the
     * constants defined on SelectionKey.
     * @throws java.io.IOException on error
     */
    public void addChannelInterestNow(SelectableChannel channel,
            int interest) throws IOException {
        if (isClosed()) {
            return;
        }
        checkSelectorThread();
        final SelectionKey sk = getSelectionKey(channel);
        if (sk.isValid()) {
            changeKeyInterest(sk, sk.interestOps() | interest);
        }
    }

    /**
     * Like addChannelInterestNow(), but executed asynchronouly on the
     * selector thread. It returns after scheduling the task, without
     * waiting for it to be executed.
     * @param channel The channel to be updated. Must be registered.
     * @param interest The new interest to add. Should be one of the
     * constants defined on SelectionKey.
     * @param errorHandler Callback used if an exception is raised when executing the task.
     */
    public void addChannelInterestLater(final SelectableChannel channel,
            final int interest,
            final CallbackErrorHandler errorHandler) {
        // Add a new runnable to the list of tasks to be executed in the selector thread
        invokeLater(() -> {
            try {
                addChannelInterestNow(channel, interest);
            } catch (IOException e) {
                errorHandler.handleError("failed to add channel interest "
                        + keyToString(interest), e);
            }
        });
    }

    /**
     * Removes an interest from the list of events where a channel is
     * registered. The associated event handler will stop receiving events
     * for the specified interest.
     *
     * This method should only be called on the selector thread. Otherwise
     * an exception is thrown. Use the removeChannelInterestLater() when calling
     * from another thread.
     * @param channel The channel to be updated. Must be registered.
     * @param interest The interest to be removed. Should be one of the
     * constants defined on SelectionKey.
     * @throws java.io.IOException on error
     */
    public void removeChannelInterestNow(SelectableChannel channel,
            int interest) throws IOException {
        if (isClosed()) {
            return;
        }
        checkSelectorThread();
        SelectionKey sk = getSelectionKey(channel);
        changeKeyInterest(sk, sk.interestOps() & ~interest);
    }

    /**
     * Like removeChannelInterestNow(), but executed asynchronouly on
     * the selector thread. This method returns after scheduling the task,
     * without waiting for it to be executed.
     * @param channel The channel to be updated. Must be registered.
     * @param interest The interest to remove. Should be one of the
     * constants defined on SelectionKey.
     * @param errorHandler Callback used if an exception is raised when
     * executing the task.
     */
    public void removeChannelInterestLater(final SelectableChannel channel,
            final int interest,
            final CallbackErrorHandler errorHandler) {
        invokeLater(() -> {
            try {
                removeChannelInterestNow(channel, interest);
            } catch (IOException e) {
                errorHandler.handleError("failed to remove channel", e);
            }
        });
    }

    /**
     * Updates the interest set associated with a selection key. The
     * old interest is discarded, being replaced by the new one.
     * @param sk The key to be updated.
     * @param newInterest ops
     * @throws IOException on error
     */
    private void changeKeyInterest(SelectionKey sk, int newInterest) throws IOException {
        /* This method might throw two unchecked exceptions:
         * 1. IllegalArgumentException  - Should never happen. It is a bug if it happens
         * 2. CancelledKeyException - Might happen if the channel is closed while
         * a packet is being dispatched.
         */
        try {
            sk.interestOps(newInterest);
        } catch (CancelledKeyException cke) {
            IOException ioe = new IOException("Failed to change channel interest.");
            ioe.initCause(cke);
            throw ioe;
        }
    }

    /**
     * Like registerChannelLater(), but executed asynchronouly on the
     * selector thread. It returns after scheduling the task, without
     * waiting for it to be executed.
     * @param channel The channel to be monitored.
     * @param selectionKeys The interest set. Should be a combination of
     * SelectionKey constants.
     * @param handler The handler for events raised on the registered channel.
     * @param errorHandler Used for asynchronous error handling.
     */
    public void registerChannelLater(final SelectableChannel channel,
            final int selectionKeys,
            final SelectorHandler handler,
            final CallbackErrorHandler errorHandler) {
        invokeLater(() -> {
            try {
                registerChannelNow(channel, selectionKeys, handler);
            } catch (IOException e) {
                errorHandler.handleError("failed to register keys "
                        + keyToString(selectionKeys), e);
            }
        });
    }

    /**
     * Registers a SelectableChannel with this selector. This channel will
     * start to be monitored by the selector for the set of events associated
     * with it. When an event is raised, the corresponding handler is
     * called.
     *
     * This method can be called multiple times with the same channel
     * and selector. Subsequent calls update the associated interest set
     * and selector handler to the ones given as arguments.
     *
     * This method should only be called on the selector thread. Otherwise
     * an exception is thrown. Use the registerChannelLater() when calling
     * from another thread.
     * @param channel The channel to be monitored.
     * @param selectionKeys The interest set. Should be a combination of
     * SelectionKey constants.
     * @param handler The handler for events raised on the registered channel.
     * @throws java.io.IOException on error
     */
    public void registerChannelNow(SelectableChannel channel,
            int selectionKeys,
            SelectorHandler handler) throws IOException {
        if (isClosed()) {
            throw new IOException("closed");
        }
        if (!channel.isOpen()) {
            throw new IOException("Channel is not open.");
        }

        checkSelectorThread();

        try {
            if (channel.isRegistered()) {
                SelectionKey sk = channel.keyFor(this.selector);
                if (sk == null) {
                    throw new IllegalStateException("Channel is already registered with other selector");
                }
                sk.interestOps(selectionKeys);
                sk.attach(handler);
            }
            else {
                channel.configureBlocking(false);
                channel.register(selector, selectionKeys, handler);
            }
        } catch (Exception e) {
            IOException ioe = new IOException("Error registering channel.");
            ioe.initCause(e);
            throw ioe;
        }
    }

    private SelectionKey getSelectionKey(SelectableChannel channel) throws IOException {
        final SelectionKey result = channel.keyFor(selector);
        if (result == null) {
            throw new IOException(channel + " not registered with selector");
        }
        return result;
    }

    void checkSelectorThread() throws IOException {
        if (!isCurrentThreadSelectorThread()) {
            throw new IOException("Method can only be called from selector thread");
        }
    }

    void checkNotSelectorThread() {
        if (isCurrentThreadSelectorThread()) {
            throw new IllegalStateException("Method must not be called from selector thread");
        }
    }

    boolean isCurrentThreadSelectorThread() {
        return Thread.currentThread() == this.selectorThread || !this.selectorThread.isAlive();
    }

    /**
     * Executes the given task in the selector thread. This method returns
     * as soon as the task is scheduled, without waiting for it to be
     * executed.
     * @param run The task to be executed.
     */
    public void invokeLater(Runnable run) {
        if (isClosed()) {
            return;
        }
        if (this.pendingInvocations.offer(run)) {
            this.selector.wakeup();
        }
        else {
            this.logger.error("<invokeLater> invoke queue is full");
        }
    }

    /**
     * Executes the given task synchronously in the selector thread. This
     * method schedules the task, waits for its execution and only then
     * returns.
     * @param task The task to be executed on the selector's thread.
     * @throws InterruptedException if interrupted while waiting
     */
    public void invokeAndWait(final Runnable task)
            throws InterruptedException {
        if (isClosed()) {
            return;
        }
        if (isCurrentThreadSelectorThread()) {
            task.run();
        }
        else {
            final CountDownLatch latch = new CountDownLatch(1);
            this.invokeLater(() -> {
                task.run();
                latch.countDown();
            });
            latch.await();
        }
    }

    /**
     * Executes all tasks queued for execution on the selector's thread.
     */
    private void doInvocations() {
        Runnable task;
        while ((task = this.pendingInvocations.poll()) != null) {
            run(task);
        }
    }

    private void run(Runnable task) {
        try {
            task.run();
        } catch (Throwable t) {
            this.logger.error("<run> failed for task " + t, t);
        }
    }

    public String getSelectionKeys() throws InterruptedException {
        final StringBuilder sb = new StringBuilder(800);
        invokeAndWait(() -> {
            final Set<SelectionKey> selectionKeys = selector.keys();
            for (SelectionKey selectionKey : selectionKeys) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(selectionKey.channel());
                sb.append("[");
                sb.append("interest=").append(keyToString(selectionKey.interestOps()));
                sb.append(", ready=").append(keyToString(selectionKey.readyOps()));
                sb.append("]");
            }
        });
        return sb.toString();
    }

    private static String keyToString(int ops) {
        final StringBuilder sb = new StringBuilder(20);
        sb.append("[");
        if ((ops & SelectionKey.OP_ACCEPT) != 0) {
            sb.append(" OP_ACCEPT");
        }
        if ((ops & SelectionKey.OP_CONNECT) != 0) {
            sb.append(" OP_CONNECT");
        }
        if ((ops & SelectionKey.OP_READ) != 0) {
            sb.append(" OP_READ");
        }
        if ((ops & SelectionKey.OP_WRITE) != 0) {
            sb.append(" OP_WRITE");
        }
        return sb.append("]").toString();
    }

    private void doRun() {
        while (true) {
            // Execute all the pending tasks.
            doInvocations();

            if (isClosed()) {
                this.logger.info("<run> close requested, returning");
                return;
            }

            int selectedKeys;
            try {
                selectedKeys = doSelect();
            } catch (IOException ioe) {
                // Select should never throw an exception under normal
                // operation. If this happens, print the error and try to
                // continue working.
                this.logger.error("<run> select failed, should never happen", ioe);
                continue;
            }

            if (selectedKeys == 0) {
                ackNop();
                continue;
            }

            // Someone is ready for IO, get the ready keys
            final Iterator it = selector.selectedKeys().iterator();
            while (it.hasNext()) {
                final SelectionKey sk = (SelectionKey) it.next();
                it.remove();
                try {
                    // we have to check for valid keys otherwise the readOps might throw CancelledKeyException
                    if (!sk.isValid()) {
                        this.logger.warn("<run> ignoring an invalid selection key, the client might have disconnected");
                        continue;
                    }
                    int readyOps = sk.readyOps();
                    // Disable the interest for the operation that is ready.
                    // This prevents the same event from being raised multiple
                    // times.
                    sk.interestOps(sk.interestOps() & ~readyOps);
                    SelectorHandler handler =
                            (SelectorHandler) sk.attachment();

                    // Some of the operations set in the selection key
                    // might no longer be valid when the handler is executed.
                    // So handlers should take precautions against this
                    // possibility.

                    // Check what are the interests that are active and
                    // dispatch the event to the appropriate method.
                    if (sk.isAcceptable()) {
                        // A connection is ready to be completed
                        ((AcceptSelectorHandler) handler).handleAccept();
                    }
                    else if (sk.isConnectable()) {
                        // A connection is ready to be accepted
                        ((ConnectorSelectorHandler) handler).handleConnect();
                    }
                    else {
                        // Readable or writable
                        ReadWriteSelectorHandler rwHandler =
                                (ReadWriteSelectorHandler) handler;
                        if (sk.isReadable()) {
                            rwHandler.handleRead();
                        }

                        // Check if the key is still valid, since it might
                        // have been invalidated in the read handler
                        // (for instance, the socket might have been closed)
                        if (sk.isValid() && sk.isWritable()) {
                            rwHandler.handleWrite();
                        }
                    }
                } catch (Throwable t) {
                    // No exceptions should be thrown in the previous block!
                    // So kill everything if one is detected.
                    // Makes debugging easier.
                    this.logger.error("<run> failed", t);
                    return;
                }
            }
        }
    }

    private int doSelect() throws IOException {
        if (this.timeout > 0) {
            return this.selector.select(this.timeout);
        }
        try {
            return this.selector.selectNow();
        } finally {
            this.timeout = SECOND_IN_MILLIS;
        }
    }

    private void ackNop() {
        if (!this.nopListeners.isEmpty()) {
            this.nopListeners.forEach(NopListener::ackNop);
        }
    }

    /**
     * Closes all channels registered with the selector and the selector itself during shutdown.
     */
    private void closeSelectorAndChannels() {
        for (SelectionKey key : this.selector.keys()) {
            try {
                key.channel().close();
            } catch (IOException e) {
                // Ignore
            }
        }
        try {
            this.selector.close();
        } catch (IOException e) {
            // Ignore
        }
        this.closeRequested.set(true);
        this.logger.info("<closeSelectorAndChannels> finished");
    }

    private boolean isClosed() {
        return this.closeRequested.get();
    }

    public boolean isDone() {
        return this.done;
    }
}
