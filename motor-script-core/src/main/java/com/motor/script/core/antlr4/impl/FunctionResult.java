package com.motor.script.core.antlr4.impl;

public class FunctionResult {
    public FunctionDef functionDef;
    public Object result;

    public FunctionResult(FunctionDef functionDef, Object result) {
        this.functionDef = functionDef;
        this.result = result;
    }
}
