package MasterSystem.Listeners;

import Components.TaskSocketPair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;

abstract public class ListenerServer implements Runnable {
    int portNumber;
    String connectionMessage;

    ListenerServer(int portNumber, String connectionMessage){
        this.portNumber = portNumber;
        this.connectionMessage = connectionMessage;
    }

    public void run() {
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

    abstract void HandleCommunication(Object o);
}
