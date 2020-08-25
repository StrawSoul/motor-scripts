package com.motor.script.core.antlr4.impl;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.ObjectUtils.isEmpty;

public class Parameters {
    private Map<String,Object> data;
    private Map<String,Object> sourceData;

    public Parameters(Map<String, Object> data) {
        this.sourceData = data == null ? new HashMap<>() : new HashMap<>(data);
        this.data = new HashMap<>();
    }
    public Parameters put(String key, Object value){
        this.data.put(key,value);
        return this;
    }
    public Parameters peek(String key){
        if(sourceData.containsKey(key)&& !isEmpty(sourceData.get(key))){
            data.put(key, sourceData.get(key));
        }
        return this;
    }
    public Parameters peek(String key, String newKey){
        if(sourceData.containsKey(key)&& !isEmpty(sourceData.get(key))){
            data.put(newKey, sourceData.get(key));
        }
        return this;
    }
    public Parameters peek(String key, String newKey, Object defValue){
        this.data.put(newKey, this.sourceData.getOrDefault(key, defValue));
        return this;
    }
    public Parameters clone(){
        return  new Parameters(this.sourceData);
    }

    public Map<String,Object> get(){
        return  this.data;
    }
}
