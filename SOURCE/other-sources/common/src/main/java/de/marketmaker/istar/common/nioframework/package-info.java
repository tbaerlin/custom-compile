/**
 * Framework for clients and servers using non-blocking IO.
 * <p>
 * Inspired by <a href="http://www.onjava.com/pub/a/onjava/2004/09/01/nio.html?page=1">
 * Building Highly Scalable Servers with Java NIO</a>, but completely refactored into
 * a solution that is compatible with spring and makes heavy use of
 * <code>java.util.concurrent</code> to avoid synchronized blocks or methods.
 *
 * @see java.util.concurrent
 */
package de.marketmaker.istar.common.nioframework;
