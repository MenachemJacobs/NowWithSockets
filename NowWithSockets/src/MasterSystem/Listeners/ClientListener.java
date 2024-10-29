package MasterSystem.Listeners;

import Components.PortNumbers;
import Components.Task;
import MasterSystem.TasksToSlavesBroadcaster;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientListener implements Runnable {
    String connectionMessage = "Connection made with client";

    BlockingQueue<Task> uncompletedTasks = new LinkedBlockingQueue<>();
    Map<Task, Socket> clientMap;
    TasksToSlavesBroadcaster dispatcher;

    public ClientListener(Map<Task, Socket> clientMap){
        this.clientMap = clientMap;

        dispatcher = new TasksToSlavesBroadcaster(uncompletedTasks);
        new Thread(dispatcher).start();
    }

    public void run() {
        int portNumber = PortNumbers.MasterClientPort;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(connectionMessage);

                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    HandleCommunication(socket, objectInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading task: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void HandleCommunication(Socket socket, Object object) {
        if (object instanceof Task task) {
            clientMap.put(task, socket);
            uncompletedTasks.add(task);
        }
    }
}

