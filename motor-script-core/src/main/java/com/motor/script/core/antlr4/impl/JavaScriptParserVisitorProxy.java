package com.motor.script.core.antlr4.impl;

import com.motor.script.core.antlr4.JavaScriptParserVisitor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class JavaScriptParserVisitorProxy implements InvocationHandler {
    private JavaScriptParserVisitor visitor;

    public JavaScriptParserVisitorProxy(JavaScriptParserVisitor visitor) {
        this.visitor = visitor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String name = method.getName();
        try {
            return method.invoke(visitor, args);
        } catch (Throwable e){
            if(name.length()> 5 && name.startsWith("visit") && args.length == 1 && args[0] instanceof ParserRuleContext) {
                ParserRuleContext ruleContext = (ParserRuleContext) args[0];
                int startIndex = ruleContext.getStart().getStartIndex();
                int stopIndex = ruleContext.getStop().getStopIndex();
                CharStream inputStream = ruleContext.getStart().getInputStream();
                String text = inputStream.getText(new Interval(startIndex, stopIndex));
                throw new RuntimeException(text, e);
            }else{
                throw new RuntimeException("执行异常:"+name,e);
            }
        }
    }

}
