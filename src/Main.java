import java.net.*;
import java.util.*;
import java.io.*;

public class Main {
    private static final String WORKER_ADDRESS = "localhost";
    private static final int[] WORKER_PORTS = {5001, 5002, 5003, 5004, 5005};
    private static int lamportClock = 0;
    private static Map<Integer, List<String>> workerWords = new HashMap<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Initialize the worker words map
        for (int port : WORKER_PORTS) {
            workerWords.put(port, new ArrayList<>());
        }

        System.out.print("Enter a paragraph: ");
        String paragraph = scanner.nextLine();
        if (paragraph.trim().isEmpty()) {
            System.err.println("Paragraph cannot be empty. Please restart and enter valid input.");
            return;
        }
        String[] words = paragraph.split("\\s+");

        // Send words to workers
        for (String word : words) {
            int workerPort = WORKER_PORTS[new Random().nextInt(WORKER_PORTS.length)];
            workerWords.get(workerPort).add(word);
            sendWordToWorker(word, workerPort);
        }

        System.out.println("Waiting for responses...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.err.println("Error while waiting: " + e.getMessage());
        }

        List<Message> receivedMessages = new ArrayList<>();
        // Collect messages from each worker
        for (int workerPort : WORKER_PORTS) {
            List<Message> workerMessages = collectWordsFromWorker(workerPort);
            receivedMessages.addAll(workerMessages);
        }

        // Sort by Lamport clock and reconstruct
        receivedMessages.sort(Comparator.comparingInt(Message::getClock));
        String reconstructedParagraph = String.join(" ", extractWordsFromMessages(receivedMessages));
        System.out.println("Reconstructed paragraph: " + reconstructedParagraph);
    }

    private static void sendWordToWorker(String word, int workerPort) {
        lamportClock++;
        try (Socket socket = new Socket(WORKER_ADDRESS, workerPort);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            out.writeObject(new Message(word, lamportClock));
            out.flush();
            System.out.println("Sent word '" + word + "' to worker at port " + workerPort);

        } catch (IOException e) {
            System.err.println("Error sending word to worker at port " + workerPort + ": " + e.getMessage());
        }
    }

    private static List<Message> collectWordsFromWorker(int port) {
        List<Message> messages = new ArrayList<>();
        try (Socket socket = new Socket(WORKER_ADDRESS, port);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            // Send collection request
            out.writeObject(new Message("COLLECT", 0));
            out.flush();

            // Read all messages from this worker
            while (true) {
                Object obj = in.readObject();
                if (obj == null || !(obj instanceof Message)) {
                    break;
                }
                Message msg = (Message) obj;
                if (msg.getWord().equals("END")) {
                    break;
                }
                messages.add(msg);
                System.out.println("Received word '" + msg.getWord() + "' from worker at port " + port);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error collecting words from worker at port " + port + ": " + e.getMessage());
        }
        return messages;
    }

    private static List<String> extractWordsFromMessages(List<Message> messages) {
        return messages.stream()
                .map(Message::getWord)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}