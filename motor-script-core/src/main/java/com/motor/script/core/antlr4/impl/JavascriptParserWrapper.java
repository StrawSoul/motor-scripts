package com.motor.script.core.antlr4.impl;

import com.motor.script.core.antlr4.JavaScriptParser;
import org.antlr.v4.runtime.TokenStream;

public class JavascriptParserWrapper extends JavaScriptParser {
    public JavascriptParserWrapper(TokenStream input) {
        super(input);
    }
}
