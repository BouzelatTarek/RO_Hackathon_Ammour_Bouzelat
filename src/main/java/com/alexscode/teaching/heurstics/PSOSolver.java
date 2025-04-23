package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class PSOSolver implements TAPSolver {

    private static final int SWARM_SIZE = 30;
    private static final double INERTIA = 0.5;
    private static final double COGNITIVE_PARAM = 1.5;
    private static final double SOCIAL_PARAM = 1.5;
    private static final long TIME_LIMIT_MS = 10 * 60 * 1000; // 10 minutes for each instance

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        Random random = new Random();
        long startTime = System.currentTimeMillis();

        // Initialize particles
        List<Particle> swarm = new ArrayList<>();
        for (int i = 0; i < SWARM_SIZE; i++) {
            swarm.add(new Particle(generateRandomSolution(ist, obj), ist, obj));
        }

        Particle globalBest = swarm.stream().max(Comparator.comparingDouble(p -> obj.interest(p.position))).orElse(null);

        while (System.currentTimeMillis() - startTime < TIME_LIMIT_MS) {
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
