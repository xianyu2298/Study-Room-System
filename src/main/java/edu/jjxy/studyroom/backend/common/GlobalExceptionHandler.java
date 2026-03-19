package edu.jjxy.studyroom.backend.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public R<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 - URI: {}, Code: {}, Message: {}", request.getRequestURI(), e.getCode(), e.getMessage());
        return R.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数校验失败 - URI: {}, Errors: {}", request.getRequestURI(), message);
        return R.error(ResultCode.PARAMETER_ERROR.getCode(), String.format(ResultCode.PARAMETER_ERROR.getMessage(), message));
    }

    /**
     * 参数绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e, HttpServletRequest request) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败 - URI: {}, Errors: {}", request.getRequestURI(), message);
        return R.error(ResultCode.PARAMETER_ERROR.getCode(), String.format(ResultCode.PARAMETER_ERROR.getMessage(), message));
    }

    /**
     * 约束校验异常处理（@Validated）
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e, HttpServletRequest request) {
        String message = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束校验失败 - URI: {}, Errors: {}", request.getRequestURI(), message);
        return R.error(ResultCode.PARAMETER_ERROR.getCode(), String.format(ResultCode.PARAMETER_ERROR.getMessage(), message));
    }

    /**
     * 缺少请求参数异常处理
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        log.warn("缺少请求参数 - URI: {}, Parameter: {}", request.getRequestURI(), e.getParameterName());
        return R.error(ResultCode.PARAMETER_ERROR.getCode(),
                String.format(ResultCode.PARAMETER_ERROR.getMessage(), "缺少参数: " + e.getParameterName()));
    }

    /**
     * 请求方法不支持异常处理
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException e, HttpServletRequest request) {
        log.warn("请求方法不支持 - URI: {}, Method: {}", request.getRequestURI(), e.getMethod());
        return R.error(ResultCode.REQUEST_METHOD_ERROR);
    }

    /**
     * 404异常处理
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public R<Void> handleNoHandlerFoundException(NoHandlerFoundException e, HttpServletRequest request) {
        log.warn("404 - URI: {}, Path: {}", request.getRequestURI(), e.getRequestURL());
        return R.error(ResultCode.DATA_NOT_FOUND);
    }

    /**
     * 其他异常处理
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 - URI: {}", request.getRequestURI(), e);
        return R.error(ResultCode.INTERNAL_SERVER_ERROR);
    }
}
