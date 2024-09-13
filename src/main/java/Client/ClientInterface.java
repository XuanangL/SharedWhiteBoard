/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientInterface extends Remote {
    // Update user interface's drawing
    public void updateShapes() throws RemoteException;
    // Update user interface's chats
    public void updateChats() throws RemoteException;
    // Update user interface's user list
    public void updateUsers() throws RemoteException;
    // Notify that you have been kicked out
    public void notifyKicked() throws RemoteException;
    // Check if a new user can join in (Only admin)
    public boolean notifyJoinRequest(String username) throws RemoteException;
    // Close application
    public void shutdown() throws RemoteException;
    // Client side receive an image
    public void receiveImage(byte[] imageData) throws RemoteException;
    // Client interface reset all drawings
    public void resetShapes() throws RemoteException;
}
