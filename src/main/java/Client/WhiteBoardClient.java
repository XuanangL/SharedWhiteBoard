/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Client;

import Remote.RemoteWhiteBoardInterface;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.atomic.AtomicBoolean;


public class WhiteBoardClient extends UnicastRemoteObject implements ClientInterface {
    private String serverIP;
    private int port;
    private  RemoteWhiteBoardInterface server;
    private String username;
    private WhiteboardGUI gui;

    public WhiteBoardClient(String serverIPaddr, int setPort,String setUsername) throws RemoteException {
        serverIP = serverIPaddr;
        port = setPort;
        username = setUsername;

    }

    // Start client GUI
    public void start() {
        try {
            Registry registry = LocateRegistry.getRegistry(serverIP, port);
            server = (RemoteWhiteBoardInterface) registry.lookup("SharedWhiteBoard");

            if (!server.requestJoin(username)) {
                System.out.println("Join request denied or username is duplicate, please choose another username or try again later.");
                return;
            }
            gui = new WhiteboardGUI(server, username);
            server.addClient(this, username);
            System.out.println("Connected: " + username);

        } catch (ConnectException e) {
            System.out.println("Connection Exception: Unable to connect to server.");
//            throw new RuntimeException(e);
        }catch (RemoteException e) {
            System.out.println("Remote exception");
//            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            System.out.println("not bound");
//            throw new RuntimeException(e);
        }
    }

    // Update all drawings from server
    @Override
    public void updateShapes() throws RemoteException {
        System.out.println("client:update shapes");
        gui.guiUpdateShapes();
    }

    // Update all chats from server
    @Override
    public void updateChats() throws RemoteException {
        gui.guiUpdateChats();
    }

    // Update user list from user
    @Override
    public void updateUsers() throws RemoteException {
        gui.updateUsersDisplay();
    }

    // It is called when client has been kicked by admin
    @Override
    public void notifyKicked() throws RemoteException {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, "You have been kicked out by the admin.",
                    "Kicked Out", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        });
    }

    // Notify admin if there is a new client
    @Override
    public boolean notifyJoinRequest(String username) throws RemoteException {
        // This needs to be an atomic reference to handle the value inside and outside the EDT
        return gui.notifyJoinRequest(username);
    }

    // Close application
    public void shutdown() {
        gui.shutdown();
    }

    // Set GUi image
    @Override
    public void receiveImage(byte[] imageData) throws RemoteException {
        gui.guiReceiveImage(imageData);
    }

    // Clear all drawings
    @Override
    public void resetShapes() throws RemoteException {
        gui.guiResetShapes();
    }

}
