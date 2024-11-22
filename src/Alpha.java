import java.net.*;
import java.io.*;
import java.util.*;

public class Alpha {
    private static final int PORT = 5001;
    private static final String WORKER_ADDRESS = "localhost";
    private static int lamportClock = 0;
    private static List<Message> storedMessages = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("Alpha worker started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
                     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

                    Message message = (Message) in.readObject();

                    if (message.getWord().equals("COLLECT")) {
                        // Send all stored messages
                        for (Message stored : storedMessages) {
                            out.writeObject(stored);
                        }
                        out.writeObject(new Message("END", 0));
                        out.flush();
                    } else {
                        // Process new word
                        lamportClock = Math.max(lamportClock, message.getClock()) + 1;
                        Message newMessage = new Message(message.getWord(), lamportClock);
                        storedMessages.add(newMessage);
                        System.out.println("Alpha received: " + message.getWord() + " with clock: " + lamportClock);
                        out.writeObject(newMessage);
                    }
                    out.flush();

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error handling client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start Alpha worker on port " + PORT + ": " + e.getMessage());
        }
    }
}