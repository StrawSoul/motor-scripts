package com.motor.script.core.antlr4.impl;

import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Objects;

import static org.springframework.util.ObjectUtils.isEmpty;

public class ParameterOptional {
    private String name;
    private Map<String,Object> data;

    public ParameterOptional(String name, Map<String, Object> data) {
        this.name = name;
        this.data = data;
    }

    public boolean isEmpty(){
        return ObjectUtils.isEmpty(data) || ObjectUtils.isEmpty(data.get(name));
    }

    public Object value(){
        return data.get(name);
    }
    public Object orElse(Object value){
        Object v = data.get(name);
        if (v == null || Objects.equals("", v)) {
            return value;
        }
        return v;
    }

}
