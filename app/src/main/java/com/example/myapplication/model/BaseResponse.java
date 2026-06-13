package com.example.myapplication.model;

public class BaseResponse<T> {
    private T data;
    private String message;
    private int status;

    public T getData() { return data; }
    public String getMessage() { return message; }
    public int getStatus() { return status; }
}
