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
    Queue<Task> finishedTasks = new LinkedBlockingQueue<>();
    Queue<Task> toDispatchTasks = new LinkedBlockingQueue<>();

    AtomicBoolean isDispatcherRunning = new AtomicBoolean(false);

    public static void main(String[] args) {
        try{
            MasterSystemServer masterSystem = new MasterSystemServer();

            masterSystem.startServer();
        } catch(Exception e){ System.err.println("Error: " + e.getMessage()); }
    }

    private void startServer() throws Exception {
        try(ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("System accepted");

                handleTask(socket);
            }
        }
    }

    private void handleTask(Socket socket) {
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())){
            Task task = (Task) objectInputStream.readObject();
            toDispatchTasks.add(task);
            System.out.println(task.toString());
        } catch (IOException | ClassNotFoundException e){
            System.err.println("Error reading task: " + e.getMessage());
        }
    }
}
