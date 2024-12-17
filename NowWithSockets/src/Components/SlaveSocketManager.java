package Components;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SlaveSocketManager {
    private static final ConcurrentHashMap<Integer, Socket> slaveSockets = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, Semaphore> socketSemaphores = new ConcurrentHashMap<>();

    // Configuration constants
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long SEMAPHORE_TIMEOUT_MS = 5000;

    public static void addSlaveSocket(int port) {
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                // Close existing socket if it exists
                closeExistingSocket(port);

                // Create new socket
                Socket socket = new Socket("localhost", port);
                slaveSockets.put(port, socket);
                socketSemaphores.put(port, new Semaphore(1));

                System.out.println("Connected to slave on port: " + port);
                return; // Successfully connected
            } catch (IOException e) {
                System.out.println("Attempt " + attempt + " failed to connect to slave on port: " + port +
                        " - " + e.getMessage());

                if (attempt == MAX_RETRY_ATTEMPTS) {
                    System.out.println("Failed to connect to slave on port " + port + " after " +
                            MAX_RETRY_ATTEMPTS + " attempts");
                }
            }
        }
    }

    public static Socket getSlaveSocket(int port) {
        Semaphore semaphore = socketSemaphores.get(port);
        if (semaphore == null) {
            System.out.println("No semaphore found for port: " + port);
            return null;
        }

        try {
            // Attempt to acquire semaphore with timeout
            if (semaphore.tryAcquire(SEMAPHORE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                Socket socket = slaveSockets.get(port);

                // Check if socket is still valid
                if (isSocketValid(socket)) {
                    return socket;
                } else {
                    // Attempt to reconnect if socket is invalid
                    System.out.println("Socket for port " + port + " is invalid. Attempting to reconnect.");
                    semaphore.release();
                    addSlaveSocket(port);

                    // Retry acquiring socket after reconnection
                    if (semaphore.tryAcquire(SEMAPHORE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                        return slaveSockets.get(port);
                    }
                }
            } else {
                System.out.println("Failed to acquire semaphore for port: " + port + " within timeout");
            }
        } catch (Exception e) {
            System.out.println("Error acquiring socket for port " + port + ": " + e.getMessage());
        }

        return null;
    }

    public static void releaseSlaveSocket(int port) {
        Semaphore semaphore = socketSemaphores.get(port);
        if (semaphore != null) {
            semaphore.release();
        } else {
            System.out.println("No semaphore found to release for port: " + port);
        }
    }

    private static void closeExistingSocket(int port) throws IOException {
        Socket existingSocket = slaveSockets.get(port);
        if (existingSocket != null && !existingSocket.isClosed()) {
            existingSocket.close();
        }
    }

    private static boolean isSocketValid(Socket socket) {
        return socket != null &&
                !socket.isClosed() &&
                socket.isConnected() &&
                !socket.isInputShutdown() &&
                !socket.isOutputShutdown();
    }
}