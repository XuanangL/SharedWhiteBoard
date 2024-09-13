/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
import Client.WhiteBoardClient;
import Server.WhiteBoardServer;
import java.rmi.RemoteException;

public class CreateWhiteBoard {
    public static void main(String[] args) {
        try {
            // Check for the correct number of arguments
            if (args.length != 3) {
                throw new IllegalArgumentException("Usage: java CreateWhiteBoard <serverIpAddr> <port> <username>");
            }

            String serverIpAddr = args[0]; // IP address of the server
            int port = Integer.parseInt(args[1]); // Port number
            String username = args[2]; // Username of the user

            // Start the server
            WhiteBoardServer whiteBoardServer = new WhiteBoardServer(serverIpAddr, port, username);
            whiteBoardServer.start();

            // Start the client
            WhiteBoardClient whiteBoardClient = new WhiteBoardClient(serverIpAddr, port, username);
            whiteBoardClient.start();

        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be an integer.");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        } catch (RemoteException e) {
            System.err.println("Remote exception: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            System.exit(1);
        }
    }
}
