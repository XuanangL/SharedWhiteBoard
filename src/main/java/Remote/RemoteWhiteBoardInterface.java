/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Remote;
import Client.ClientInterface;
import Client.MyShape;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

public interface RemoteWhiteBoardInterface extends Remote {
    // Send new draw to server
    public boolean newDrawing(MyShape currentMyShape) throws RemoteException;
    // get all drawings
    public CopyOnWriteArrayList<MyShape> getShapes() throws RemoteException;
    // Send new message to server
    public void sendMessage(String message) throws RemoteException;
    // Get all chats
    public CopyOnWriteArrayList<String> getChats() throws RemoteException;
    // Add a new client
    public boolean addClient(ClientInterface client, String username) throws RemoteException;
    // Get the admin of the whiteboard
    public String getAdmin()throws RemoteException;
    // Reset whiteboard,
    public void resetWhiteBoard() throws RemoteException;
    // Get all users
    public ArrayList<String> getUsers() throws RemoteException;
    // Remove a user
    public void removeUser(String username) throws RemoteException;
    // Handle join request
    public boolean requestJoin(String username) throws RemoteException;
    // Notify user to shutdown
    public void notifyShutdown() throws RemoteException;
    // Handle user quit
    public void userQuit(String username) throws RemoteException;
    // Upload image to server
    void uploadImage(byte[] imageData) throws RemoteException;

}
