package com.motor.script.core.antlr4.impl;

import java.util.Objects;
import java.util.Optional;

public class CtrlOptional {
    public CtrlFlag flag;
    public Object data;
    public String target;

    public static CtrlOptional run = new CtrlOptional();

    public CtrlOptional() {
        flag = CtrlFlag.ctrl_run;
    }

    public CtrlOptional(CtrlFlag flag) {
        this.flag = flag;
    }

    public CtrlOptional(CtrlFlag flag, Object data) {
        this.flag = flag;
        this.data = data;
    }

    public CtrlOptional(CtrlFlag flag, Object data, String target) {
        this.flag = flag;
        this.data = data;
        this.target = target;
    }

    public <T> Optional<T> optional(Class<T> clazz){
        return Optional.ofNullable((T)data);
    }
    public Optional optional(){
        return Optional.ofNullable(data);
    }

    public boolean is(CtrlFlag ctrl){
        return Objects.equals(ctrl, flag);
    }

}
