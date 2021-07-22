package com.lab240.utils;

public class Comparator {
    public static <T extends Comparable<T>> int compare(T[] one, T[] two){
        int res;
        int i = 0;
        do{
            res = one[i].compareTo(two[i]);
            i++;
        }while (i < one.length && i < two.length && res == 0);
        return 0;
    }
}
