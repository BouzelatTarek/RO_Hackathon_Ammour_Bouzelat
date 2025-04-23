package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class SimulatedAnnealingSolver implements TAPSolver {

    private static final double INITIAL_TEMPERATURE = 1000.0;
    private static final double COOLING_RATE = 0.99;
    private static final long TIME_LIMIT_MS = 10 * 60 * 1000; // 10 minutes for each instance

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        Random random = new Random();
        double temperature = INITIAL_TEMPERATURE;
        long startTime = System.currentTimeMillis();

        // Initial solution (can be a greedy solution or a random feasible solution)
        List<Integer> currentSolution = generateInitialSolution(ist, obj);
        double currentInterest = obj.interest(currentSolution);

        while (temperature > 1 && System.currentTimeMillis() - startTime < TIME_LIMIT_MS) {
            List<Integer> newSolution = generateNeighborSolution(currentSolution, ist, obj);
            double newInterest = obj.interest(newSolution);

            if (acceptanceProbability(currentInterest, newInterest, temperature) > random.nextDouble()) {
                currentSolution = newSolution;
                currentInterest = newInterest;
            }

            temperature *= COOLING_RATE;
        }

        return currentSolution;
    }

    private List<Integer> generateInitialSolution(Instance ist, Objectives obj) {
        List<Integer> solution = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) unused.add(i);

        int start = 0;
        solution.add(start);
        unused.remove(start);

        while (true) {
            int best = -1;
            double bestRatio = -1;
            for (int i : unused) {
                double addedCost = ist.getCosts()[i];
                double addedDistance = ist.getDistances()[solution.get(solution.size() - 1)][i];
                if (obj.time(solution) + addedCost <= ist.getTimeBudget()
                        && obj.distance(solution) + addedDistance <= ist.getMaxDistance()) {
                    double ratio = ist.getInterest()[i] / (addedCost + addedDistance);
                    if (ratio > bestRatio) {
                        bestRatio = ratio;
                        best = i;
                    }
                }
            }
            if (best == -1) break;
            solution.add(best);
            unused.remove(best);
        }

        return solution;
    }

    private List<Integer> generateNeighborSolution(List<Integer> solution, Instance ist, Objectives obj) {
        List<Integer> newSolution = new ArrayList<>(solution);
        Random random = new Random();
        int index = random.nextInt(newSolution.size());
        int newQuery = random.nextInt(ist.getNbQueries());

        if (!newSolution.contains(newQuery)) {
            newSolution.set(index, newQuery);
            if (obj.time(newSolution) <= ist.getTimeBudget() && obj.distance(newSolution) <= ist.getMaxDistance()) {
                return newSolution;
            }
        }

        return solution; // Return the original solution if the new one is not feasible
    }

    private double acceptanceProbability(double currentInterest, double newInterest, double temperature) {
        if (newInterest > currentInterest) {
            return 1.0;
        }
        return Math.exp((newInterest - currentInterest) / temperature);
    }
}
