package SlaveSystem;

import Components.PortNumbers;
import Components.Task;
import Components.TaskType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class SlaveSystemServer {
    List<TaskProcessor> TaskProcessorPool = new ArrayList<>();
    BlockingQueue<Task> ATaskQ = new LinkedBlockingQueue<>();
    BlockingQueue<Task> BTaskQ = new LinkedBlockingQueue<>();
    private final Object AQueueLock = new Object();
    private final Object BQueueLock = new Object();

    SlaveSystemServer(){
        TaskProcessorPool.add(new TaskProcessor(ATaskQ, BTaskQ, AQueueLock, BQueueLock, new AtomicBoolean(false), "A-Slave"));
        TaskProcessorPool.add(new TaskProcessor(BTaskQ, ATaskQ, BQueueLock, AQueueLock, new AtomicBoolean(false), "B-Slave"));
    }

    public void startServer() throws IOException, ClassNotFoundException {
        int portNumber = PortNumbers.SlaveServerPort;
        try(ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("System listening on port: " + portNumber);

            while(true){
                Socket socket = serverSocket.accept();
                System.out.println("System accepted");

                handleTask(socket);
            }
        }
    }

    public void handleTask(Socket socket) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            Task task = (Task) objectInputStream.readObject();
            System.out.println("Received Components.Task of type: " + task.taskType);

            if(task.taskType == TaskType.A)
                synchronized (AQueueLock) {
                    ATaskQ.add(task);
                }
            else if(task.taskType == TaskType.B)
                synchronized (BQueueLock) {
                    BTaskQ.add(task);
                }

            else
                throw new IllegalArgumentException("New task type has been added without updating the slave server");

            for(TaskProcessor processor : TaskProcessorPool){
                if(!processor.isRunning.get()) {
                    processor.isRunning.set(true);
                    new Thread(processor).start();
                }
            }

            socket.close();
        }
    }

    public static void main(String[] args){
        try {
            // Initialize the SlaveSystem.SlaveSystem with passed arguments (task type)
            SlaveSystemServer slaveSystem = new SlaveSystemServer();

            // Start the server and listen for incoming tasks
            slaveSystem.startServer();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
