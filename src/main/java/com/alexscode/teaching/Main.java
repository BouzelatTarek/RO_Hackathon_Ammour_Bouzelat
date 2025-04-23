package com.alexscode.teaching;

import com.alexscode.teaching.heurstics.*;

import com.alexscode.teaching.tap.Instance;
import com.alexscode.teaching.tap.Objectives;
import com.alexscode.teaching.tap.TAPSolver;

import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class Main {

    static Map<String, Double> baselineOptima = Map.of(
            "f1_tap_3_400.dat", 94.63,
            "f1_tap_9_400.dat", 94.36,
            "f4_tap_0_20.dat", 6.92,
            "f4_tap_1_400.dat", 175.47,
            "f4_tap_4_400.dat", 179.71,
            "tap_10_100.dat", 43.33,
            "tap_11_250.dat", 49.16,
            "tap_13_150.dat", 47.03,
            "tap_14_400.dat", 169.29,
            "tap_15_60.dat", 14.46
    );


    static Map<String, int[]> constraints = Map.of(
            "f1_tap_3_400.dat", new int[]{6600, 540},
            "f1_tap_9_400.dat", new int[]{6600, 540},
            "f4_tap_0_20.dat", new int[]{330, 27},
            "f4_tap_1_400.dat", new int[]{6600, 540},
            "f4_tap_4_400.dat", new int[]{6600, 540},
            "tap_10_100.dat", new int[]{1200, 150},
            "tap_11_250.dat", new int[]{1200, 250},
            "tap_13_150.dat", new int[]{1200, 150},
            "tap_14_400.dat", new int[]{6600, 540},
            "tap_15_60.dat", new int[]{330, 27}
    );

    public static void main(String[] args) {
        //TAPSolver solver = new GreedyLocalSearchSolver();
        TAPSolver solver = new GreedyLocalSearchMultiStartSolver();
        //TAPSolver solver = new GreedyLocalSearchMultiStartSolverSwap();
        //TAPSolver solver = new GreedyLocalSearchMultiStartSolverTabu();


        for (String filename : baselineOptima.keySet()) {
            int time = constraints.get(filename)[0];
            int distance = constraints.get(filename)[1];

            String path = "./instances/" + filename;
            System.out.println("\n=== 📂 Test de l’instance : " + filename + " ===");

            Instance ist = Instance.readFile(path, time, distance);
            if (ist.getNbQueries() == 0) {
                System.out.println("⚠️ Instance vide ou fichier non lu : " + filename);
                continue;
            }

            Objectives obj = new Objectives(ist);
            List<Integer> solution = solver.solve(ist);

            double interest = obj.interest(solution);
            System.out.printf("✔ Interet   : %.4f\n", interest);
            System.out.println("✔ Temps     : " + obj.time(solution));
            System.out.println("✔ Distance  : " + obj.distance(solution));
            System.out.println("✔ Faisable  : " + (isSolutionFeasible(ist, solution) ? "✅ OUI" : "❌ NON"));

            double baseline = baselineOptima.get(filename);
            double gap = 100 * (baseline - interest) / baseline;

            System.out.printf("📉 Gap relatif : %.2f %%\n", gap);

            if (gap <= 0) {
                System.out.println("🎉 Ta solution est MEILLEURE ou ÉGALE au optimal ! 🔥");
            } else if (gap <= 20) {
                System.out.println("👌 Ta solution est correcte, proche du optimal.");
            } else {
                System.out.println("🔧 Ta solution est LOIN du optimal, à améliorer.");
            }
        }
    }

    public static boolean isSolutionFeasible(Instance ist, List<Integer> sol) {
        Objectives obj = new Objectives(ist);
        return obj.time(sol) <= ist.getTimeBudget() &&
                obj.distance(sol) <= ist.getMaxDistance() &&
                sol.size() == (new TreeSet<>(sol)).size(); // vérifie l’unicité des requêtes
    }
}
