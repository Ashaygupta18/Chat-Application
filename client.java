import java.io.*;
import java.net.*;
import java.util.*;

public class client {
    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private String username;

    public static void main(String[] args) {
        client client = new client();
        client.startClient("localhost", 5000); // change IP if needed
    }

    public void startClient(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            System.out.println("Connected to chat server.");

            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);

            Scanner sc = new Scanner(System.in);
            String serverResponse;

            while(true){
                System.out.print("Enter your Username: ");
                username = sc.nextLine();
                output.println(username);
                
                serverResponse = input.readLine();
                System.out.println(serverResponse);
                
                if(serverResponse != null){
                    break;
                }
            }
                
                // Thread to read server messages
                new Thread(() -> {
                try {
                    String serverMsg;
                    while ((serverMsg = input.readLine()) != null) {
                        System.out.println(serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();
            // Main loop to send messages
            while (true) {
                String input = sc.nextLine();
                
                if (input.equalsIgnoreCase("/exit")) {
                    System.out.println("You left the chat.");
                    socket.close();
                    break;
                }
                output.println(input);
            }
            sc.close();

        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }
}
