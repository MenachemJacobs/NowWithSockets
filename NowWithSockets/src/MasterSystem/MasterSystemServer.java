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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MasterSystemServer {
    static int portNumber = PortNumbers.MasterServerPort;

    Map<Task, Socket> clientMap;

    ClientsNotifier Replier;
    Queue<Task> finishedTasks = new LinkedBlockingQueue<>();
    AtomicBoolean isReplierRunning = new AtomicBoolean(false);

    TasksToSlavesBroadcaster Dispatcher;
    Queue<TaskSocketPair> tasksToDispatch = new LinkedBlockingQueue<>();
    AtomicBoolean isDispatcherRunning = new AtomicBoolean(false);

    MasterSystemServer() {
        clientMap = new ConcurrentHashMap<>();

        Object clientLock = new Object();
        Replier = new ClientsNotifier(finishedTasks, isReplierRunning, clientMap);
        Dispatcher = new TasksToSlavesBroadcaster(tasksToDispatch, isDispatcherRunning, clientMap);
    }

    public static void main(String[] args) {
        MasterSystemServer masterSystem = new MasterSystemServer();
        masterSystem.startServer();
    }

    private void startServer() {
        try(ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("System accepted");

                handleTask(socket);
            }
        } catch (IOException e) { System.err.println("Error reading task: " + e.getMessage()); }
    }

    private void handleTask(Socket socket) {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())){
            Task task = (Task) objectInputStream.readObject();

            if(!task.isComplete) {
                handleIncompleteTask(task, socket);
            }
            else {
                handleCompleteTask(task);
            }

            System.out.println(task);
        } catch (IOException | ClassNotFoundException e){
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void handleIncompleteTask(Task task, Socket socket){
        TaskSocketPair toDispatch = new TaskSocketPair(task, socket);
        tasksToDispatch.add(toDispatch);

        if (!isDispatcherRunning.get()) {
            isDispatcherRunning.set(true);
            new Thread(Dispatcher).start();
        }

        if(!isReplierRunning.get())
            new Thread(Replier).start();
    }

    void handleCompleteTask(Task task){
        finishedTasks.add(task);

        if (!isReplierRunning.get()) {
            isReplierRunning.set(true);
            new Thread(Replier).start();
        }

        if(!isDispatcherRunning.get())
            new Thread(Dispatcher).start();
    }
}