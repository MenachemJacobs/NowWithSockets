import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    int idNumber;
    Task[] tasks;
    static int size = 5;

    Client(int idNumber, Task[] tasks) {
        this.idNumber = idNumber;
        this.tasks = tasks;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            char type = args[0].charAt(0);
            if (type == 'A' || type == 'B') {
                TaskType myType = type == 'A' ? TaskType.A : TaskType.B;
                Task[] tasks = new Task[size];

                for (int i = 0; i < size; i++) { tasks[i] = new Task(1,1, myType); }

                Client client = new Client(1, tasks); // ID can be generated or passed as another argument
                client.execute_tasks();
            } else {
                System.err.println("Invalid type. Please use 'A' or 'B'.");
            }
        }
    }

    void execute_tasks() {
        for (Task task : tasks) {
            try (Socket socket = new Socket("localhost", PortNumbers.MasterClientPort);
                 ObjectOutputStream outTask = new ObjectOutputStream(socket.getOutputStream())) {
                outTask.writeObject(task);
                outTask.flush(); // Ensure the task is sent immediately
                System.out.println("Sent task of type: " + task.taskType + " from Client ID: " + idNumber + ", task number: " + task.taskID);
            } catch (IOException e) {
                System.err.println("Failed to send task of type: " + idNumber + " - " + e.getMessage());
            }
        }
    }
}
