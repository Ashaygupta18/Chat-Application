import java.io.*;
import java.net.*;


public class ClientHandler implements Runnable {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String username;
    private server server;

    public ClientHandler(Socket socket, String username, server server) {
        this.socket = socket;
        this.username = username;
        this.server = server;

        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.out.println("Error setting up client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            writer.println(
                    "Welcome " + username + "! Type @username <message> for private messages or just type to chat.");

            String message;
            while ((message = reader.readLine()) != null) {
                if (message.equalsIgnoreCase("/exit")) {
                    writer.println("You have disconnected.");
                    server.removeClient(username);
                    if (reader != null)
                        reader.close();
                    if (writer != null)
                        writer.close();
                    if (socket != null && !socket.isClosed())
                        socket.close();
                    break;
                }

                // Handle private message
                if (message.startsWith("@")) {
                    int spaceIndex = message.indexOf(' ');
                    if (spaceIndex != -1) {
                        String targetUser = message.substring(1, spaceIndex);
                        String privateMsg = message.substring(spaceIndex + 1);
                        server.sendPrivate(username, targetUser, privateMsg);
                    } else {
                        writer.println("Invalid private message format. Use @username <message>");
                    }
                } else {
                    server.broadcast(username, message);
                }
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected unexpectedly.");
        } finally {
            server.removeClient(username);
            try {
                if (reader != null)
                    reader.close();
                if (writer != null)
                    writer.close();
                if (socket != null && !socket.isClosed())
                    socket.close();
            } catch (IOException e) {
                System.out.println("Error closing socket for " + username);
            }
        }
    }

    public void sendMessage(String message) {
        writer.println(message);
    }

}
