package com.lab240.utils;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    public static <T> boolean equals(List<T> one, List<T> two){
        if(one.size() != two.size())
            return false;
        for(int i = 0; i < one.size(); i++){
            if(!equals(one.get(i),two.get(i)))
                return false;
        }
        return true;
    }

    public static <T> boolean equals(Set<T> one, Set<T> two){
        if(one.size() != two.size())
            return false;
        for(Iterator<T> it1 = one.iterator(), it2 = two.iterator(); it1.hasNext() && it2.hasNext();){
            if(!equals(it1.next(), it2.next()))
                return false;
        }
        return true;
    }

    public static boolean equals(Object one, Object two){
        if(one == null || two == null){
            return one == two;
        }
        return one.equals(two);
    }
}
