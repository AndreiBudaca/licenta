package org.example.Task;

import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private final HashMap<String, Double> weights = new HashMap<>();
    private final HashMap<String, Double> voteErrors = new HashMap<>();
    private final HashMap<String, Integer> totalVotes = new HashMap<>();
    private int voters = 0;

    public HashMap<String, Double> getWeights() {
        return weights;
    }

    public Task updateTask(Task task, TaskVote taskVote) {
        double weight = weights.get(taskVote.getVoter());
        List<String> newVoters = task.getVoters();
        newVoters.add(taskVote.getVoter());
        List<Double> newVotes = task.getVotes();
        newVotes.add(taskVote.getTrust());

        if (weight == 0) {
            return new Task(task.getIdentifier(), task.getTrust(), newVoters, newVotes);
        }

        if (task.getVoters().isEmpty()) {
            return new Task(task.getIdentifier(), taskVote.getTrust(), newVoters, newVotes);
        }

        double currentVotersWeightSum = 0.0;
        for (String voter: task.getVoters()) {
            currentVotersWeightSum += weights.get(voter);
        }

        double newTrust = currentVotersWeightSum / (currentVotersWeightSum + weight) * task.getTrust()
                + taskVote.getTrust() / (currentVotersWeightSum + weight);
        return new Task(task.getIdentifier(), newTrust, newVoters, newVotes);
    }

    public ConcludedTask givPartialVerdict(Task task) {
        return new ConcludedTask(task.getIdentifier(), task.getTrust(),
                task.getVoters(), task.getVotes(), task.getTrust() > 0.5 ? TaskDecision.Normal : TaskDecision.Hostile);
    }

    public ConcludedTask giveFinalVerdict(Task task) {
        double finalTrust = updateWeights(task);

        return new ConcludedTask(task.getIdentifier(), finalTrust,
                task.getVoters(), task.getVotes(), finalTrust > 0.5 ? TaskDecision.Normal : TaskDecision.Hostile);
    }

    public boolean canGivePartialVerdict(Task task) {
        return task.getTrust() >= 0.5;
    }

    public boolean canGiveFinalVerdict(Task task) {
        return task.getVoters().size() == voters;
    }

    public void addVoter(String name) {
        ++voters;
        if (voters != 1) {
            weights.put(name, 0.0);
        } else {
            weights.put(name, 1.0);
        }

        voteErrors.put(name, 0.0);
        totalVotes.put(name, 0);
    }

    public void deleteVote(String name) {
        --voters;
        weights.remove(name);
        voteErrors.remove(name);
        totalVotes.remove(name);
    }

    private double updateWeights(Task task) {
        double lastTrust = -100;
        double currentTrust = task.getTrust();

        Double[] moduleErrors = new Double[task.getVoters().size()];
        Double[] newWeights = new Double[task.getVoters().size()];
        int rounds = 0;
        while (Math.abs(currentTrust - lastTrust) > EnvConfiguration.weightEps) {
            // Compute each voter total error based on current weights
            double errorSum = 0.0;
            for (int i = 0; i < task.getVoters().size(); ++i) {
                String voterName = task.getVoters().get(i);
                double vote = task.getVotes().get(i);

                int votes = totalVotes.get(voterName);
                moduleErrors[i] = votes / (votes + 1) * voteErrors.get(voterName) +
                        (currentTrust - vote) * (currentTrust - vote) / (votes + 1);

                errorSum += moduleErrors[i];
            }

            // Update the weights
            double newWeightsSum = 0.0;
            double newWeightedDecisionSum = 0.0;
            for (int i = 0; i < task.getVoters().size(); ++i) {
                newWeights[i] = Math.log(errorSum / moduleErrors[i]);
                newWeightsSum += newWeights[i];
                newWeightedDecisionSum += newWeights[i] * task.getVotes().get(i);
            }

            // Recompute the trust using the new weights
            lastTrust = currentTrust;
            currentTrust = newWeightedDecisionSum / newWeightsSum;

            ++rounds;
        }

        System.out.println("Updated weights in " + rounds + " rounds");

        // Update each voter weights, total error and votes
        for (int i = 0; i < task.getVoters().size(); ++i) {
            String voterName = task.getVoters().get(i);

            weights.replace(voterName, newWeights[i]);
            totalVotes.replace(voterName, totalVotes.get(voterName) + 1);
            voteErrors.replace(voterName, moduleErrors[i]);
        }

        return currentTrust;
    }

    private static class EnvConfiguration {
        public final static double weightEps = System.getenv("WEIGHT_EPS") == null ?
        .01 : Double.parseDouble(System.getenv("WEIGHT_EPS"));
    }
}
