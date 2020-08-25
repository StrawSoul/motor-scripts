package com.motor.script.core.antlr4;

import com.motor.common.message.command.Command;
import com.motor.script.core.antlr4.impl.*;
import com.motor.script.core.MotorScriptsRepository;
import com.motor.script.core.model.ScriptEntity;
import org.antlr.v4.runtime.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class MotorScriptEval {

    private static Logger logger = LoggerFactory.getLogger(MotorScriptEval.class);

    private RuntimeContext runtimeContext;

    private JavaScriptParserVisitor visitor;

    private MotorScriptsRepository motorScriptsRepository;

    public MotorScriptEval(MotorScriptsRepository motorScriptsRepository){
        runtimeContext = new RuntimeContext();
        runtimeContext.setFunctionContextId(runtimeContext.id());
        visitor = JavaScriptParserVisitorImpl.proxy(runtimeContext);
        this.motorScriptsRepository = motorScriptsRepository;
    }
    public void loadScriptFile(String name) {
        motorScriptsRepository.findByName("$", name);
    }
    public Object eval(Command<ScriptEntity> cmd) {
        return eval(cmd, null);
    }

    public Object eval(Command<ScriptEntity> scriptText, Map<String, Object> params) {
        if(!StringUtils.isEmpty(scriptText.traceId())){
            MDC.put("traceId", scriptText.traceId() );
        }
        return visit(buildContext(scriptText), params);
    }
    private Map<String, ProgramContextWrapper> exprCache = new HashMap<>();

    /**
     * 把解析过的脚本对象缓存起来,同样的脚本只解析一次
     * 但是如果同一个脚本更改次数过多,会导致exprCache 存在很多再也用不到的数据
     * todo 需要处理成按照函数ID缓存,然后调用时比较脚本内容,相同则直接使用缓存对象,不同则重新解析并更新缓存
     * @param cmd
     * @return
     */
    public ProgramContextWrapper buildContext(Command<ScriptEntity> cmd){
        ScriptEntity scriptText = cmd.data();

        ProgramContextWrapper expr = exprCache.computeIfAbsent(scriptText.fullName(), e->{
            CharStream charStream = CharStreams.fromString(scriptText.getBody());
            CommonTokenStream commonTokenStream = new CommonTokenStream( new JavaScriptLexer(charStream));
            JavaScriptParser parser = new JavascriptParserWrapper(commonTokenStream);
            JavaScriptParser.ProgramContext program = parser.program();
            Logger log = LoggerFactory.getLogger("calculate.calculator.script.formula-" + scriptText.fullName());
            ProgramContextWrapper wrapper = new ProgramContextWrapper(program, charStream, log);
            return wrapper;
        });
        return expr;
    }
    public Object visit(ProgramContextWrapper wrapper, Map<String,Object> params){
        JavaScriptParser.ProgramContext programContext = wrapper.getProgramContext();
        RuntimeContext runtimeContext = new RuntimeContext(this.runtimeContext);
        Logger log = Optional.ofNullable(wrapper.getLogger()).orElse(this.logger);
        runtimeContext.put("$template", wrapper.getCharStream());
        runtimeContext.put("logger", log);
        runtimeContext.setFunctionContextId(runtimeContext.getFunctionContextId());
        runtimeContext.put("$params", new Parameters(params));
        runtimeContext.put("$prop", (Function<String, ParameterOptional>) name -> new ParameterOptional(name, params));
        Map<String,Object> requestContext =  new HashMap<>(1);
        requestContext.put("$projectId", wrapper.getProjectId());
        runtimeContext.put("$request", requestContext);
        if(params != null && params.size()>0){
            runtimeContext.putAll(params);
        }
        JavaScriptParserVisitor visitor = JavaScriptParserVisitorImpl.proxy(runtimeContext);
        Object result = visitor.visit(programContext);
        return result;
    }

}
