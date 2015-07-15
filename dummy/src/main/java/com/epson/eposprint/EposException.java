package com.epson.eposprint;

public class EposException extends RuntimeException {
    public static final int ERR_PARAM = 1;

    public EposException() {
        super();
    }

    public EposException(String message) {
        super(message);
    }

    public EposException(String message, Throwable cause) {
        super(message, cause);
    }

    public EposException(Throwable cause) {
        super(cause);
    }

    public int getErrorStatus() {
        return ERR_PARAM;
    }
}
