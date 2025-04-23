package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyLocalSearchMultiStartSolverSwap implements TAPSolver {

    private static final int NUM_STARTS = 20;

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        List<Integer> bestSol = new ArrayList<>();
        double bestInterest = 0;

        for (int start = 0; start < NUM_STARTS; start++) {
            List<Integer> solution = buildGreedySolution(ist, start % ist.getNbQueries());
            solution = localSearch(solution, ist);
            double interest = obj.interest(solution);
            if (interest > bestInterest) {
                bestInterest = interest;
                bestSol = solution;
            }
        }

        return bestSol;
    }

    private List<Integer> buildGreedySolution(Instance ist, int start) {
        Objectives obj = new Objectives(ist);
        int n = ist.getNbQueries();
        List<Integer> current = new ArrayList<>();
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < n; i++) unused.add(i);

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

        return current;
    }

    private List<Integer> localSearch(List<Integer> current, Instance ist) {
        Objectives obj = new Objectives(ist);
        Set<Integer> unused = new TreeSet<>();
        for (int i = 0; i < ist.getNbQueries(); i++) {
            if (!current.contains(i)) unused.add(i);
        }

        boolean improved;
        do {
            improved = false;
            // Substitution
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

            // SWAP entre deux positions internes de la solution
            for (int i = 1; i < current.size() - 1 && !improved; i++) {
                for (int j = i + 1; j < current.size(); j++) {
                    List<Integer> newSolution = new ArrayList<>(current);
                    Collections.swap(newSolution, i, j);
                    if (obj.time(newSolution) <= ist.getTimeBudget()
                            && obj.distance(newSolution) <= ist.getMaxDistance()
                            && obj.interest(newSolution) > obj.interest(current)) {
                        Collections.swap(current, i, j);
                        improved = true;
                        break;
                    }
                }
            }
        } while (improved);

        return current;
    }
}