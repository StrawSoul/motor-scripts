package com.motor.script.core.antlr4.impl;

public class PropertyOperatorWrapper implements PropertyOperator {
    private PropertyOperator target;

    public PropertyOperatorWrapper(PropertyOperator target) {
        this.target = target;
    }

    @Override
    public void set(Object value) {

    }

    @Override
    public Object get() {
        return null;
    }
}
