package com.altimetrik.adf.Util;

import java.util.ArrayList;

/**
 * Created by icabanas on 8/5/15.
 */
public class MathUtils {

    //Greatest common factor between two numbers
    public static Double gcd(Double a, Double b)
    {
        while (b > 0)
        {
            Double temp = b;
            b = a % b; // % is remainder
            a = temp;
        }
        return a;
    }

    public static Double gcd(ArrayList<Double> input)
    {
        Double result = input.get(0);
        for(int i = 1; i < input.size(); i++) result = gcd(result, input.get(i));
        return result;
    }

}
