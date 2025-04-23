package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;
import java.util.stream.Collectors;

public class HybridPSOSolver implements TAPSolver {

    private static final int SWARM_SIZE = 30;
    private static final int POPULATION_SIZE = 50;
    private static final double INERTIA = 0.5;
    private static final double COGNITIVE_PARAM = 1.5;
    private static final double SOCIAL_PARAM = 1.5;
    private static final double MUTATION_RATE = 0.01;
    private static final int TOURNAMENT_SIZE = 5;
    private static final long TIME_LIMIT_MS = 10 * 60 * 1000; // 10 minutes for each instance

    public static void setStartTime() {
    }

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        // Initialize particles (PSO)
        List<Particle> swarm = new ArrayList<>();
        for (int i = 0; i < SWARM_SIZE; i++) {
            swarm.add(new Particle(generateRandomSolution(ist, obj), ist, obj));
        }

        // Initialize population (GA)
        List<List<Integer>> population = new ArrayList<>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population.add(generateRandomSolution(ist, obj));
        }

        Particle globalBest = swarm.stream().max(Comparator.comparingDouble(p -> obj.interest(p.position))).orElse(null);

        while (System.currentTimeMillis() - startTime < TIME_LIMIT_MS) {
            // PSO update
            for (Particle particle : swarm) {
                particle.updateVelocity(globalBest, INERTIA, COGNITIVE_PARAM, SOCIAL_PARAM, random);
                particle.updatePosition(ist, obj);
                if (obj.interest(particle.position) > obj.interest(particle.personalBest)) {
                    particle.personalBest = new ArrayList<>(particle.position);
                }
                if (obj.interest(particle.position) > obj.interest(globalBest.position)) {
                    globalBest = particle;
                }
            }

            // GA update
            List<List<Integer>> newPopulation = new ArrayList<>();
            for (int i = 0; i < POPULATION_SIZE; i++) {
                List<Integer> parent1 = tournamentSelection(population, obj);
                List<Integer> parent2 = tournamentSelection(population, obj);
                List<Integer> child = crossover(parent1, parent2, ist, obj);
                mutate(child, ist, obj);
                newPopulation.add(child);
            }
            population = newPopulation;

            // Combine PSO and GA
            for (Particle particle : swarm) {
                List<Integer> gaSolution = population.get(random.nextInt(POPULATION_SIZE));
                List<Integer> newSolution = combineSolutions(particle.position, gaSolution, ist, obj);
                if (obj.interest(newSolution) > obj.interest(particle.position)) {
                    particle.position = newSolution;
                }
            }
        }

        return globalBest.position;
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

    private List<Integer> combineSolutions(List<Integer> psoSolution, List<Integer> gaSolution, Instance ist, Objectives obj) {
        Random random = new Random();
        List<Integer> combinedSolution = new ArrayList<>(psoSolution);
        for (int i = 0; i < gaSolution.size(); i++) {
            if (random.nextDouble() < 0.5 && !combinedSolution.contains(gaSolution.get(i))) {
                combinedSolution.add(gaSolution.get(i));
            }
        }
        // Ensure the combined solution is feasible
        combinedSolution = combinedSolution.stream().distinct().collect(Collectors.toList());
        while (obj.time(combinedSolution) > ist.getTimeBudget() || obj.distance(combinedSolution) > ist.getMaxDistance()) {
            combinedSolution.remove(combinedSolution.size() - 1);
        }
        return combinedSolution;
    }

    private static class Particle {
        List<Integer> position;
        List<Integer> personalBest;
        double[] velocity;

        Particle(List<Integer> initialPosition, Instance ist, Objectives obj) {
            this.position = initialPosition;
            this.personalBest = new ArrayList<>(initialPosition);
            this.velocity = new double[ist.getNbQueries()];
        }

        void updateVelocity(Particle globalBest, double inertia, double cognitiveParam, double socialParam, Random random) {
            for (int i = 0; i < velocity.length; i++) {
                double r1 = random.nextDouble();
                double r2 = random.nextDouble();
                velocity[i] = inertia * velocity[i] +
                        cognitiveParam * r1 * (personalBest.contains(i) ? 1 : 0) +
                        socialParam * r2 * (globalBest.position.contains(i) ? 1 : 0);
            }
        }

        void updatePosition(Instance ist, Objectives obj) {
            Random random = new Random();
            for (int i = 0; i < velocity.length; i++) {
                if (random.nextDouble() < Math.abs(velocity[i])) {
                    int query = i;
                    if (!position.contains(query)) {
                        position.add(query);
                        if (obj.time(position) > ist.getTimeBudget() || obj.distance(position) > ist.getMaxDistance()) {
                            position.remove(position.size() - 1);
                        }
                    }
                }
            }
        }
    }
}
