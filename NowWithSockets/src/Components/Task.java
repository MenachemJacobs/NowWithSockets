package Components;

import java.io.Serializable;
import java.util.Objects;

public class Task implements Serializable{
    public int taskID;
    public int clientID;
    public TaskType taskType;
    public boolean isComplete = false;

    public Task(int taskNum, int clientNum, TaskType type){
        taskID = taskNum;
        clientID = clientNum;
        taskType = type;
    }

    public void efficientExecute() {
        try{
            Thread.sleep(2 * 1000);
            isComplete = true;
        } catch(InterruptedException ignored){}
    }

    public void inefficientExecute() {
        try{
            Thread.sleep(10 * 1000);
            isComplete = true;
        } catch(InterruptedException ignored){}
    }

    @Override
    public String toString() {
        return "ClientID: " + clientID + " TaskID: " + taskID + " TaskType: " + taskType + "  isComplete: " + isComplete;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return taskID == task.taskID &&
                clientID == task.clientID &&
                taskType == task.taskType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskID, clientID, taskType);
    }
}

