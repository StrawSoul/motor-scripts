package com.motor.script.core.antlr4.impl;

import java.util.Map;

public class JsonFieldDef implements PropertyOperator {
    Map<String,Object> target;
    String name;

    public JsonFieldDef(Map<String,Object> target, String name) {
        this.target = target;
        this.name = name;
    }

    @Override
    public void set(Object value) {
        target.put(name,value);
    }

    @Override
    public Object get() {
        return target.get(name);
    }
}
