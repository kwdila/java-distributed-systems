import java.net.*;
import java.io.*;
import java.util.*;

public class Beta {
    private static final int PORT = 5002;
    private static final String WORKER_ADDRESS = "localhost";
    private static int lamportClock = 0;
    private static Message lastReceivedMessage = null;

    public static void main(String[] args) {
        System.out.println("Beta worker started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    Message message = (Message) in.readObject();

                    if (message.getWord().equals("REQUEST")) {
                        // Send back the last processed message if we have one
                        if (lastReceivedMessage != null) {
                            out.writeObject(lastReceivedMessage);
                        } else {
                            out.writeObject(new Message("", 0)); // Send empty message if nothing processed yet
                        }
                    } else {
                        // Process new word
                        lamportClock = Math.max(lamportClock, message.getClock()) + 1;
                        lastReceivedMessage = new Message(message.getWord(), lamportClock);
                        System.out.println("Beta received: " + message.getWord() + " with clock: " + lamportClock);
                        out.writeObject(lastReceivedMessage);
                    }
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start Beta worker on port " + PORT + ": " + e.getMessage());
        }
    }
}