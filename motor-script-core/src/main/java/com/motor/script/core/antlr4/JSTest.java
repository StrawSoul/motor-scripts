package com.motor.script.core.antlr4;

import com.alibaba.fastjson.JSONObject;
import com.ibdp.kanban.calculate.domain.CalculateBaseFunctions;
import com.ibdp.kanban.calculate.domain.javascript.antlr4.impl.ExtendsFunctionDef;
import com.ibdp.kanban.calculate.domain.javascript.antlr4.impl.JavaScriptParserVisitorImpl;
import com.ibdp.kanban.calculate.domain.javascript.antlr4.impl.RuntimeContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;

/**
 * @author: zlj
 * @date: 2019-09-06 下午3:31
 * @description:
 */
public class JSTest {

    public static void main(String[] args) {

        StringBuffer sb = new StringBuffer();
//        sb.append("for(var i =0; i<10 ;i++){if(i==3){break;};print('----'+i);}");
//        sb.append("function a(a,b){\n" +
//                "    var x = 1,y=2;\n" +
//                "    return x+y+a+b;\n" +
//                "    return x+y+a+b+1;\n" +
//                "}\n" +
//                "var a1 = a;\n" +
//                "a1(4,8);");
//        sb.append("(function(a){return a(2);})(x=> x*2)");
//        sb.append("function a(){this.name=1};(function(){return new a();})()");
        sb.append("var sum = 0;for(var i =0 ;i<=1000000; i++){sum += i;}return \"Hello Formula. The Sum Is \"+ sum;");
        CharStream in = CharStreams.fromString(sb.toString());

        JavaScriptLexer javaScriptLexer = new JavaScriptLexer(in);

        CommonTokenStream commonTokenStream = new CommonTokenStream( javaScriptLexer);

        JavaScriptParser parser = new JavaScriptParser(commonTokenStream);
        ParserRuleContext expr = parser.program();
        long startTime = System.currentTimeMillis();
//        System.out.println(JSONObject.toJSONString(expr));
        visitor(expr);
        long endTime = System.currentTimeMillis();
        System.out.println("time "+( endTime - startTime));

    }

    public static void visitor(ParserRuleContext parse){
        String text = parse.getText();
        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setFunctionContextId(runtimeContext.id());
        runtimeContext.put("Number", new ExtendsFunctionDef<Object>((it, args)-> {
            if(CollectionUtils.isEmpty(args)|| args.get(0) == null){
                return null;
            }
            Object o = args.get(0);
            if(o instanceof BigDecimal){
                return o;
            }
            return new BigDecimal(o.toString());
        }, null));
        CalculateBaseFunctions calculateBaseFunctions = new CalculateBaseFunctions();
        runtimeContext.put("$", calculateBaseFunctions);
        JavaScriptParserVisitor visitor = JavaScriptParserVisitorImpl.proxy(runtimeContext);
        Object result = visitor.visit(parse);
        System.out.print("result=");
        System.out.println(JSONObject.toJSONString(result));
    }

}
