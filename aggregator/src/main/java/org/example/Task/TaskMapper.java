package org.example.Task;

public class TaskMapper {
    public static TaskVote taskVoteFromFaasResult(String taskString) {
        String[] taskBits = taskString.split(";");
        return new TaskVote(Integer.parseInt(taskBits[0]), Double.parseDouble(taskBits[2]), taskBits[1]);
    }

    public static Task taskFromTaskAlert(String taskString) {
        return new Task(Integer.parseInt(taskString), 0.0);
    }
}
