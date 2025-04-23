package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.ArrayList;
import java.util.List;

public class TestNaif implements TAPSolver {
    @Override
    public List<Integer> solve(Instance ist) {
        List<Integer> solution = new ArrayList<>();
        Objectives obj = new Objectives(ist);
        int q = 0;

        while (q < ist.getNbQueries()) {
            solution.add(q);
            if (obj.time(solution) > ist.getTimeBudget() || obj.distance(solution) > ist.getMaxDistance()) {
                solution.remove(solution.size() - 1);
                break;
            }
            q++;
        }
        return solution;
    }
}
