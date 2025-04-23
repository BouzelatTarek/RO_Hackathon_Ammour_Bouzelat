package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class BranchAndBoundSolver implements TAPSolver {

    private static final long TIME_LIMIT_MS = 10 * 60 * 1000; // 10 minutes for all instances
    private static final int MAX_DEPTH = 10; // Limite de profondeur pour éviter l'explosion de la mémoire
    private static long startTime;

    public static void setStartTime() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        long instanceStartTime = System.currentTimeMillis();

        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingDouble(node -> -node.upperBound));
        queue.add(new Node(new ArrayList<>(), 0, 0, ist, obj, 0));

        List<Integer> bestSolution = new ArrayList<>();
        double bestInterest = 0;

        while (!queue.isEmpty() && (System.currentTimeMillis() - startTime) < TIME_LIMIT_MS &&
                (System.currentTimeMillis() - instanceStartTime) < (TIME_LIMIT_MS / 10)) {
            Node current = queue.poll();
            if (current.solution.size() == ist.getNbQueries() || current.depth >= MAX_DEPTH) {
                double interest = obj.interest(current.solution);
                if (interest > bestInterest) {
                    bestSolution = current.solution;
                    bestInterest = interest;
                }
            } else {
                for (int query : current.unused) {
                    List<Integer> newSolution = new ArrayList<>(current.solution);
                    newSolution.add(query);
                    Set<Integer> newUnused = new TreeSet<>(current.unused);
                    newUnused.remove(query);

                    // Vérifiez si newSolution n'est pas vide avant d'accéder à l'élément précédent
                    double additionalDistance = (newSolution.size() < 2 ? 0 : ist.getDistances()[newSolution.get(newSolution.size() - 2)][query]);

                    queue.add(new Node(newSolution, current.time + ist.getCosts()[query], current.distance + additionalDistance, ist, obj, current.depth + 1));
                }
            }
        }

        return bestSolution;
    }

    private static class Node {
        List<Integer> solution;
        double time;
        double distance;
        double upperBound;
        Set<Integer> unused;
        int depth;

        Node(List<Integer> solution, double time, double distance, Instance ist, Objectives obj, int depth) {
            this.solution = solution;
            this.time = time;
            this.distance = distance;
            this.unused = new TreeSet<>();
            for (int i = 0; i < ist.getNbQueries(); i++) {
                if (!solution.contains(i)) {
                    this.unused.add(i);
                }
            }
            this.upperBound = calculateUpperBound(ist, obj);
            this.depth = depth;
        }

        private double calculateUpperBound(Instance ist, Objectives obj) {
            double interest = obj.interest(solution);
            for (int query : unused) {
                interest += ist.getInterest()[query];
            }
            return interest;
        }
    }
}
