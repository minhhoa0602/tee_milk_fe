package com.example.myapplication.model;

public class BaseResponse<T> {
    private int code;
    private T data;
    private String message;

    public int getCode() { return code; }
    public T getData() { return data; }
    public String getMessage() { return message; }
}
