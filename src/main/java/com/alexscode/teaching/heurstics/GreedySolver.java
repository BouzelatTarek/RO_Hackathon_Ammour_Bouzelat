package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GreedySolver implements TAPSolver {

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);

        List<Integer> all = new ArrayList<>();
        for (int i = 0; i < ist.getNbQueries(); i++) {
            all.add(i);
        }

        // Trier par intérêt / coût décroissant
        all.sort(Comparator.comparingDouble(i -> -ist.getInterest()[i] / ist.getCosts()[i]));

        List<Integer> solution = new ArrayList<>();
        double currentTime = 0;
        double currentDistance = 0;

        for (int i = 0; i < all.size(); i++) {
            int candidate = all.get(i);

            if (!solution.isEmpty()) {
                int last = solution.get(solution.size() - 1);
                currentDistance += ist.getDistances()[last][candidate];
            }

            currentTime += ist.getCosts()[candidate];

            if (currentTime <= ist.getTimeBudget() && currentDistance <= ist.getMaxDistance()) {
                solution.add(candidate);
            } else {
                break;
            }
        }

        return solution;
    }
}
