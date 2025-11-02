/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package controller;

/**
 *
 * @author hn235
 */
public interface UIListener {
    public void onDataUpdated(String type, Object obj);
    public void onConnectedToServer();
    public void onDisconnectedFromServer();
    public void onError(String message);
}
