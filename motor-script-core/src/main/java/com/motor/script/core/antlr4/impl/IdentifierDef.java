package com.motor.script.core.antlr4.impl;

public class IdentifierDef implements PropertyOperator {

    private RuntimeContext runtimeContext;
    private String name;

    public IdentifierDef(RuntimeContext runtimeContext, String name) {
        this.runtimeContext = runtimeContext;
        this.name = name;
    }

    @Override
    public void set(Object value) {
        value = parse(value);
        runtimeContext.findAndPut(name,value);
    }

    @Override
    public Object get() {
        return runtimeContext.findValue(name);
    }

    private Object parse(Object value){
        if (value instanceof PropertyOperator) {
            Object o = ((PropertyOperator) value).get();
            return parse(o);
        }
        return value;
    }
}
