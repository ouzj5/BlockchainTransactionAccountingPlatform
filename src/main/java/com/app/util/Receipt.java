package com.app.util;

public class Receipt {
    public String from;
    public String to;
    public String fromAdd;
    public String toAdd;
    public int origin;
    public int mount;
    public String state;
    public Receipt(String f, String t, String fa, String ta, int origin, int mount, String state) {
        this.from = f;
        this.to = t;
        this.fromAdd = fa;
        this.toAdd = ta;
        this.origin = origin;
        this.mount = mount;
        this.state = state;
    }
}
