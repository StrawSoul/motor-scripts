package com.motor.script.core.antlr4.impl;

import java.lang.reflect.Method;
import java.util.List;

import static com.ibdp.kanban.calculate.utils.FormulaUtil.paramsParseForInvoke;

public class JavaMethodDef {
    public Object target;
    public String methodName;
    public List<Method> methods;

    public JavaMethodDef(Object target, String methodName, List<Method> methods) {
        this.target = target;
        this.methodName = methodName;
        this.methods = methods;
    }

    public JavaMethodDef(String methodName, List<Method> methods) {
        this.methodName = methodName;
        this.methods = methods;
    }

    public JavaMethodDef(List<Method> methods) {
        this.methods = methods;
    }

    public Object invoke(List args){
        int size = args.size();
        for (Method method : methods) {
            int parameterCount = method.getParameterCount();
            if(parameterCount == size){
                try {
                    if(size == 0){
                        method.setAccessible(true);
                        Object value = method.invoke(this.target);
                        return value;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Object[] objects = paramsParseForInvoke(parameterTypes, args);
                    // todo 异常不适合做逻辑处理,这里还是要做合理性判断
                    method.setAccessible(true);
                    return method.invoke(this.target, objects);
                } catch (IllegalArgumentException e){
                    continue;
                } catch (Exception e) {
                    throw new RuntimeException(String.format("method %s invoke failed", methodName),e);
                }
            }
        }
        throw new RuntimeException(String.format("no matcher method %s to invoke ", methodName));
    }

}
