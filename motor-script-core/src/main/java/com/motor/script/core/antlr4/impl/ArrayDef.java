package com.motor.script.core.antlr4.impl;

import java.util.List;

public class ArrayDef implements PropertyOperator {
    List target;
    Integer index;

    public ArrayDef(List target, Integer index) {
        this.target = target;
        this.index = index;
    }

    @Override
    public void set(Object value) {
        while (target.size()<= index){
            target.add(null);
        }
        target.set(index,value);
    }

    @Override
    public Object get() {
        return target.get(index);
    }
}
