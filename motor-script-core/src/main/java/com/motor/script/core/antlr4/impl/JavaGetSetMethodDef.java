package com.motor.script.core.antlr4.impl;

import java.lang.reflect.Method;
import java.util.List;

public class JavaGetSetMethodDef implements PropertyOperator{
    public Object target;
    public String methodName;
    public List<Method> methods;

    public JavaGetSetMethodDef(Object target, String methodName, List<Method> methods) {
        this.target = target;
        this.methodName = methodName;
        this.methods = methods;
    }

    public JavaGetSetMethodDef(String methodName, List<Method> methods) {
        this.methodName = methodName;
        this.methods = methods;
    }

    public JavaGetSetMethodDef(List<Method> methods) {
        this.methods = methods;
    }

    @Override
    public void set(Object value) {
        for (Method method : methods) {
            int parameterCount = method.getParameterCount();
            if(parameterCount == 1){
                try {
                    method.invoke(this.target,value);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("method %s invoke failed", methodName),e);
                }
            }
        }
    }

    @Override
    public Object get() {
        for (Method method : methods) {
            int parameterCount = method.getParameterCount();
            if(parameterCount == 0){
                try {
                    return method.invoke(this.target);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("method %s invoke failed", methodName),e);
                }
            }
        }
        return null;
    }
}
