package com.motor.script.core.antlr4.impl;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDef extends HashMap<String,Object>{

    public static final Map<String,Object> prototype = new HashMap<>();
    static {
//        prototype.put("toString")
    }
    RuntimeContext runtimeContext;

    public ClassDef(RuntimeContext runtimeContext,String name,String type) {
        this.runtimeContext = new RuntimeContext(runtimeContext);
        put("name", name);
        put("__type__", type);
        put("prototype", new HashMap<>(prototype));
    }

    public  Map<String,Object>  newInstance(List<Object> params){
        Map<String,Object> jsonObject = new JSONObject();
        FunctionDef constructor = (FunctionDef) runtimeContext.get("constructor");
        Map<String,Object> apply = (Map<String,Object>) constructor.apply(jsonObject, params);
        return apply == null ? jsonObject: apply;
    }
}
