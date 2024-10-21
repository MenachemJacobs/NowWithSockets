import ValuesLabels.PortNumbers;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MasterSystem {
    static int portNumber = PortNumbers.MasterPort;
    Queue<Task> finishedTasks = new LinkedList<>();
    Queue<Task> toDispatchTasks = new LinkedList<>();

    public static void main(String[] args) {
        try{// Initialize the SlaveSystem with passed arguments (task type)
            MasterSystem masterSystem = new MasterSystem();

            // Start the server and listen for incoming tasks
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
    }
}
