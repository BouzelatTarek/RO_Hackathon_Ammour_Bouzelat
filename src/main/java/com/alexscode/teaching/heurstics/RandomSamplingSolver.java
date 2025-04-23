package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class RandomSamplingSolver implements TAPSolver {

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        int n = ist.getNbQueries();

        List<Integer> bestSolution = new ArrayList<>();
        double bestInterest = 0;

        int maxIterations = 10_000; // ✏️ Ajuste selon ton temps max
        Random rand = new Random();

        for (int iter = 0; iter < maxIterations; iter++) {
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < n; i++) indices.add(i);
            Collections.shuffle(indices, rand);

            List<Integer> current = new ArrayList<>();
            double totalTime = 0;
            double totalDistance = 0;
            int last = -1;

            for (int idx : indices) {
                double addedCost = ist.getCosts()[idx];
                double addedDistance = (last == -1) ? 0 : ist.getDistances()[last][idx];

                if (totalTime + addedCost <= ist.getTimeBudget() &&
                        totalDistance + addedDistance <= ist.getMaxDistance()) {
                    current.add(idx);
                    totalTime += addedCost;
                    totalDistance += addedDistance;
                    last = idx;
                }
            }

            double interest = obj.interest(current);
            if (interest > bestInterest) {
                bestInterest = interest;
                bestSolution = current;
            }
        }

        return bestSolution;
    }
}
