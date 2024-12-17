package Client;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
    int idNumber;
    private int taskCounter = 0;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    private Socket masterSystemSocket;
    private ObjectOutputStream outTask;

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
            outTask = new ObjectOutputStream(masterSystemSocket.getOutputStream());
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
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Press 'a' for TaskType.A, 'b' for TaskType.B, or any other key for help.");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("a")) {
                dispatchTask(TaskType.A);
            } else if (input.equalsIgnoreCase("b")) {
                dispatchTask(TaskType.B);
            } else {
                System.out.println("Invalid input. Please press 'a' for TaskType.A or 'b' for TaskType.B.");
                scanner.nextLine();
            }
        }
    }

    void dispatchTask(TaskType taskType) {
        if (masterSystemSocket == null || masterSystemSocket.isClosed()) {
            System.err.println("Socket is closed or not connected.");
            return;
        }

        Task task = new Task(idNumber, ++taskCounter, taskType);

        try {
            outTask.writeObject(task);
            outTask.flush(); // Ensure the task is sent immediately
            System.out.println("Sent task of type: " + task.taskType + " from Client.Client ID: " + idNumber + ", task number: " + task.taskID);
        } catch (IOException e) {
            System.err.println("Failed to send task of type: " + taskType + " - " + e.getMessage());
        }
    }
}