package Components;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class SlaveSocketManager {
    private static final ConcurrentHashMap<Integer, Socket> slaveSockets = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Semaphore> socketSemaphores = new ConcurrentHashMap<>();

    public static void addSlaveSocket(int port) {
        while (true) {
            try {
                Socket socket = new Socket("localhost", port);
                slaveSockets.put(port, socket);
                socketSemaphores.put(port, new Semaphore(1));
                System.out.println("Connected to slave on port: " + port);
                break; // Exit the loop once the connection is successful
            } catch (IOException e) {
                System.err.println("Error connecting to slave on port: " + port + " - " + e.getMessage());
                try {
                    Thread.sleep(1000); // Sleep for a short period before retrying
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    System.err.println("Thread interrupted while waiting to retry connection.");
                }
            }
        }

    }

    public static Socket getSlaveSocket(int port) throws InterruptedException {
        Semaphore semaphore = socketSemaphores.get(port);
        if (semaphore != null) {
            while (true) {
                if (semaphore.tryAcquire()) {
                    return slaveSockets.get(port);
                }
                Thread.sleep(100); // Sleep for a short period before retrying
            }
        }
        return null;
    }

    public static void releaseSlaveSocket(int port) {
        Semaphore semaphore = socketSemaphores.get(port);
        if (semaphore != null) {
            semaphore.release();
        }
    }
}