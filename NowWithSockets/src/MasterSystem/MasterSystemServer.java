package MasterSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskSocketPair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class MasterSystemServer {
    static int portNumber = PortNumbers.MasterServerPort;
    Map<Task, Socket> clientMap = new ConcurrentHashMap<>();

    Thread ReplyThread;
    Runnable Replier;
    Queue<Task> finishedTasks = new ConcurrentLinkedQueue<>();

    Thread  DispatchThread;
    Runnable Dispatcher;
    Queue<TaskSocketPair> tasksToDispatch = new ConcurrentLinkedQueue<>();

    MasterSystemServer() {
        Replier = new ClientsNotifier(finishedTasks, clientMap);
        Dispatcher = new TasksToSlavesBroadcaster(tasksToDispatch, clientMap);
    }

    public static void main(String[] args) {
        MasterSystemServer masterSystem = new MasterSystemServer();
        masterSystem.startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("System accepted");

                handleTask(socket);
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void handleTask(Socket socket) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            Task task = (Task) objectInputStream.readObject();

            if (!task.isComplete) {
                handleIncompleteTask(task, socket);
            } else {
                handleCompleteTask(task);
            }

            System.out.println(task);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void handleIncompleteTask(Task task, Socket socket) {
        tasksToDispatch.add(new TaskSocketPair(task, socket));

        if (!DispatchThread.isAlive()) {
            DispatchThread = new Thread(Dispatcher);
            DispatchThread.start();
            System.out.println("Woke up the system Dispatcher");
        }
    }

    void handleCompleteTask(Task task) {
        finishedTasks.add(task);

        if (!ReplyThread.isAlive()) {
            ReplyThread = new Thread(Replier);
            ReplyThread.start();
            System.out.println("Woke up the system replier");
        }
    }
}
