package com.motor.script.core.antlr4.impl;

import org.antlr.v4.runtime.ParserRuleContext;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;
import java.util.Map;

public class FunctionDef {
    public boolean async;
    public String name;
    public RuntimeContext runtimeContext;
    public ParserRuleContext functionBodyContext;

    public List args;

    public FunctionDef(RuntimeContext runtimeContext, ParserRuleContext functionBodyContext) {

        this.runtimeContext = runtimeContext;
        this.functionBodyContext = functionBodyContext;

    }

    public Object apply(Object obj, List args){
        return invoke(obj,args );
    }
    public Object invoke(Object obj, List args){
        int size = CollectionUtils.isEmpty(args)? 0 :args.size();
        FunctionDef f = this;
        RuntimeContext runtimeContext = new RuntimeContext(f.runtimeContext);
        runtimeContext.setFunctionContextId(runtimeContext.id());
        runtimeContext.put("arguments", args);
        if(obj != null){
            runtimeContext.put("this", obj);
        }
        for (Map.Entry<String, Object> entry : f.runtimeContext.entrySet()) {
            String key = entry.getKey();
            Object v = entry.getValue();
            if(v instanceof  ArgsRef){
                int index = ((ArgsRef) v).index;
                runtimeContext.put(key, index>(size-1)? null : args.get(index));
            }
        }
        Object result = JavaScriptParserVisitorImpl.proxy(runtimeContext).visitValue(functionBodyContext);
        return result;
    }

    public Object invoke(Object obj){
        return this.apply(obj, args);
    }
    public void bindArgs(List args){
        this.args = args;
    }

}
