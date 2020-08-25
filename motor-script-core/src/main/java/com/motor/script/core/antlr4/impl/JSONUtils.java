package com.motor.script.core.antlr4.impl;

import com.alibaba.fastjson.JSONObject;

public class JSONUtils {

    public static String stringify(Object object){
        return JSONObject.toJSONString(object);
    }
    public static JSONObject parse(String jsonStr){
        return JSONObject.parseObject(jsonStr);
    }
}
