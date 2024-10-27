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
    ExecutorService executorService = Executors.newFixedThreadPool(2);

    // Futures to track the running tasks
    Future<?> replyTaskFuture = null;
    Future<?> dispatchTaskFuture = null;

    ClientsNotifier Replier;
    Queue<Task> finishedTasks = new ConcurrentLinkedQueue<>();

    TasksToSlavesBroadcaster Dispatcher;
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

                // Read the object directly in the startServer method
                Object obj;
                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    obj = objectInputStream.readObject();
                    System.out.println("Object received");
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading task: " + e.getMessage());
                    continue; // Go to the next iteration if there is an error
                }

                // Check if the object is an instance of Task
                if (obj instanceof Task task) { // Using instanceof with pattern matching (Java 16+)
                    handleTask(socket, task);
                } else if (obj != null) {
                    // Log if obj is not a Task
                    System.out.println("Received an object that is not a Task. Ignoring it.");
                } else {
                    System.out.println("No object received, nothing to process.");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void handleTask(Socket socket, Task task) {
        System.out.println("Task Identified: " + task);

        executorService.submit(() -> {
            if (!task.isComplete) {
                System.out.println("Task incomplete");
                handleIncompleteTask(task, socket);
            } else {
                System.out.println("Task complete");
                handleCompleteTask(task);
            }

            System.out.println(task);
        });
    }

    void handleIncompleteTask(Task task, Socket socket) {
        tasksToDispatch.add(new TaskSocketPair(task, socket));
        if (dispatchTaskFuture == null || dispatchTaskFuture.isDone()) {
            System.out.println("Dispatching task: " + task);
            dispatchTaskFuture = executorService.submit(Dispatcher);
        }
    }

    void handleCompleteTask(Task task) {
        finishedTasks.add(task);
        if (replyTaskFuture == null || replyTaskFuture.isDone()) {
            System.out.println("Replying task: " + task);
            replyTaskFuture = executorService.submit(Replier);
        }
    }
}
