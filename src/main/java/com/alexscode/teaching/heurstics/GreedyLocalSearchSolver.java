package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyLocalSearchSolver implements TAPSolver {

    @Override
    public List<Integer> solve(Instance ist) {
        // Initialize the objectives calculator
        Objectives obj = new Objectives(ist);

        // Get the number of queries
        int n = ist.getNbQueries();

        // Initialize the current solution and the set of unused queries
        List<Integer> current = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();

        // Populate the set of unused queries with all query indices
        for (int i = 0; i < n; i++) unused.add(i);

        // Greedy construction phase

        // Start with the first query
        int start = 0;
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

        // Local search phase
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

        // Return the final solution
        return current;
    }
}
