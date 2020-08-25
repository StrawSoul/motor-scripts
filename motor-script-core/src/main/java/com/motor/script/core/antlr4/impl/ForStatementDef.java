package com.motor.script.core.antlr4.impl;


import com.ibdp.kanban.calculate.domain.javascript.antlr4.JavaScriptParser;

public class ForStatementDef {

    RuntimeContext runtimeContext;
    JavaScriptParser.ExpressionSequenceContext condition;
    JavaScriptParser.ExpressionSequenceContext change;
    JavaScriptParser.BlockContext block;
}
