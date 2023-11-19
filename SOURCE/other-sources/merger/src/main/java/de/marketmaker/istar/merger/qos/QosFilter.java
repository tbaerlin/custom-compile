package de.marketmaker.istar.merger.qos;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.Lifecycle;

public abstract class QosFilter<T> implements Lifecycle, Runnable, InitializingBean, BeanNameAware {
    protected T delegate;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String beanName;

    private long checkIntervalMillis = TimeUnit.SECONDS.toMillis(30);

    private volatile boolean enabled;

    private Thread qosCheck;

    private volatile boolean stopped = false;

    public void afterPropertiesSet() throws Exception {
        if (this.delegate == null) {
            throw new IllegalStateException("no delegate set");
        }
        updateStatus();
    }

    public void run() {
        while (!this.stopped) {
            try {
                Thread.sleep(this.checkIntervalMillis);
            } catch (InterruptedException e) {
                if (!this.stopped) {
                    this.logger.error("<run> Interrupted Exception: " + e.getMessage());
                }
                continue;
            }
            updateStatus();
        }
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setCheckIntervalSecs(long checkIntervalSecs) {
        this.checkIntervalMillis = TimeUnit.SECONDS.toMillis(checkIntervalSecs);
    }

    public void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    public void start() {
        this.qosCheck = new Thread(this, this.beanName + "-Check");
        this.qosCheck.setDaemon(true);
        this.qosCheck.start();
    }

    public void stop() {
        this.stopped = true;
        this.logger.info("<stop> stopping QosCheck-Thread for " + this.beanName);
        this.qosCheck.interrupt();
        try {
            this.qosCheck.join(30000);
        } catch (InterruptedException e) {
            this.logger.error("<stop> interrupted?!");
            Thread.currentThread().interrupt();
        }
        if (this.qosCheck.isAlive()) {
            this.logger.warn("<stop> QosCheck-Thread still running, returning");
        }
    }

    @Override
    public boolean isRunning() {
        return this.qosCheck != null && this.qosCheck.isAlive();
    }

    protected T getDelegate() {
        return delegate;
    }

    protected boolean isEnabled() {
        return this.enabled;
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    protected abstract boolean tryService() throws Exception;

    private boolean getStatus() {
        try {
            return tryService();
        } catch (Throwable t) {
            this.logger.error("<getStatus> failed", t);
        }
        return false;
    }

    private void updateStatus() {
        final boolean wasEnabled = this.enabled;
        this.enabled = getStatus();

        if (this.isEnabled()) {
            this.logger.info("<updateStatus> delegate enabled for " + this.beanName);
            if (!wasEnabled) {
                this.logger.info("__OK__"); // Explizites Nagiosrecovery if ok again
            }
        }
        else {
            this.logger.error("<updateStatus> delegate disabled for " + this.beanName);
        }
    }
}
