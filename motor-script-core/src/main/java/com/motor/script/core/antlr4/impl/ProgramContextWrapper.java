package com.motor.script.core.antlr4.impl;

import com.motor.script.core.antlr4.JavaScriptParser;
import org.antlr.v4.runtime.CharStream;
import org.slf4j.Logger;

public class ProgramContextWrapper {
    private JavaScriptParser.ProgramContext programContext;
    private CharStream charStream;
    private Logger logger;
    private String dataSourceId;
    private String repository;
    private String projectId;

    public ProgramContextWrapper(JavaScriptParser.ProgramContext programContext, CharStream charStream, Logger logger) {
        this.programContext = programContext;
        this.charStream = charStream;
        this.logger = logger;
    }

    public ProgramContextWrapper(JavaScriptParser.ProgramContext programContext, CharStream charStream) {
        this.programContext = programContext;
        this.charStream = charStream;
    }

    public JavaScriptParser.ProgramContext getProgramContext() {
        return programContext;
    }

    public CharStream getCharStream() {
        return charStream;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Logger getLogger() {
        return logger;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
