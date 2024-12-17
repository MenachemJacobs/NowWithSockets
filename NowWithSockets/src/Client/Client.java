package Client;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Client class represents a client in a distributed task processing system.
 * It connects to a master system to submit tasks of specified types and listens
 * for responses from the master system in a separate thread.
 *
 * <p>
 * Each client instance has a unique identifier and can submit multiple tasks.
 * Tasks are dispatched to the master system, which assigns them to appropriate
 * slave servers for processing.
 * </p>
 *
 * <p>
 * Key features of this class include:
 * - Establishing a connection to the master system.
 * - Dispatching tasks to the master system.
 * - Listening for responses from the master system.
 * - Handling user input for task submission.
 * - Cleaning up resources on shutdown.
 * </p>
 *
 * <p>
 * This class serves as an entry point for users to interact with the distributed system.
 * It can run independently and communicate with the master system using sockets.
 * </p>
 */
public class Client {
    /**
     * A flag to indicate whether the client is running.
     * Used to gracefully terminate the client during shutdown.
     */
    private volatile static Boolean isRunning = true;

    /**
     * An atomic counter to assign unique IDs to each client.
     * Ensures thread-safe incrementing.
     */
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    /**
     * A counter to track the number of tasks submitted by this client.
     */
    private int taskCounter = 0;

    /**
     * The socket used to communicate with the master system.
     */
    private Socket masterSystemSocket;

    /**
     * An output stream to send serialized Task objects to the master system.
     */
    private ObjectOutputStream outTask;

    /**
     * The main method serves as the entry point for the client application.
     * It initializes a new Client instance, establishes a connection to the
     * master system, starts a listener for responses, registers a shutdown
     * hook, and processes user input for task submission.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(String[] args) {
        Client client = new Client();
        client.establishConnection();
        client.startMasterListener();
        client.addShutdownHook();
        client.listenForInput(clientCounter.incrementAndGet());
    }

    /**
     * Establishes a connection to the master system using a socket.
     * Initializes the output stream for sending tasks to the master system.
     * If the connection fails, the program exits with an error message.
     */
    void establishConnection() {
        try {
            masterSystemSocket = new Socket("localhost", PortNumbers.MasterClientPort);
            outTask = new ObjectOutputStream(masterSystemSocket.getOutputStream());
            System.out.println("Connected to Master System.");
        } catch (IOException e) {
            System.err.println("Failed to connect to Master System: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Starts a separate thread to listen for responses from the master system.
     * The listener runs in the background and processes incoming messages.
     */
    void startMasterListener() {
        MasterListener masterListener = new MasterListener(masterSystemSocket, isRunning);
        new Thread(masterListener).start();
    }

    /**
     * Continuously listens for user input to submit tasks of specified types.
     * The user can submit TaskType.A or TaskType.B tasks by pressing the corresponding keys.
     * Provides feedback for invalid input.
     */
    void listenForInput(int myIdNumber) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Press 'a' for TaskType.A, 'b' for TaskType.B, or any other key for help.");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("a")) {
                dispatchTask(myIdNumber, TaskType.A);
            } else if (input.equalsIgnoreCase("b")) {
                dispatchTask(myIdNumber, TaskType.B);
            } else {
                System.out.println("Invalid input. Please press 'a' for TaskType.A or 'b' for TaskType.B.");
                scanner.nextLine();
            }
        }
    }

    /**
     * Dispatches a task of the specified type to the master system.
     * Creates a new Task object with a unique task ID and sends it to the master system.
     *
     * @param taskType the type of task to be dispatched (TaskType.A or TaskType.B).
     */
    void dispatchTask(int myIdNumber, TaskType taskType) {
        if (masterSystemSocket == null || masterSystemSocket.isClosed()) {
            System.err.println("Socket is closed or not connected.");
            return;
        }

        Task task = new Task(++taskCounter, myIdNumber, taskType);

        try {
            outTask.writeObject(task);
            outTask.flush(); // Ensure the task is sent immediately
            System.out.println("Sent task of type: " + task.taskType + " from Client.Client ID: " + myIdNumber + ", task number: " + task.taskID);
        } catch (IOException e) {
            System.err.println("Failed to send task of type: " + taskType + " - " + e.getMessage());
        }
    }

    /**
     * Registers a shutdown hook to clean up resources during system shutdown.
     * Ensures the socket connection to the master system is properly closed.
     */
    void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown hook triggered. Cleaning up resources...");
            isRunning = false;
            try {
                if (masterSystemSocket != null && !masterSystemSocket.isClosed()) {
                    masterSystemSocket.close();
                    System.out.println("Closed master system socket.");
                }
            } catch (IOException e) {
                System.err.println("Error closing master system socket: " + e.getMessage());
            }
        }));
    }
}