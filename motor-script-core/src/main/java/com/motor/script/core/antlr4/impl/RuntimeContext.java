package com.motor.script.core.antlr4.impl;

import com.ibdp.kanban.calculate.utils.FormulaUtil;
import jdk.nashorn.internal.runtime.Undefined;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RuntimeContext  {

    public RuntimeContext parent;
    private String id;
    private String functionContextId;
    public Map<String,Object> context = new HashMap();


    public String getFunctionContextId() {
        return functionContextId;
    }
    public RuntimeContext(RuntimeContext parent) {
        this.id = FormulaUtil.md5("RuntimeContext");
        this.parent = parent;
        this.functionContextId = parent.functionContextId;
    }
    public RuntimeContext(String id, RuntimeContext parent) {
        this.id = id;
        this.parent = parent;
        this.functionContextId = parent.functionContextId;
    }

    public String id(){
        return id;
    }


    public void setFunctionContextId(String functionContextId) {
        this.functionContextId = functionContextId;
    }

    public RuntimeContext() {

        this.id = FormulaUtil.md5("RuntimeContext");
        this.functionContextId = this.id;
    }
    public RuntimeContext(String id) {
        this.id = id;
        this.functionContextId = this.id;
    }


    public RuntimeContext parentTop(){
        return  parent == null ? this: parent.parentTop();
    }
    public void findAndPut(String key, Object value){
        RuntimeContext runtimeContext = null;
        if(context.containsKey(key)){
            runtimeContext = this;
        } else{
            runtimeContext = parentOfContainsKey(key);
            runtimeContext = runtimeContext == null ? this: runtimeContext;
        }
        runtimeContext.put(key,value);
    }
    public void findAndPut(Map<String,Object> map){
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            this.findAndPut(entry.getKey(),entry.getValue());
        }
    }

    private RuntimeContext parentOfContainsKey(String key){
        if(parent == null){
            return null;
        } else if(parent.containsKey(key)){
            return parent;
        }else{
            return parent.parentOfContainsKey(key);
        }
    }
    public Object findValue(String key){
        Object value = null;
        if (this.containsKey(key)) {
            value = context.get(key);
        } else if(parent == null){
            value = Undefined.getUndefined();
        } else {
            value =  parent.findValue(key);
        }
        if(value instanceof ArgsRef){
            int index = ((ArgsRef) value).index;
            value = ((List)context.get("arguments")).get(index);
        }
        return value;
    }

    public void put(String k,Object v){
        context.put(k,v);
    }
    public boolean containsKey(String key){
        return context.containsKey(key);
    }

    public void putAll(Map<String, Object> params) {
        context.putAll(params);
    }

    public Object get(String key) {
        return context.get(key);
    }

    public Set<Map.Entry<String, Object>> entrySet() {
        return context.entrySet();
    }

}
