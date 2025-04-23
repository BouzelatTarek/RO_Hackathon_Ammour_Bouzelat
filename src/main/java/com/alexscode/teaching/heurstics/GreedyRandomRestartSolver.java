package com.alexscode.teaching.heurstics;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.*;

public class GreedyRandomRestartSolver implements TAPSolver {

    int nbTries = 50; // Nombre de redémarrages

    @Override
    public List<Integer> solve(Instance ist) {
        List<Integer> best = new ArrayList<>();
        double bestScore = 0;

        for (int attempt = 0; attempt < nbTries; attempt++) {
            List<Integer> queries = new ArrayList<>();
            for (int i = 0; i < ist.getNbQueries(); i++) {
                queries.add(i);
            }

            // Shuffle initial pour varier les points de départ
            Collections.shuffle(queries);

            Objectives obj = new Objectives(ist);
            List<Integer> current = new ArrayList<>();

            for (int i : queries) {
                current.add(i);
                if (!isFeasible(obj, ist, current)) {
                    current.remove(current.size() - 1);
                }
            }

            double score = obj.interest(current);
            if (score > bestScore) {
                bestScore = score;
                best = new ArrayList<>(current);
            }
        }

        return best;
    }

    private boolean isFeasible(Objectives obj, Instance ist, List<Integer> seq) {
        return obj.time(seq) <= ist.getTimeBudget()
                && obj.distance(seq) <= ist.getMaxDistance()
                && seq.size() == (new HashSet<>(seq)).size();
    }
}
