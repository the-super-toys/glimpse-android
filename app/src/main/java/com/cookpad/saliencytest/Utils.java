package com.cookpad.saliencytest;

public class Utils {


    public static void run(float[][][][] input_array, float[][][] output) {
        Class c;
        for(c = input_array.getClass(); c.isArray(); c = c.getComponentType()) {
            ;
        }

        Class<Float> result2 = (Class<Float>) new Float(2.2).getClass();


        if (Float.TYPE.equals(c)) {
             String a = "";
        } else {
            String a = "";
        }

    }
}
