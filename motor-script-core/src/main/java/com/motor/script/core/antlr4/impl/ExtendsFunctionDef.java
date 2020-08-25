package com.motor.script.core.antlr4.impl;

import java.util.List;
import java.util.function.BiFunction;

public class ExtendsFunctionDef<T>{

    public BiFunction function;
    public Object target;

    public ExtendsFunctionDef(BiFunction<T, List,?> function, Object target) {
        this.function = function;
        this.target = target;
    }

    public Object invoke(List args){
        return function.apply(target, args);
    }

}
