package com.motor.script.core.antlr4.impl;

import com.motor.script.core.antlr4.JavaScriptParserVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

public interface JavaScriptParserVisitorForValue extends JavaScriptParserVisitor {

    public Object visitValue(ParseTree tree);
}
