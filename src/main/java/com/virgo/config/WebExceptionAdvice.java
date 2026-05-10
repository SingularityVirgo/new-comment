package com.virgo.config;

import com.virgo.common.exception.BizException;
import com.virgo.web.api.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "业务异常";
        log.warn("业务异常: {}", msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<?> handleIllegalArgument(IllegalArgumentException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "参数错误";
        log.warn("参数异常: {}", msg);
        return Result.fail(msg);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("未处理异常", e);
        return Result.fail("服务器异常");
    }
}
