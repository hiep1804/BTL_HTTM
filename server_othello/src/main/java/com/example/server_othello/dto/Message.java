package com.example.server_othello.dto;

public class Message<T> {
    private String type;
    private T data;

    public Message() {}
    public Message(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public String getType() { return type; }
    public T getData() { return data; }
    public void setType(String type) { this.type = type; }
    public void setData(T data) { this.data = data; }
}
