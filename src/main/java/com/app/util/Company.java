package com.app.util;

import com.app.constants.StateType;

public class Company {
    public String name;
    public String add;
    public String property;
    public String credit;
    public String address;
    public Company(String name, String add, String property, String credit, String address) {
        this.name = name;
        this.add = add;
        this.property = property;
        this.credit = credit;
        this.address = address;
    }
}
