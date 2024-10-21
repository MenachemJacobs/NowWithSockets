import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class SlaveSystem {
    TaskType systemType;
    int PortNumber;

    public SlaveSystem (String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("Must Pass A Valid Task Type (Either 'A' or 'B') to the created System");
        }

        String passedType = args[0];

        switch (passedType.toCharArray()[0]) {
            case 'A':
                systemType = TaskType.A;
                PortNumber = PortNumbers.ASlavePort;
                break;
            case 'B':
                systemType = TaskType.B;
                PortNumber = PortNumbers.BSlavePort;
                break;
            default: throw new Exception("Must Pass A Valid Task Type (Either 'A' or 'B') to the created System");
        }
    }

    public void execute(Task task){
        if (task.taskType == this.systemType) {
            task.efficientExecute();
        }
        else task.inefficientExecute();
    }

    public void startServer() throws IOException, ClassNotFoundException {
        try(ServerSocket serverSocket = new ServerSocket(PortNumber)) {
            System.out.println("System listening on port: " + PortNumber);

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
            System.out.println("Received Task of type: " + task.taskType);
            execute(task);
            socket.close();
        }
    }

    public static void main(String[] args){
        try {
            // Initialize the SlaveSystem with passed arguments (task type)
            SlaveSystem slaveSystem = new SlaveSystem(args);

            // Start the server and listen for incoming tasks
            slaveSystem.startServer();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}


