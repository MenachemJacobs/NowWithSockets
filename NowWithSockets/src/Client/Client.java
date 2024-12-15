package Client;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    int idNumber;
    private int taskCounter = 0;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    private Socket masterSystemSocket;
    private ObjectInputStream inTask;

    Client(int idNumber) {
        this.idNumber = idNumber;
    }

    public static void main(String[] args) {
        Client client = new Client(clientCounter.incrementAndGet());
        client.establishConnection();
        client.startMasterListener();
        client.listenForInput();
    }

    void establishConnection() {
        try {
            masterSystemSocket = new Socket("localhost", PortNumbers.MasterClientPort);
            System.out.println("Connected to Master System.");
        } catch (IOException e) {
            System.err.println("Failed to connect to Master System: " + e.getMessage());
            System.exit(1);
        }
    }

    void startMasterListener() {
        MasterListener masterListener = new MasterListener(masterSystemSocket);
        Thread listenerThread = new Thread(masterListener);
        listenerThread.start();
    }

    void listenForInput() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Press 'a' for TaskType.A, 'b' for TaskType.B, or any other key for help.");

            while (true) {
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("a")) {
                    dispatchTask(TaskType.A);
                } else if (input.equalsIgnoreCase("b")) {
                    dispatchTask(TaskType.B);
                } else {
                    System.out.println("Invalid input. Please press 'a' for TaskType.A or 'b' for TaskType.B.");
                }
            }
        }
    }

    void dispatchTask(TaskType taskType) {
        if (masterSystemSocket == null || masterSystemSocket.isClosed()) {
            try {
                masterSystemSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close the connection: " + e.getMessage());
            }
        }

        Task task = new Task(idNumber, ++taskCounter, taskType);

        try (ObjectOutputStream outTask = new ObjectOutputStream(masterSystemSocket.getOutputStream())) {
            outTask.writeObject(task);
            outTask.flush(); // Ensure the task is sent immediately
            System.out.println("Sent task of type: " + task.taskType + " from Client.Client ID: " + idNumber + ", task number: " + task.taskID);
        } catch (IOException e) {
            System.err.println("Failed to send task of type: " + taskType + " - " + e.getMessage());
        }
    }
}