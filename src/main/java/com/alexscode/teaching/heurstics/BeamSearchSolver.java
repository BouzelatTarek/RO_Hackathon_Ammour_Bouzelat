package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class BeamSearchSolver implements TAPSolver {

    int beamWidth = 10; // 🔧 taille du faisceau

    @Override
    public List<Integer> solve(Instance ist) {
        Objectives obj = new Objectives(ist);
        int n = ist.getNbQueries();

        List<List<Integer>> beam = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            List<Integer> initial = new ArrayList<>();
            initial.add(i);
            beam.add(initial);
        }

        List<Integer> best = new ArrayList<>();
        double bestInterest = 0;

        while (!beam.isEmpty()) {
            List<List<Integer>> newBeam = new ArrayList<>();

            for (List<Integer> partial : beam) {
                Set<Integer> used = new HashSet<>(partial);
                int last = partial.get(partial.size() - 1);

                for (int next = 0; next < n; next++) {
                    if (used.contains(next)) continue;

                    List<Integer> extended = new ArrayList<>(partial);
                    extended.add(next);

                    if (obj.time(extended) <= ist.getTimeBudget() &&
                            obj.distance(extended) <= ist.getMaxDistance()) {

                        newBeam.add(extended);
                        double interest = obj.interest(extended);
                        if (interest > bestInterest) {
                            bestInterest = interest;
                            best = extended;
                        }
                    }
                }
            }

            // ⚠️ garde uniquement les k meilleurs
            newBeam.sort(Comparator.comparingDouble(obj::interest).reversed());
            beam = newBeam.subList(0, Math.min(beamWidth, newBeam.size()));
        }

        return best;
    }
}
