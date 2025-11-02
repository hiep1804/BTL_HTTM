/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

/**
 *
 * @author hn235
 */
public class Message<T> {
    private String type;
    private T data;

    public Message() {}

    public Message(String type, T data) {
        this.type = type;
        this.data = data;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}
