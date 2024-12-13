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

    Client(int idNumber) {
        this.idNumber = idNumber;
    }

    public static void main(String[] args) {
        Client client = new Client(clientCounter.incrementAndGet()); // ID can be generated or passed as an argument
        client.listenForInput();
    }

    void listenForInput() {
        Scanner scanner = new Scanner(System.in);
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

    void dispatchTask(TaskType taskType) {
        Task task = new Task(idNumber, ++taskCounter, taskType);
        try (Socket socket = new Socket("localhost", PortNumbers.MasterClientPort);
             ObjectOutputStream outTask = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream inTask = new ObjectInputStream(socket.getInputStream())) {

            outTask.writeObject(task);
            outTask.flush(); // Ensure the task is sent immediately
            System.out.println("Sent task of type: " + task.taskType + " from Client ID: " + idNumber + ", task number: " + task.taskID);

            // Wait for the server to send a response indicating task completion
            Object response = inTask.readObject();
            if (response instanceof Task completedTask) {
                System.out.println("Task completed: " + completedTask.taskID + " of type: " + completedTask.taskType);
            } else {
                System.out.println("Received unknown response from server.");
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to send task of type: " + taskType + " - " + e.getMessage());
        }
    }
}