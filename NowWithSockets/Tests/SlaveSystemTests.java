import Components.PortNumbers;
import Components.Task;
import Components.TaskType;
import SlaveSystem.SlaveSystemServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class SlaveSystemTests {

    private SlaveSystemServer slaveSystemServer;

    @BeforeEach
    public void setUp() {
        slaveSystemServer = new SlaveSystemServer();
    }

    @Test
    public void testHandleTask_AddsATaskToQueue() throws Exception {
        // Create a test task of type A
        Task testTask = new Task(1, 1, TaskType.A);

        // Create a socket with an ObjectOutputStream
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(testTask);
            objectOutputStream.flush();

            // Simulate reading from the socket
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());

            // Create a socket that provides the InputStream
            Socket testSocket = new Socket("localhost", PortNumbers.SlaveServerPort) {
                @Override
                public java.io.InputStream getInputStream() {
                    return byteArrayInputStream;
                }
            };

            // Manually invoke the handleTask method
            slaveSystemServer.handleTask(testSocket);

            // Check that the task was added to the ATaskQ
            assertFalse(slaveSystemServer.ATaskQ.isEmpty());
            assertEquals(testTask, slaveSystemServer.ATaskQ.peek());
        }
    }

    @Test
    public void testHandleTask_AddsBTaskToQueue() throws Exception {
        // Create a test task of type B
        Task testTask = new Task(1, 1, TaskType.B);

        // Create a socket with an ObjectOutputStream
        try (Socket ignored = new Socket("localhost", PortNumbers.SlaveServerPort);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

            objectOutputStream.writeObject(testTask);
            objectOutputStream.flush();

            // Simulate reading from the socket
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

            // Manually invoke the handleTask method
            slaveSystemServer.handleTask(new Socket("localhost", PortNumbers.SlaveServerPort) {
                @Override
                public java.io.InputStream getInputStream() {
                    return byteArrayInputStream;
                }
            });

            // Check that the task was added to the BTaskQ
            assertFalse(slaveSystemServer.BTaskQ.isEmpty());
            assertEquals(testTask, slaveSystemServer.BTaskQ.peek());
        }
    }

    @Test
    public void testHandleTask_ThrowsExceptionForUnknownTaskType() {
        assertThrows(IllegalArgumentException.class, () -> {
            // Create a test task with an unknown task type
            Task testTask = new Task(1, 1, null);

            // Create a socket with an ObjectOutputStream
            try (Socket socket = new Socket("localhost", PortNumbers.SlaveServerPort);
                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

                objectOutputStream.writeObject(testTask);
                objectOutputStream.flush();

                // Simulate reading from the socket
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);

                // Manually invoke the handleTask method
                slaveSystemServer.handleTask(new Socket("localhost", PortNumbers.SlaveServerPort) {
                    @Override
                    public java.io.InputStream getInputStream() {
                        return byteArrayInputStream;
                    }
                });
            }
        });
    }
}
