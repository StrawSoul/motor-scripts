package com.motor.script.core.antlr4.impl;

import com.ibdp.kanban.calculate.domain.javascript.antlr4.JavaScriptParser;
import com.ibdp.kanban.calculate.domain.javascript.antlr4.JavaScriptParserBaseListener;
import org.antlr.v4.runtime.misc.Interval;

public class JavaScriptParserListenerImpl extends JavaScriptParserBaseListener {

    @Override
    public void enterIfStatement(JavaScriptParser.IfStatementContext ctx) {
        super.enterIfStatement(ctx);
    }


    @Override
    public void enterSqlBody(JavaScriptParser.SqlBodyContext ctx) {
        Interval source = ctx.getSourceInterval();

        super.enterSqlBody(ctx);
    }
}
