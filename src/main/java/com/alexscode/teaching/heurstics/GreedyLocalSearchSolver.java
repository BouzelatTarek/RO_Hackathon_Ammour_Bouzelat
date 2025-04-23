package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyLocalSearchSolver implements TAPSolver {

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        int n = ist.getNbQueries();
        List<Integer> current = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();

        for (int i = 0; i < n; i++) unused.add(i);

        // Greedy construction
        int start = 0;
        current.add(start);
        unused.remove(start);

        while (true) {
            int best = -1;
            double bestRatio = -1;
            for (int i : unused) {
                double addedCost = ist.getCosts()[i];
                double addedDistance = ist.getDistances()[current.get(current.size() - 1)][i];
                if (obj.time(current) + addedCost <= ist.getTimeBudget()
                        && obj.distance(current) + addedDistance <= ist.getMaxDistance()) {
                    double ratio = ist.getInterest()[i] / (addedCost + addedDistance);
                    if (ratio > bestRatio) {
                        bestRatio = ratio;
                        best = i;
                    }
                }
            }
            if (best == -1) break;
            current.add(best);
            unused.remove(best);
        }

        // Local search
        boolean improved;
        do {
            improved = false;
            for (int i = 1; i < current.size(); i++) {
                for (int candidate : unused) {
                    List<Integer> newSolution = new ArrayList<>(current);
                    newSolution.set(i, candidate);
                    if (obj.time(newSolution) <= ist.getTimeBudget()
                            && obj.distance(newSolution) <= ist.getMaxDistance()
                            && obj.interest(newSolution) > obj.interest(current)) {
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

        return current;
    }
}
