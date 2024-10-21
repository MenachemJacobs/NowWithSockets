import ValuesLabels.TaskType;

import java.io.Serializable;

public class Task implements Serializable{
    int taskID;
    int clientID;
    TaskType taskType;

    Task(int taskNum, int clientNum, TaskType type){
        taskID = taskNum;
        clientID = clientNum;
        taskType = type;
    }

    void efficientExecute() {
        try{
            Thread.sleep(2 * 1000);
        } catch(InterruptedException ignored){}
    }

    void inefficientExecute() {
        try{
            Thread.sleep(10 * 1000);
        } catch(InterruptedException ignored){}
    }
}

