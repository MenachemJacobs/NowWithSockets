package Components;
import java.net.Socket;

public record TaskSocketPair(Task task, Socket socket) {
}