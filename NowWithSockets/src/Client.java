import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    int idNumber;
    Task[] tasks;

    Client(int idNumber, Task[] tasks) {
        this.idNumber = idNumber;
        this.tasks = tasks;
    }

    void execute_tasks() {


        for (Task task : tasks) {
            try (Socket socket = new Socket("localhost", PortNumbers.MasterServerPort);
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
