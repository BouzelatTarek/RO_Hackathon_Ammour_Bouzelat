package com.alexscode.teaching.tap;

import java.util.List;

public class Objectives {
    Instance ist;

    public Objectives(Instance ist) {
        this.ist = ist;
    }

    public double interest(List<Integer> sequence){
        return sequence.stream().mapToDouble(i -> ist.interest[i]).sum();
    }

    public double time(List<Integer> sequence){
        return sequence.stream().mapToDouble(i -> ist.costs[i]).sum();
    }

    public double distance(List<Integer> sequence){
        double d = 0;
        for (int i = 0; i < sequence.size() - 1; i++) {
            d += ist.distances[sequence.get(i)][sequence.get(i+1)];
        }
        return d;
    }
}
