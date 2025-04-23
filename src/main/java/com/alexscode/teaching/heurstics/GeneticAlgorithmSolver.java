package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;
import java.util.stream.Collectors;

public class GeneticAlgorithmSolver implements TAPSolver {

    private static final int POPULATION_SIZE = 50;
    private static final double MUTATION_RATE = 0.01;
    private static final int TOURNAMENT_SIZE = 5;
    private static final long TIME_LIMIT_MS = 5 * 60 * 1000; // 10 minutes for each instance

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        // Initialize population
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution(ist, obj));
        }

        while (System.currentTimeMillis() - startTime < TIME_LIMIT_MS) {
            // Selection
            List<List<Integer>> newPopulation = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                List<Integer> parent1 = tournamentSelection(population, obj);
                List<Integer> parent2 = tournamentSelection(population, obj);
                List<Integer> child = crossover(parent1, parent2, ist, obj);
                mutate(child, ist, obj);
                newPopulation.add(child);
            }
            population = newPopulation;
        }

        // Return the best solution found
        return population.stream().max(Comparator.comparingDouble(obj::interest)).orElse(new ArrayList<>());
    }

    private List<Integer> generateRandomSolution(Instance ist, Objectives obj) {
        List<Integer> solution = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) unused.add(i);

        while (!unused.isEmpty()) {
            int query = unused.stream().skip(new Random().nextInt(unused.size())).findFirst().orElse(-1);
            if (obj.time(solution) + ist.getCosts()[query] <= ist.getTimeBudget() &&
                    obj.distance(solution) + (solution.isEmpty() ? 0 : ist.getDistances()[solution.get(solution.size() - 1)][query]) <= ist.getMaxDistance()) {
                solution.add(query);
            }
            unused.remove(query);
        }

        return solution;
    }

    private List<Integer> tournamentSelection(List<List<Integer>> population, Objectives obj) {
        List<List<Integer>> tournament = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < TOURNAMENT_SIZE; i++) {
            tournament.add(population.get(random.nextInt(population.size())));
        }
        return tournament.stream().max(Comparator.comparingDouble(obj::interest)).orElse(new ArrayList<>());
    }

    private List<Integer> crossover(List<Integer> parent1, List<Integer> parent2, Instance ist, Objectives obj) {
        Random random = new Random();
        int crossoverPoint = random.nextInt(Math.min(parent1.size(), parent2.size()));
        List<Integer> child = new ArrayList<>(parent1.subList(0, crossoverPoint));
        child.addAll(parent2.subList(crossoverPoint, parent2.size()));

        // Ensure the child is feasible
        child = child.stream().distinct().collect(Collectors.toList());
        while (obj.time(child) > ist.getTimeBudget() || obj.distance(child) > ist.getMaxDistance()) {
            child.remove(child.size() - 1);
        }

        return child;
    }

    private void mutate(List<Integer> solution, Instance ist, Objectives obj) {
        Random random = new Random();
        if (random.nextDouble() < MUTATION_RATE) {
            int index = random.nextInt(solution.size());
            int newQuery = random.nextInt(ist.getNbQueries());
            if (!solution.contains(newQuery)) {
                solution.set(index, newQuery);
                // Ensure the solution is feasible
                while (obj.time(solution) > ist.getTimeBudget() || obj.distance(solution) > ist.getMaxDistance()) {
                    solution.remove(solution.size() - 1);
                }
            }
        }
    }
}
