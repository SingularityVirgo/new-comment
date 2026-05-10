package com.virgo.common.exception;

/**
 * 业务规则不满足时抛出，由全局异常处理转换为 {@link com.virgo.web.api.Result#fail} 响应。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
