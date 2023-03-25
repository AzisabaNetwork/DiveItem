package com.flora30.diveitem.util;

public class NumberUtil {
    public static int parseInt(String str, int def){
        try{
            return Integer.parseInt(str);
        } catch (NumberFormatException e){
            return def;
        }
    }
}
