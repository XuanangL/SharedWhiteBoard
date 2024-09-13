/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
package Server;

import Remote.RemoteWhiteBoardInterface;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WhiteBoardServer {
    private String serverIP;
    private String username;
    private int port;
    private String serviceName = "SharedWhiteBoard";

    public WhiteBoardServer(String setServerIP, int setPort, String setUsername) {
        serverIP = setServerIP;
        port = setPort;
        username = setUsername;
    }
    public void start() {
        try {
            System.setProperty("java.rmi.server.hostname", serverIP);  // set server ip address
            Registry registry = LocateRegistry.createRegistry(port);   // create registry
            RemoteWhiteBoardInterface whiteBoard = new WhiteBoardObject(username);
            registry.bind(serviceName, whiteBoard);  // bind whiteboard object
            System.out.println(serviceName+" server ready");
        } catch (RemoteException e) {
            System.out.println("remote Exception"+e.getMessage());
        } catch (AlreadyBoundException e) {
            throw new RuntimeException(e);
        }
    }
}