import Components.Task;
import Components.TaskType;

public class App {
    public static void main(String[] args) {
        Task[] taskarray;
        int size = 10;
        int frequency = 3;

        for (int i = 0; i < size; i++) {
            taskarray = new Task[size];

            for (int j = 0; j < size; j++) {
                TaskType type = i + j % frequency == 0 ? TaskType.A : TaskType.B;
                taskarray[j] = new Task(i * size + j, i, type);
            }

            (new Client(i, taskarray)).execute_tasks();
        }
    }
}
