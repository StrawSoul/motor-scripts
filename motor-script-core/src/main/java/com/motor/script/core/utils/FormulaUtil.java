package com.motor.script.core.utils;

import com.alibaba.fastjson.JSONObject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zlj
 * @date: 2019-05-21 上午11:58
 * @description:
 */
public class FormulaUtil {

    private final static Pattern p = Pattern.compile("(\\[[^\\]]*\\])");

    public static Pattern compile = Pattern.compile("(#)([a-zA-Z0-9]+)");
    public static String formatFormulaStr(String formula, Map<String,Object> params){

        String calculate = formula;
        Matcher matcher = compile.matcher(formula);
        if (matcher.find()) {
            do{
                String group = matcher.group(0);
                String key = matcher.group(2);
                Object value = params.get(key);
                if(value != null){
                    calculate = calculate.replace(group,value.toString());
                }
            }while (matcher.find());
        }
        return calculate;
    }

    public static Object[] paramsParseForInvoke( Class[] parameterTypes , List<Object> args){
        Object[] params = new Object[args.size()];
        for (int i = 0; i < parameterTypes.length; i++) {
            Class type = parameterTypes[i];
            Object item = args.get(i);

            if(item == null || type.isInstance(item)){
                params[i]= item;
            } else if(type.isAssignableFrom(String.class)){
                if(item instanceof Map){
                    params[i] = JSONObject.toJSONString(item);
                } else{
                    params[i] = item.toString();
                }

            } else if(type.isAssignableFrom(Number.class)){
                try {
                    params[i] = type.getMethod("valueOf", type).invoke(null, item.toString());
                } catch (Exception e) {
                    break;
                }
            } else if(item instanceof BigDecimal){
                if(Objects.equals(type.getName(), "int")){
                   params[i] = ((BigDecimal)item).intValue();
                } else
                if(Objects.equals(type.getName(), "long")){
                    params[i] = ((BigDecimal)item).longValue();
                } else
                if(Objects.equals(type.getName(), "double")){
                    params[i] = ((BigDecimal)item).doubleValue();
                } else
                if(Objects.equals(type.getName(), "byte")){
                    params[i] = ((BigDecimal)item).byteValue();
                } else
                if(Objects.equals(type.getName(), "short")){
                    params[i] = ((BigDecimal)item).shortValue();
                }

            } else if(item instanceof JSONObject){
                params[i] = ((JSONObject) item).toJavaObject(type);
            } else if(item instanceof List){
                if (type.isArray()) {
                    params[i] = ((List) item).toArray();
                }
                if(type.isAssignableFrom(List.class)){
                    params[i] = item;
                }
            }
        }
        return params;
    }


}
