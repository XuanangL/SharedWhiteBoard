/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Server;

import Client.ClientInterface;
import Client.MyShape;
import Remote.RemoteWhiteBoardInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WhiteBoardObject extends UnicastRemoteObject implements RemoteWhiteBoardInterface {
    private CopyOnWriteArrayList<MyShape> myShapes = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> chats = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String, ClientInterface> clients =  new ConcurrentHashMap<>();
    private final String admin;
    private byte[] currentBackgroundImg;

    public WhiteBoardObject(String setAdmin) throws RemoteException {
        admin = setAdmin;
    }

    // It is called when new user wants to connect
    public boolean addClient(ClientInterface client, String username) throws RemoteException {

        System.out.println("add  client"+client);
        clients.put(username, client);
        for (ClientInterface c: clients.values()) {
            c.updateChats();
            // check if current whiteboard have any drawing,
            // if it has then isModified in client is set to true
            if (!myShapes.isEmpty()) {
                c.updateShapes();
            } else {
                c.resetShapes();
            }
            c.updateUsers();
            c.receiveImage(currentBackgroundImg);
        }
        return true;
    }

    // It is called when user sends a message in the chat
    public void sendMessage(String message) throws RemoteException {
        chats.add(message);
        for (ClientInterface c: clients.values()) {
            c.updateChats();
        }
    }

    public CopyOnWriteArrayList<String> getChats() throws RemoteException {
        return chats;
    }

    //  When user made a new drawing, server calls all clients to update their canvas
    @Override
    public boolean newDrawing(MyShape currentMyShape) throws RemoteException {
        myShapes.add(currentMyShape);
        for (ClientInterface c: clients.values()) {
            c.updateShapes();

        }
        return true;
    }
    public CopyOnWriteArrayList<MyShape> getShapes() throws RemoteException {
        return myShapes;
    }

    public String getAdmin()throws RemoteException {
        return admin;
    }

    // reset white board to origin, only reset whiteboard and image.
    public void resetWhiteBoard() throws RemoteException {
        myShapes.clear();
        currentBackgroundImg = null;
        System.out.println("reset white board");
        for (ClientInterface c: clients.values()) {
            c.resetShapes();
            c.receiveImage(null);
        }
    }

    public ArrayList<String> getUsers() throws RemoteException {
        return new ArrayList<>(clients.keySet());
    }

    // Remove a given user
    public void removeUser(String username) throws RemoteException {
        clients.get(username).notifyKicked();
        clients.remove(username); // Remove the user from the clients map

        // Notify all clients about the change
        for (ClientInterface client : clients.values()) {
            client.updateUsers();
        }
    }

    // Handle user join request
    public boolean requestJoin(String username) throws RemoteException {
        // check if given username already exists
        if (!clients.isEmpty() && clients.containsKey(username)) {
            System.out.println("Client already exists: " + username);
            return false;
        }
        // If the first user is admin, accepts
        if (username.equals(admin) && clients.isEmpty()) {
            return true;
        }
        // Send a notification to the manager
        ClientInterface managerClient = clients.get(admin);
        boolean res1 = managerClient.notifyJoinRequest(username);

        System.out.println("res1 "+res1);

        return (res1);
    }

    // It is called when admin quits
    @Override
    public void notifyShutdown() throws RemoteException {
        // Notify all clients that the server is shutting down
        for (ClientInterface client : clients.values()) {
            try {
                client.shutdown();
            } catch (RemoteException e) {
                System.err.println("Error notifying client: " + e.getMessage());
            }
        }
        System.exit(0);  // Terminate the server process
    }

    // User closes the application
    public void userQuit(String username) throws RemoteException {
        clients.remove(username); // Remove the user from the clients map

        // Notify all users to update user list
        for (ClientInterface client : clients.values()) {
            client.updateUsers();
        }
    }

    // It is called when user opens a file
    public void uploadImage(byte[] imageData) throws RemoteException {
        currentBackgroundImg = imageData;
        // Broadcast the image to all clients
        for (ClientInterface client : clients.values()) {
            client.receiveImage(imageData);
        }
    }
}
