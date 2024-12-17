package Client;

import Components.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

/**
 * The MasterListener class listens for incoming messages from the master system,
 * specifically for completed tasks. It runs in a separate thread, continuously
 * reading the task responses from the master system and printing them to the console.
 *
 * <p>
 * The class uses a socket to receive task completion responses from the master system.
 * Upon receiving a completed task, it processes the task and outputs the result.
 * </p>
 *
 * <p>
 * The listener operates in the background, reading objects sent by the master system.
 * If the object is of type {@link Components.Task}, the task is printed to the console.
 * Otherwise, a message indicating an unknown response is shown.
 * </p>
 *
 * <p>
 * The listener also ensures that resources are cleaned up and the socket is closed
 * properly when the listener stops or when an error occurs.
 * </p>
 */
public class MasterListener implements Runnable {

    /**
     * A flag indicating whether the listener is running.
     * This flag controls the loop in the listener's run method.
     */
    private volatile static Boolean isRunning = null;

    /**
     * The socket used to receive responses from the master system.
     */
    private final Socket masterSystemSocket;

    /**
     * Constructs a new MasterListener instance.
     * Initializes the socket connection and the running flag.
     *
     * @param masterSystemSocket the socket to be used for receiving responses from the master system.
     * @param isRunning          a flag indicating whether the listener should continue running.
     */
    public MasterListener(Socket masterSystemSocket, Boolean isRunning) {
        this.masterSystemSocket = masterSystemSocket;
        if(MasterListener.isRunning == null) MasterListener.isRunning = isRunning;

        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
        else System.out.println("socket is open in the MasterListener constructor");
    }

    /**
     * The main execution method for the MasterListener. This method runs in a separate thread
     * and continuously listens for incoming task completion responses from the master system.
     * It reads the responses and prints information about the completed tasks.
     *
     * <p>
     * If the received object is a {@link Components.Task}, it will print information about
     * the completed task. If the object is not a task, it will print an error message.
     * </p>
     */
    @Override
    public void run() {
        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
        else System.out.println("socket is open in the MasterListener run method");

        ObjectInputStream inTask;
        try {
            inTask = new ObjectInputStream(masterSystemSocket.getInputStream());

            while (isRunning) {
                Object response = inTask.readObject();
                if (response instanceof Task completedTask)
                    System.out.println("Task completed: " + completedTask.taskID + " of type: " + completedTask.taskType);
                else System.out.println("Received unknown response from server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error in client reading response: " + e.getMessage());
        }

        if (masterSystemSocket.isClosed()) System.err.println("Master System has closed the connection.");
        teardown();
    }

    /**
     * Cleans up resources by closing the socket connection with the master system.
     * This method is called when the listener is stopped or an error occurs.
     */
    private void teardown() {
        try {
            if (masterSystemSocket != null && !masterSystemSocket.isClosed()) {
                masterSystemSocket.close();
                System.out.println("Closed master system socket in teardown.");
            }
        } catch (IOException e) {
            System.err.println("Error closing master system socket in teardown: " + e.getMessage());
        }
    }
}