package com.example.myapplication.model;

/**
 * Wrapper khớp với BaseResponse<T> của BE:
 *   { "data": [...], "message": "successfully" }
 */
public class BaseResponse<T> {
    private T data;
    private String message;
    private int status;

    public T getData() { return data; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}