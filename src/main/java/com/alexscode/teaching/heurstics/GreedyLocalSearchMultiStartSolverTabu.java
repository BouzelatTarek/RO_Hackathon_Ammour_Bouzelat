package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyLocalSearchMultiStartSolverTabu implements TAPSolver {

    // Number of different starting points for the greedy algorithm
    private static final int NUM_STARTS = 20;

    // Number of iterations a move is considered tabu
    private static final int TABU_TENURE = 5;

    @Override
    public List<Integer> solve(Instance ist) {
        // Initialize the objectives calculator
        Objectives obj = new Objectives(ist);

        // Initialize the best solution found and its interest value
        List<Integer> bestSol = new ArrayList<>();
        double bestInterest = 0;

        // Loop over multiple starting points
        for (int start = 0; start < NUM_STARTS; start++) {
            // Build a greedy solution starting from a specific query
            List<Integer> solution = buildGreedySolution(ist, start % ist.getNbQueries());

            // Improve the solution using tabu search
            solution = tabuSearch(solution, ist);

            // Calculate the interest of the current solution
            double interest = obj.interest(solution);

            // Update the best solution if the current one is better
            if (interest > bestInterest) {
                bestInterest = interest;
                bestSol = solution;
            }
        }

        // Return the best solution found
        return bestSol;
    }

    private List<Integer> buildGreedySolution(Instance ist, int start) {
        // Initialize the objectives calculator
        Objectives obj = new Objectives(ist);

        // Get the number of queries
        int n = ist.getNbQueries();

        // Initialize the current solution and the set of unused queries
        List<Integer> current = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < n; i++) unused.add(i);

        // Start with the initial query
        current.add(start);
        unused.remove(start);

        // Greedily add queries to the solution
        while (true) {
            int best = -1;
            double bestRatio = -1;

            // Iterate over unused queries
            for (int i : unused) {
                // Calculate the added cost and distance for the query
                double addedCost = ist.getCosts()[i];
                double addedDistance = ist.getDistances()[current.get(current.size() - 1)][i];

                // Check if adding the query is feasible
                if (obj.time(current) + addedCost <= ist.getTimeBudget()
                        && obj.distance(current) + addedDistance <= ist.getMaxDistance()) {
                    // Calculate the interest-to-cost ratio
                    double ratio = ist.getInterest()[i] / (addedCost + addedDistance);

                    // Update the best query to add
                    if (ratio > bestRatio) {
                        bestRatio = ratio;
                        best = i;
                    }
                }
            }

            // If no feasible query was found, exit the loop
            if (best == -1) break;

            // Add the best query to the solution
            current.add(best);
            unused.remove(best);
        }

        // Return the greedy solution
        return current;
    }

    private List<Integer> tabuSearch(List<Integer> current, Instance ist) {
        // Initialize the objectives calculator
        Objectives obj = new Objectives(ist);

        // Initialize the set of unused queries
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) {
            if (!current.contains(i)) unused.add(i);
        }

        // Initialize the tabu list to keep track of tabu moves
        Map<String, Integer> tabuList = new HashMap<>();
        int iteration = 0;
        double bestInterest = obj.interest(current);
        List<Integer> bestSolution = new ArrayList<>(current);

        boolean improved;

        // Perform tabu search until no improvement is found
        do {
            improved = false;
            iteration++;

            // Substitution phase: Try replacing each query in the solution with an unused query
            for (int i = 1; i < current.size(); i++) {
                for (int candidate : unused) {
                    // Create a new solution with the replacement
                    List<Integer> newSolution = new ArrayList<>(current);
                    newSolution.set(i, candidate);

                    // Define the move as a string for tabu list tracking
                    String move = "swap_in_" + candidate + "_out_" + current.get(i);

                    // Skip the move if it is tabu
                    if (tabuList.getOrDefault(move, 0) > iteration) continue;

                    // Check if the new solution is feasible and better
                    if (obj.time(newSolution) <= ist.getTimeBudget()
                            && obj.distance(newSolution) <= ist.getMaxDistance()
                            && obj.interest(newSolution) > obj.interest(current)) {
                        // Update the tabu list with the new move
                        tabuList.put(move, iteration + TABU_TENURE);

                        // Update the current solution and the set of unused queries
                        unused.add(current.get(i));
                        current.set(i, candidate);
                        unused.remove(candidate);
                        improved = true;
                        break;
                    }
                }
                if (improved) break;
            }

            // Swap phase: Try swapping two internal positions in the solution
            for (int i = 1; i < current.size() - 1 && !improved; i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    // Create a new solution with the swap
                    List<Integer> newSolution = new ArrayList<>(current);
                    Collections.swap(newSolution, i, j);

                    // Define the move as a string for tabu list tracking
                    String move = "swap_pos_" + i + "_" + j;

                    // Skip the move if it is tabu
                    if (tabuList.getOrDefault(move, 0) > iteration) continue;

                    // Check if the new solution is feasible and better
                    if (obj.time(newSolution) <= ist.getTimeBudget()
                            && obj.distance(newSolution) <= ist.getMaxDistance()
                            && obj.interest(newSolution) > obj.interest(current)) {
                        // Update the tabu list with the new move
                        tabuList.put(move, iteration + TABU_TENURE);

                        // Update the current solution with the swap
                        Collections.swap(current, i, j);
                        improved = true;
                        break;
                    }
                }
                if (improved) break;
            }

            // Update the best solution found if the current solution is better
            double currentInterest = obj.interest(current);
            if (currentInterest > bestInterest) {
                bestInterest = currentInterest;
                bestSolution = new ArrayList<>(current);
            }

        } while (improved);

        // Return the best solution found
        return bestSolution;
    }
}
