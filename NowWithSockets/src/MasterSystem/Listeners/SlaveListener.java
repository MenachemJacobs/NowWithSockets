package MasterSystem.Listeners;

import Components.PortNumbers;
import Components.Task;
import MasterSystem.ClientsNotifier;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SlaveListener implements Runnable {
    String connectionMessage = "Connection made with client";

    BlockingQueue<Task> completedTasks = new LinkedBlockingQueue<>();
    Map<Task, Socket> clientMap;

    ClientsNotifier Replier;

    public SlaveListener(Map<Task, Socket> clientMap){
        this.clientMap = clientMap;

        Replier = new ClientsNotifier(completedTasks, clientMap);
        new Thread(Replier).start();
    }

    public void run() {
        int portNumber = PortNumbers.MasterSlavePort;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println(connectionMessage);

                try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
                    HandleCommunication(objectInputStream.readObject());
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error reading task: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading task: " + e.getMessage());
        }
    }

    void HandleCommunication(Object object) {
        if (object instanceof Task) {
            completedTasks.add((Task) object);
        }
    }
}
