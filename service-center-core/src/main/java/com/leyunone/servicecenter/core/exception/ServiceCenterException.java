package com.leyunone.servicecenter.core.exception;

/**
 * :)
 *
 * @author LeYunone
 * @email 365627310@qq.com
 * @date 2023-07-30
 */
public class ServiceCenterException extends RuntimeException{

    static final long serialVersionUID = -7034897190745766939L;

    public ServiceCenterException() {
        super();
    }

    public ServiceCenterException(String message) {
        super(message);
    }

    public ServiceCenterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceCenterException(Throwable cause) {
        super(cause);
    }

    protected ServiceCenterException(String message, Throwable cause,
                              boolean enableSuppression,
                              boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
