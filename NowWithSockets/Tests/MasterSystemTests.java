//import Components.PortNumbers;
//import Components.Task;
//import Components.TaskSocketPair;
//import Components.TaskType;
//import MasterSystem.MasterSystemServer;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.IOException;
//import java.io.ObjectOutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//public class MasterSystemTests {
//    private MasterSystemServer masterSystemServer;
//    private ServerSocket serverSocket;
//    private Socket testSocket;
//
//    @BeforeEach
//    void setUp() throws IOException {
//        serverSocket = new ServerSocket(PortNumbers.MasterServerPort);
//        testSocket = new Socket("localhost", PortNumbers.MasterServerPort);
//    }
//
//    @AfterEach
//    void tearDown() throws IOException {
//        testSocket.close();
//        serverSocket.close();
//    }
//
//    @Test
//    void testHandleIncompleteTaskStartsDispatcher() throws Exception {
//        // Arrange
//        Task incompleteTask = new Task(1, 1, TaskType.A);
//        incompleteTask.isComplete = false;
//
//        // Sending the incomplete task through the socket
//        ObjectOutputStream outStream = new ObjectOutputStream(testSocket.getOutputStream());
//        outStream.writeObject(incompleteTask);
//        outStream.flush();
//
//        // Act
//        // Simulating the server accepting a connection and handling the task
//        Socket acceptedSocket = serverSocket.accept();
//        masterSystemServer.handleTask(acceptedSocket);
//
//        // Assert
//        assertTrue(masterSystemServer.isDispatcherRunning.get(), "Dispatcher should be running");
//        assertEquals(1, masterSystemServer.tasksToDispatch.size(), "There should be one task in the dispatch queue");
//    }
//
//    @Test
//    void testHandleCompleteTaskStartsReplier() throws Exception {
//        // Arrange
//        Task completeTask = new Task(1, 1, TaskType.A);
//        completeTask.isComplete = true;
//
//        // Sending the complete task through the socket
//        ObjectOutputStream outStream = new ObjectOutputStream(testSocket.getOutputStream());
//        outStream.writeObject(completeTask);
//        outStream.flush();
//
//        // Act
//        // Simulating the server accepting a connection and handling the task
//        Socket acceptedSocket = serverSocket.accept();
//        masterSystemServer.handleTask(acceptedSocket);
//
//        // Assert
//        assertTrue(masterSystemServer.isReplierRunning.get(), "Replier should be running");
//        assertEquals(1, masterSystemServer.finishedTasks.size(), "There should be one task in the finished tasks queue");
//    }
//
//    @Test
//    void testHandleTaskHandlesIOException() throws Exception {
//        // Arrange
//        // Close the socket to simulate an IOException when reading
//        testSocket.close();
//
//        // Act
//        // Attempting to handle a task with a closed socket
//        assertDoesNotThrow(() -> masterSystemServer.handleTask(testSocket));
//    }
//
//    @Test
//    void testHandleTaskHandlesClassNotFoundException() throws Exception {
//        // Arrange
//        // Create a socket and send a non-serializable object
//        Socket anotherSocket = new Socket("localhost", PortNumbers.MasterServerPort);
//        ObjectOutputStream outStream = new ObjectOutputStream(anotherSocket.getOutputStream());
//        outStream.writeObject(new Object()); // Writing a non-serializable object
//        outStream.flush();
//
//        // Act & Assert
//        assertDoesNotThrow(() -> {
//            Socket acceptedSocket = serverSocket.accept();
//            masterSystemServer.handleTask(acceptedSocket);
//        });
//
//        anotherSocket.close();
//    }
//
//    @Test
//    void testHandleIncompleteTaskAddsToDispatchQueue() throws Exception {
//        // Arrange
//        Task incompleteTask = new Task(1, 1, TaskType.A);
//        incompleteTask.isComplete = false;
//
//        // Sending the incomplete task through the socket
//        ObjectOutputStream outStream = new ObjectOutputStream(testSocket.getOutputStream());
//        outStream.writeObject(incompleteTask);
//        outStream.flush();
//
//        // Act
//        // Simulating the server accepting a connection and handling the task
//        Socket acceptedSocket = serverSocket.accept();
//        masterSystemServer.handleTask(acceptedSocket);
//
//        // Assert
//        assertEquals(1, masterSystemServer.tasksToDispatch.size(), "The dispatch queue should contain the incomplete task");
//        TaskSocketPair taskSocketPair = masterSystemServer.tasksToDispatch.poll();
//        assertNotNull(taskSocketPair, "TaskSocketPair should not be null");
//        assertEquals(incompleteTask, taskSocketPair.task(), "Task in TaskSocketPair should match the incomplete task");
//        assertEquals(acceptedSocket, taskSocketPair.socket(), "Socket in TaskSocketPair should match the accepted socket");
//    }
//
//    @Test
//    void testHandleCompleteTaskAddsToFinishedTasksQueue() throws Exception {
//        // Arrange
//        Task completeTask = new Task(1, 1, TaskType.A);
//        completeTask.isComplete = true;
//
//        // Sending the complete task through the socket
//        ObjectOutputStream outStream = new ObjectOutputStream(testSocket.getOutputStream());
//        outStream.writeObject(completeTask);
//        outStream.flush();
//
//        // Act
//        // Simulating the server accepting a connection and handling the task
//        Socket acceptedSocket = serverSocket.accept();
//        masterSystemServer.handleTask(acceptedSocket);
//
//        // Assert
//        assertEquals(1, masterSystemServer.finishedTasks.size(), "The finished tasks queue should contain the complete task");
//        Task task = masterSystemServer.finishedTasks.poll();
//        assertNotNull(task, "Task should not be null");
//        assertEquals(completeTask, task, "Task in the finished tasks queue should match the complete task");
//    }
//}
