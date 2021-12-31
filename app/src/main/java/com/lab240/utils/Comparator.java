package com.lab240.utils;

import java.util.List;

public class Comparator {

    public static <T extends Comparable<T>> int compare(List<T> one, List<T> two){
        if(one.size() != two.size())
            return compare(one.size(), two.size());
        for(int i = 0; i < one.size(); i++){
            int res = compare(one.get(i),two.get(i));
            if(res != 0)
                return res;
        }
        return 0;
    }

    public static <T extends Comparable<T>> int compare(T one, T two){
        if(one == null){
            if(two == null)
                return -1;
            return 0;
        }
        if(two == null){
            return 1;
        }
        return one.compareTo(two);
    }
}
