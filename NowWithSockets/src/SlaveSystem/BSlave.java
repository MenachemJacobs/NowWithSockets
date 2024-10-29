package SlaveSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class BSlave {
    BlockingQueue<Task> ToDo = new LinkedBlockingQueue<>();
    BlockingQueue<Task> Done = new LinkedBlockingQueue<>();
    TaskProcessor myWorker = new TaskProcessor(TaskType.B, ToDo, Done);
    MasterNotifier masterNotifier = new MasterNotifier(Done);

    public static void main(String[] args) {
        BSlave bSlave = new BSlave();
        bSlave.StartServer();
    }

    void StartServer() {
        int portNumber = PortNumbers.BSlavePort;
        String connectionMessage = "Slave B receiving a task";
        new Thread(myWorker).start();
        new Thread(masterNotifier).start();

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(connectionMessage);
                receiveTask(socket);
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void receiveTask(Socket socket) {
        try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())) {
            Object obj = inputStream.readObject();
            if (obj instanceof Task task) ToDo.put(task);
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }
}
