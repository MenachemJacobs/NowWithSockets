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

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down the server...");
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Forcing executor shutdown...");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }));
    }

    public static void main(String[] args) {
        MasterSystemServer masterSystem = new MasterSystemServer();
        masterSystem.addShutdownHook();
        masterSystem.startServer();
    }

    private void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
//              TODO: For some reason, the fact that the slave system triggers execution of this code is somehow
//               preventing future execution
                Socket socket = serverSocket.accept();
                System.out.println("System accepted");

                // Read the object directly in the startServer method
                Object obj;
                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    obj = objectInputStream.readObject();
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
        executorService.submit(() -> {
            if (!task.isComplete) {
                System.out.println("Task " + task.taskID + " incomplete");
                handleIncompleteTask(task, socket);
            } else {
                System.out.println("Task " + task.taskID + " complete");
                handleCompleteTask(task);
            }
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
