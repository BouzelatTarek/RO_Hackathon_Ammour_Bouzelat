package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class ACOSolver implements TAPSolver {

    private static final int ANT_COUNT = 30;
    private static final double EVAPORATION_RATE = 0.5;
    private static final double ALPHA = 1.0;
    private static final double BETA = 2.0;
    private static final long TIME_LIMIT_MS = 10 * 60 * 1000; // 10 minutes for all instances
    private static long startTime;

    public static void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        Random random = new Random();
        long instanceStartTime = System.currentTimeMillis();

        double[][] pheromone = new double[ist.getNbQueries()][ist.getNbQueries()];
        List<Integer> globalBest = new ArrayList<>();

        while ((System.currentTimeMillis() - startTime) < TIME_LIMIT_MS &&
                (System.currentTimeMillis() - instanceStartTime) < (TIME_LIMIT_MS / 10)) {
            List<List<Integer>> solutions = new ArrayList<>();
            for (int i = 0; i < ANT_COUNT; i++) {
                solutions.add(constructSolution(ist, obj, pheromone, random));
            }
            updatePheromone(solutions, ist, obj, pheromone);
            globalBest = solutions.stream().max(Comparator.comparingDouble(obj::interest)).orElse(new ArrayList<>());
        }

        return globalBest;
    }

    private List<Integer> constructSolution(Instance ist, Objectives obj, double[][] pheromone, Random random) {
        List<Integer> solution = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) unused.add(i);

        int current = unused.stream().skip(random.nextInt(unused.size())).findFirst().orElse(-1);
        solution.add(current);
        unused.remove(current);

        while (!unused.isEmpty()) {
            double[] probabilities = new double[unused.size()];
            int index = 0;
            double total = 0.0;
            for (int next : unused) {
                double pheromoneValue = pheromone[current][next];
                double heuristicValue = Math.pow(ist.getInterest()[next], ALPHA) / (ist.getCosts()[next] + ist.getDistances()[current][next]);
                probabilities[index++] = Math.pow(pheromoneValue, BETA) * heuristicValue;
                total += probabilities[index - 1];
            }
            double r = random.nextDouble() * total;
            double cumulative = 0.0;
            index = 0;
            for (int next : unused) {
                cumulative += probabilities[index];
                if (r <= cumulative) {
                    current = next;
                    break;
                }
                index++;
            }
            solution.add(current);
            unused.remove(current);
        }

        return solution;
    }

    private void updatePheromone(List<List<Integer>> solutions, Instance ist, Objectives obj, double[][] pheromone) {
        for (int i = 0; i < pheromone.length; i++) {
            for (int j = 0; j < pheromone[i].length; j++) {
                pheromone[i][j] *= (1 - EVAPORATION_RATE);
            }
        }
        for (List<Integer> solution : solutions) {
            double interest = obj.interest(solution);
            for (int i = 0; i < solution.size() - 1; i++) {
                int from = solution.get(i);
                int to = solution.get(i + 1);
                pheromone[from][to] += interest;
            }
        }
    }
}
