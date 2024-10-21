import ValuesLabels.PortNumbers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {
    int idNumber;
    Task[] tasks;

    Client(int idNumber, Task[] tasks){
        this.idNumber = idNumber;
        this.tasks = tasks;

        try{execute_tasks();}
        catch (IOException e){System.err.println("Error executing tasks: " + e.getMessage());}
    }

    void execute_tasks() throws IOException {
        for (Task task : tasks) {
            try (Socket socket = new Socket("localhost", PortNumbers.MasterPort);
                 ObjectOutputStream outTask = new ObjectOutputStream(socket.getOutputStream())) {

                outTask.writeObject(task);
                System.out.println("Sent task of type: " + task.taskType + " from Client ID: " + idNumber);

            } catch (IOException e) {
                System.err.println("Failed to send task of type: " + task.taskType + " - " + e.getMessage());
            }
        }
    }
}
