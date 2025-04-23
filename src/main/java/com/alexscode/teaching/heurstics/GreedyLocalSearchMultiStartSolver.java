package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyLocalSearchMultiStartSolver implements TAPSolver {

    // Number of different starting points for the greedy algorithm
    private static final int NUM_STARTS = 20;

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

            // Improve the solution using local search
            solution = localSearch(solution, ist);

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

    private List<Integer> localSearch(List<Integer> current, Instance ist) {
        // Initialize the objectives calculator
        Objectives obj = new Objectives(ist);

        // Initialize the set of unused queries
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) {
            if (!current.contains(i)) unused.add(i);
        }

        boolean improved;

        // Perform local search until no improvement is found
        do {
            improved = false;

            // Iterate over the queries in the current solution
            for (int i = 1; i < current.size(); i++) {
                // Try replacing the query with each unused query
                for (int candidate : unused) {
                    // Create a new solution with the replacement
                    List<Integer> newSolution = new ArrayList<>(current);
                    newSolution.set(i, candidate);

                    // Check if the new solution is feasible and better
                    if (obj.time(newSolution) <= ist.getTimeBudget()
                            && obj.distance(newSolution) <= ist.getMaxDistance()
                            && obj.interest(newSolution) > obj.interest(current)) {
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
        } while (improved);

        // Return the improved solution
        return current;
    }
}
