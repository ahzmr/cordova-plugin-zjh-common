package com.zhijianhuo.cordova.core.demain;

import java.io.Serializable;

/**
 * 通用的返回信息
 * @author zhijianhuo@gmail.com
 */
public class SimpleResult<T> implements Serializable {
    /**
     * 成功的状态码
     */
    public static final int SUCCESS_CODE = 0;
    /**
     * 通常的错误状态
     */
    public static final int COMM_FAIL_CODE = 1;
    /**
     * 返回状态: 0 成功，1 业务失败，401 未登录，403 无权访问
     */
    private int code = SUCCESS_CODE;
    /**
     * 返回消息
     */
    private String message;
    /**
     * 返回结果
     */
    private T result;
    /**
     * 异常信息
     */
    private Throwable exception;

    /**
     * 构造正确的结果
     */
    public SimpleResult() {
    }

    public <E> SimpleResult(SimpleResult<E> result) {
        if(null != result) {
            this.code = result.code;
            this.message = result.message;
        }
    }

    /**
     * 构造正确的结果
     * @param result 结果
     */
    public SimpleResult(T result) {
        this.result = result;
    }

    /**
     * @param code 状态
     * @param message 信息
     */
    public SimpleResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * @param code 状态
     * @param message 信息
     * @param exception 异常信息
     */
    public SimpleResult(int code, String message, Throwable exception) {
        this.code = code;
        this.message = message;
        this.exception = exception;
    }

    /**
     * @param errMsg 错误消息
     */
    public static <E> SimpleResult<E> newCommFailResult(String errMsg) {
        return new SimpleResult<E>(COMM_FAIL_CODE, errMsg);
    }

    /**
     * @param errMsg 错误消息
     * @param result 错误结果
     */
    public static <E> SimpleResult<E> newCommFailResult(String errMsg, E result) {
        final SimpleResult<E> simpleResult = new SimpleResult<E>(COMM_FAIL_CODE, errMsg);
        simpleResult.setResult(result);
        return simpleResult;
    }

    /**
     * @param errMsg 错误消息
     * @param e 异常信息
     * @param result 错误结果
     */
    public static <E> SimpleResult<E> newCommExceptionResult(String errMsg, Exception e, E result) {
        final SimpleResult<E> simpleResult = new SimpleResult<E>(COMM_FAIL_CODE, errMsg, e);
        simpleResult.setResult(result);
        return simpleResult;
    }

    /**
     * @param result 结果
     */
    public static <E> SimpleResult<E> newSuccessResult(E result) {
        return new SimpleResult<E>(result);
    }

    /**
     * @param result 结果
     * @param message 成功消息
     */
    public static <E> SimpleResult<E> newSuccessResult(E result, String message) {
        SimpleResult<E> simpleResult = new SimpleResult<E>(result);
        simpleResult.setMessage(message);
        return simpleResult;
    }

    public int getCode() {
        return code;
    }

    public SimpleResult<T> setCode(int code) {
        this.code = code;
        return this;
    }

    public boolean isSeccess() {
        return SUCCESS_CODE == code;
    }

    public String getMessage() {
        return message;
    }

    public SimpleResult<T> setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getResult() {
        return result;
    }

    public SimpleResult<T> setResult(T result) {
        this.result = result;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public SimpleResult<T> setException(Throwable exception) {
        this.exception = exception;
        return this;
    }

    @Override
    public String toString() {
        return "SimpleResult{" +
                "result=" + result +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
