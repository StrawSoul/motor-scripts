package com.motor.script.core.antlr4.impl;

public class JsFunctionOperator implements PropertyOperator {
    public FunctionDef functionDef;
    public Object target;
    public Object result;

    public JsFunctionOperator(FunctionDef functionDef, Object target) {
        this.functionDef = functionDef;
        this.target = target;
        this.result = functionDef.invoke(target);
    }

    @Override
    public void set(Object value) {
        throw new RuntimeException("function can`t set value");
    }

    @Override
    public Object get() {
        return result;
    }
}
