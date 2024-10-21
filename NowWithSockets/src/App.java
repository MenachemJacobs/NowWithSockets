import ValuesLabels.TaskType;

public class App {
    public static void main(String[] args) {
        Task[] taskarray;

        for (int i = 0; i < 5; i++) {
            taskarray = new Task[5];

            for (int j = 0; j < 5; j++) {
                TaskType type = i + j % 3 == 0 ? TaskType.A : TaskType.B;
                taskarray[j] = new Task(i * 5 + j, i, type);
            }

            (new Client(i, taskarray)).execute_tasks();
        }
    }
}
