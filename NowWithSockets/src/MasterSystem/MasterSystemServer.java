package MasterSystem;

import Components.PortNumbers;
import Components.Task;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MasterSystemServer {
    static int portNumber = PortNumbers.MasterServerPort;

    ClientsNotifier Replier;
    Queue<Task> finishedTasks = new LinkedBlockingQueue<>();
    private final Object replyLock = new Object();
    AtomicBoolean isReplierRunning = new AtomicBoolean(false);

    TasksToSlavesBroadcaster Dispatcher;
    Queue<Task> tasksToDispatch = new LinkedBlockingQueue<>();
    private final Object dispatchLock = new Object();
    AtomicBoolean isDispatcherRunning = new AtomicBoolean(false);

    MasterSystemServer() {
        Replier = new ClientsNotifier(finishedTasks, replyLock, isReplierRunning);
        Dispatcher = new TasksToSlavesBroadcaster(tasksToDispatch, dispatchLock, isDispatcherRunning);
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
                synchronized (dispatchLock) {
                    tasksToDispatch.add(task);
                }

                if (!isDispatcherRunning.get()) {
                    isDispatcherRunning.set(true);
                    new Thread(Dispatcher).start();
                }
            }
            else {
                synchronized (replyLock) {
                    finishedTasks.add(task);
                }

                if (!isReplierRunning.get()) {
                    isReplierRunning.set(true);
                    new Thread(Replier).start();
                }
            }

            System.out.println(task.toString());
        } catch (IOException | ClassNotFoundException e){
            System.err.println("Error reading task: " + e.getMessage());
        }
    }
}
