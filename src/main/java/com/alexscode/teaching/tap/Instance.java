package com.alexscode.teaching.tap;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.Scanner;

@Data
public class Instance {
    int size;
    double[][] distances;
    double[] costs;
    double[] interest;
    int timeBudget;
    int maxDistance;
    String fileUsed;

    public Instance(int size, double[][] distances, double[] costs, double[] interest, int timeBudget, int maxDistance, String fileUsed) {
        this.size = size;
        this.distances = distances;
        this.costs = costs;
        this.interest = interest;
        this.timeBudget = timeBudget;
        this.maxDistance = maxDistance;
        this.fileUsed = fileUsed;
    }

    public int getNbQueries() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double[][] getDistances() {
        return distances;
    }

    public void setDistances(double[][] distances) {
        this.distances = distances;
    }

    public double[] getCosts() {
        return costs;
    }

    public void setCosts(double[] costs) {
        this.costs = costs;
    }

    public double[] getInterest() {
        return interest;
    }

    public void setInterest(double[] interest) {
        this.interest = interest;
    }

    public int getTimeBudget() {
        return timeBudget;
    }

    public void setTimeBudget(int timeBudget) {
        this.timeBudget = timeBudget;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public String getFileUsed() {
        return fileUsed;
    }

    public void setFileUsed(String fileUsed) {
        this.fileUsed = fileUsed;
    }

    public static Instance readFile(String path, int time, int distance){
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new Instance(0, null, null, null, 0, 0, path);
        }

        String line = scanner.nextLine();
        int nbActions = Integer.parseInt(line);


        double[] relevancesOrig = new double[nbActions];
        line = scanner.nextLine();
        String[] val;
        val = line.split(" ");
        for (int i = 0; i < nbActions; i++) {
            relevancesOrig[i] =Double.parseDouble(val[i]);
        }


        double[] costsOrig = new double[nbActions];
        line = scanner.nextLine();
        val = line.split(" ");
        for (int i = 0; i < nbActions; i++) {
            costsOrig[i] = Double.parseDouble(val[i]);
        }

        int i = 0;
        double[][] distances = new double[nbActions][nbActions];
        while (scanner.hasNext()) {
            line = scanner.nextLine();

            val = line.split(" ");
            for (int j = 0; j < nbActions; j++) {
                //System.out.println("val "+ val[j]);
                distances[i][j] = Double.parseDouble(val[j]);
            }
            i++;
        }
        String filename = (new File(path)).getName();
        return new Instance(nbActions, distances, costsOrig, relevancesOrig, time, distance, filename);
    }
}
