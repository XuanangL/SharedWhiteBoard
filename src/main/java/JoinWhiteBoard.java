/* Author: Li xuanang
    StudentID: 1218931
    Date: 2024/05/02
 */
import Client.WhiteBoardClient;
import java.rmi.RemoteException;

public class JoinWhiteBoard {
    public static void main(String[] args) {
        try {
            // Check if the correct number of arguments is provided
            if (args.length != 3) {
                throw new IllegalArgumentException("Usage: java JoinWhiteBoard <serverIpAddr> <port> <username>");
            }

            String serverIpAddr = args[0]; // IP address of the server
            int port = Integer.parseInt(args[1]); // Port number
            String username = args[2]; // Username

            // Initialize the WhiteBoardClient
            WhiteBoardClient whiteBoardClient = new WhiteBoardClient(serverIpAddr, port, username);
            whiteBoardClient.start(); // Start the client

        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be an integer.");
            System.exit(1); // Exit with a non-zero value to indicate error
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1); // Exit due to incorrect argument usage
        } catch (RemoteException e) {
            System.err.println("RemoteException: Unable to connect to the server at " + args[0] + ":" + args[1]);
            System.exit(1); // Exit due to remote exception, indicating connection issues
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Handle any other unexpected exceptions
        }
    }
}
