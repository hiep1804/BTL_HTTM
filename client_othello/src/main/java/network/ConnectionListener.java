/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package network;

/**
 *
 * @author hn235
 */
public interface ConnectionListener {
    void onConnected();
    void onDisconnected();
    void onError(Throwable error);
}
