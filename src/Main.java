import java.net.*;
import java.util.*;
import java.io.*;

public class Main {
    private static final String WORKER_ADDRESS = "localhost";
    private static final int[] WORKER_PORTS = {5001, 5002, 5003, 5004, 5005};
    private static int lamportClock = 0;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a paragraph: ");
        String paragraph = scanner.nextLine();
        if (paragraph.trim().isEmpty()) {
            System.err.println("Paragraph cannot be empty. Please restart and enter valid input.");
            return;
        }
        String[] words = paragraph.split("\\s+");

        for (String word : words) {
            sendWordToWorker(word);
        }

        System.out.println("Waiting for responses...");
        try {
            Thread.sleep(1000); // Give workers time to process
        } catch (InterruptedException e) {
            System.err.println("Error while waiting: " + e.getMessage());
        }

        List<Message> receivedMessages = new ArrayList<>();
        for (int workerPort : WORKER_PORTS) {
            Message response = collectWordFromWorker(workerPort);
            if (response != null) {
                receivedMessages.add(response);
            }
        }

        receivedMessages.sort(Comparator.comparingInt(Message::getClock));
        String reconstructedParagraph = String.join(" ", extractWordsFromMessages(receivedMessages));
        System.out.println("Reconstructed paragraph: " + reconstructedParagraph);
    }

    private static void sendWordToWorker(String word) {
        int workerPort = WORKER_PORTS[new Random().nextInt(WORKER_PORTS.length)];
        lamportClock++;
        try (Socket socket = new Socket(WORKER_ADDRESS, workerPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(new Message(word, lamportClock));
            out.flush();
            System.out.println("Sent word '" + word + "' to worker at port " + workerPort);

        } catch (IOException e) {
            System.err.println("Error sending word to worker at port " + workerPort + ": " + e.getMessage());
        }
    }

    private static Message collectWordFromWorker(int port) {
        try (Socket socket = new Socket(WORKER_ADDRESS, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send a dummy message to get the response
            out.writeObject(new Message("REQUEST", lamportClock));
            out.flush();

            Object obj = in.readObject();
            if (obj instanceof Message) {
                Message msg = (Message) obj;
                System.out.println("Received word '" + msg.getWord() + "' from worker at port " + port);
                return msg;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error collecting word from worker at port " + port + ": " + e.getMessage());
        }
        return null;
    }

    private static List<String> extractWordsFromMessages(List<Message> messages) {
        List<String> words = new ArrayList<>();
        for (Message message : messages) {
            words.add(message.getWord());
        }
        return words;
    }
}