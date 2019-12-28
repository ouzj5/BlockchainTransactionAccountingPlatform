package com.app.constants;

public class StateType {
    public static final int UNKNOW = 0;
    public static final int CREDIBLE = 1;
    public static final int DISABLE = 2;
    public static final int LOAN = 3;
    public static final int PAYBACK = 4;
    public static final int BANK = 5;
    public static String stringValue(int state){
        if (state == UNKNOW){
            return "unknow";
        } else if (state == CREDIBLE) {
            return "crediable";
        } else if (state == DISABLE) {
            return "disable";
        } else if (state == LOAN) {
            return "loan";
        } else if (state == PAYBACK) {
            return "payback";
        } else if (state == BANK) {
            return "bank";
        }
        return "null";
    }
}
