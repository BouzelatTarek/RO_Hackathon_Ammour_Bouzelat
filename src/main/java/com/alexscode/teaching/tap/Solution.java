package com.alexscode.teaching.tap;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class Solution {
    List<Integer> seq;
    String instance_id;
    int timeBudget;
    int maxDistance;
    int nEtu;

    public Solution(Instance ist, List<Integer> sequence, int nEtudiant) {
        this.seq = sequence;
        nEtu = nEtudiant;
        timeBudget = ist.getTimeBudget();
        maxDistance = ist.getMaxDistance();
        instance_id = ist.getFileUsed();
    }

    public void writeToFile(String path){
        try(PrintWriter out = new PrintWriter(new BufferedOutputStream(new FileOutputStream(path)))){
            out.println(nEtu);
            out.println(instance_id);
            out.println(timeBudget);
            out.println(maxDistance);
            out.println(seq.stream().map(Object::toString).collect(Collectors.joining(",")));
        } catch (IOException e){
            e.printStackTrace(System.err);
        }
    }
}
