package com.motor.script.core.antlr4.impl;

import com.alibaba.fastjson.JSONObject;
import com.ibdp.persistent.resource.utils.ColumnUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaFunctions {

    public static Class type(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
    public static JSONObject toJson(Object obj){
        JSONObject map = new JSONObject();
        if(obj == null){
            return map;
        }
        Class clazz = null;
        if(obj instanceof Class){
            clazz = (Class)obj;
        }else{
            clazz = obj.getClass();
        }

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if ( methodName.startsWith("get")||methodName.startsWith("set")){
                methodName = ColumnUtils.convertHump(methodName.substring(3));
            }
            if (!map.containsKey(methodName)) {
                map.put(methodName, new ArrayList<>());
            }
            ((List)map.get(methodName)).add(method);
        }
        JSONObject json = new JSONObject();
        map.forEach((k,v)->{
            List<Method> methodList = (List<Method>)v;
            json.put(k, new JavaMethodDef(obj, k,methodList));
        });
        return json;
    }
}
