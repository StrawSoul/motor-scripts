package com.motor.script.core.antlr4.impl;


import com.motor.script.core.model.DSLTemplate;

import java.util.Map;

public class SQLDef {
    private String name;
    private String template;

    public SQLDef(String name, String template) {
        this.name = name;
        this.template = template;
    }

    public DSLTemplate build(Map<String,Object> params){
        return new DSLTemplate( template, params);
    }

    public SQLDef append(SQLDef anotherSQLDef){
        return new SQLDef(null, template + "\n" + anotherSQLDef.template);
    }
}
