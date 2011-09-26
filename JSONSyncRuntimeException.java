package com.jsonsync;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
public class JSONSyncRuntimeException extends RuntimeException {

    /**
     * Creates a new instance of <code>IsokronException</code> without detail message.
     */
    public JSONSyncRuntimeException() {
    }

    /**
     * Constructs an instance of <code>IsokronException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public JSONSyncRuntimeException(String msg) {
        super(msg);
    }

    /**
     * Constructs an instance of <code>IsokronException</code> with the specified detail message and exception
     * @param msg the detail message.
     */
    public JSONSyncRuntimeException(String msg, Throwable t) {
        super(msg, t);
    }

    public JSONSyncRuntimeException(Throwable t) {
        super("", t);
    }
}
