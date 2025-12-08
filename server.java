import java.io.*;
import java.net.*;
import java.util.*;


public class server {
    private ServerSocket serverSocket;
    // Store username → ClientHandler mapping
    private Map<String, ClientHandler> clients = Collections.synchronizedMap(new HashMap<>());

    public static void main(String[] args) {
        server server = new server();
        server.startServer(5000); 
}
 public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);

                // Each client will send username first
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                String username = in.readLine();

                // Handle username collisions
                while (clients.containsKey(username)) {
                    out.println("Username already taken. Enter a different username: ");
                    username = in.readLine();
                }

                ClientHandler handler = new ClientHandler(socket, username, this);
                clients.put(username, handler);

                new Thread(handler).start();

                broadcast("Server", username + " joined the chat!");
            }

        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    public void broadcast(String sender, String message) {
        synchronized (clients) {
            for (ClientHandler client : clients.values()) {
                client.sendMessage(sender + ": " + message);
            }
        }
    }

    public void sendPrivate(String sender, String receiver, String message) {
        ClientHandler target = clients.get(receiver);
        if (target != null) {
            target.sendMessage("(Private) " + sender + ": " + message);
        } else {
            ClientHandler senderHandler = clients.get(sender);
            if (senderHandler != null) {
                senderHandler.sendMessage("User '" + receiver + "' not found.");
            }
        }
    }

    public void removeClient(String username) {
        clients.remove(username);
        broadcast("Server", username + " left the chat.");
    }
}
