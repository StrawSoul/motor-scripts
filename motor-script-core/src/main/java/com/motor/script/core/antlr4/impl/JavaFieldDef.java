package com.motor.script.core.antlr4.impl;

import java.lang.reflect.Field;

public class JavaFieldDef implements PropertyOperator{
    public Object target;
    public String fieldName;
    public Field field;

    public JavaFieldDef(Object target, String fieldName, Field field) {
        this.target = target;
        this.fieldName = fieldName;
        this.field = field;
    }

    public JavaFieldDef(String fieldName, Field field) {
        this.fieldName = fieldName;
        this.field = field;
    }

    public JavaFieldDef(Field field) {
        this.field = field;
    }

    @Override
    public void set(Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("set field %s failed", field),e);
        }
    }

    @Override
    public Object get() {
        try {
           return field.get(target);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(String.format("get field %s failed", field),e);
        }
    }
}
