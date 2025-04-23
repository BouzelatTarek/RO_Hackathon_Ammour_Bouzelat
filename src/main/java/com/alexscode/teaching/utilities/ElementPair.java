package com.alexscode.teaching.utilities;

import lombok.Getter;

/**
 * Allows to sort double values in a collection while preserving their original indexes
 */
public class ElementPair implements Comparable<Element> {

    @Getter
    public Pair p;
    @Getter
    public double value;

    public ElementPair(Pair p, double value){
        this.p = p;
        this.value = value;
    }

    public int compareTo(Element e) {
        return Double.compare(this.value, e.value);
    }

    @Override
    public String toString() {
        return "structs.Element{" +
                "Pair=" + p +
                ", value=" + value +
                '}';
    }
}
