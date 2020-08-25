package com.motor.script.core.antlr4.impl;

import com.motor.script.core.antlr4.JavaScriptParserVisitor;
import com.motor.script.core.model.DSLTemplate;
import com.motor.script.core.model.TransactionContext;
import com.motor.script.core.antlr4.JavaScriptParser;
import com.motor.script.core.antlr4.JavaScriptParserBaseVisitor;
import jdk.nashorn.internal.runtime.Undefined;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.motor.script.core.utils.FormulaUtil.paramsParseForInvoke;

/**
 * js代码解释器,使用js语法但没有完全实现js的复杂特性
 * 支持基础的数学运算,集合&对象的属性的访问,循环,判断,函数,匿名函数,箭头函数等等
 * 底层使用java对象,但支持对java对象扩展方法
 * @author zlj
 * */
public class JavaScriptParserVisitorImpl extends JavaScriptParserBaseVisitor implements JavaScriptParserVisitorForValue{
    private static final Logger logger = LoggerFactory.getLogger(JavaScriptParserVisitorImpl.class);
    private RuntimeContext rctx;

    protected JavaScriptParserVisitorImpl(RuntimeContext rctx) {
        this.rctx = rctx;
    }

    @Override
    public Object visitClassExpression(JavaScriptParser.ClassExpressionContext ctx) {
        return super.visitClassExpression(ctx);
    }

    /**
     * 正表达式如 +1
     * @param ctx
     * @return
     */
    @Override
    public Object visitUnaryPlusExpression(JavaScriptParser.UnaryPlusExpressionContext ctx) {
        return super.visitUnaryPlusExpression(ctx);
    }


    /**
     * 加法减法表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitAdditiveExpression(JavaScriptParser.AdditiveExpressionContext ctx) {
        Object aObj = this.visitValue(ctx.singleExpression(0));
        Object bObj = this.visitValue(ctx.singleExpression(1));
        if(aObj instanceof BigDecimal && bObj instanceof BigDecimal){
            BigDecimal a = (BigDecimal)aObj;
            BigDecimal b = (BigDecimal)bObj;
            if(ctx.Plus()!= null){
                return  a.add(b);
            } else {
                return  a.subtract(b);
            }
        } else {
            return Objects.toString(aObj)+Objects.toString(bObj);
        }
    }

    /**
     *  负 表达式, 如 -1,  -(x*y)
     * @param ctx
     * @return
     */
    @Override
    public Object visitUnaryMinusExpression(JavaScriptParser.UnaryMinusExpressionContext ctx) {
        Object o = this.visitValue(ctx.singleExpression());
//        return super.visitUnaryMinusExpression(ctx);
        return null;
    }

    private Logger logger(){
        return (Logger)rctx.findValue("logger");
    }
    /**
     * 乘法出发表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitMultiplicativeExpression(JavaScriptParser.MultiplicativeExpressionContext ctx) {
        try{
            BigDecimal a = (BigDecimal)this.visitValue(ctx.singleExpression(0));
            BigDecimal b = (BigDecimal)this.visitValue(ctx.singleExpression(1));
            if(a == null || b == null){
                logger().error(String.format("乘法参数有空值 %s", ctx.getText()));
                return NaNEnum.NaN;
            }

                if(ctx.Divide()!= null){
                    return  a.divide(b, 2,BigDecimal.ROUND_HALF_UP);
                }
                if(ctx.Multiply()!= null ){
                    return  a.multiply(b);
                }
                if(ctx.Modulus()!= null){
                    return a.divideAndRemainder(b)[1];
                }
        } catch (Exception e){
            logger().error("计算异常 {} {}", ctx.getText(),e.getMessage());
            return NaNEnum.NaN;
        }

            return null;
    }

    /**
     *  由于数据格式的不确定和开发速度考虑,所有数字统一用BigDecimal处理,
     *  但这可能会影响执行速度
     * @param ctx
     * @return
     */
    @Override
    public Object visitNumericLiteral(JavaScriptParser.NumericLiteralContext ctx) {
        String text = ctx.DecimalLiteral().getText();
        return new BigDecimal(text);
    }


    /**
     * 解析文字,如  null , true , false, 1 2 3 等等
     * @param ctx
     * @return
     */
    @Override
    public Object visitLiteral(JavaScriptParser.LiteralContext ctx) {
        if(ctx.BooleanLiteral() != null){
            return Objects.equals(ctx.BooleanLiteral().getText(), "true");
        }
        if(ctx.StringLiteral() != null){
            String str = ctx.StringLiteral().getText();
            return str.substring(1,str.length()-1);
        }
        if(ctx.RegularExpressionLiteral() != null){
            return Pattern.compile(ctx.RegularExpressionLiteral().getText());
        }
        if(ctx.NullLiteral() != null){
            return null;
        }
        return super.visitLiteral(ctx);
    }

    @Override
    public Object visitLiteralExpression(JavaScriptParser.LiteralExpressionContext ctx) {
        Object o = super.visitLiteralExpression(ctx);
        return o;
    }

    @Override
    public Object visitExpressionSequence(JavaScriptParser.ExpressionSequenceContext ctx) {

        Object o = super.visitExpressionSequence(ctx);
        return o;
    }

    @Override
    public Object visitExpressionStatement(JavaScriptParser.ExpressionStatementContext ctx) {
        Object o = super.visit(ctx.expressionSequence());
        return o;
    }

    /**
     * 访问表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitStatement(JavaScriptParser.StatementContext ctx) {
        try{
            Object o = super.visitStatement(ctx);
            return o;
        }catch (ArithmeticException e){
            logger().error("数据异常,可能使用了非数字项进行计算,或者除数为0,返回NaN  {}",ctx.getText(),e);
            return NaNEnum.NaN;
        }
    }

    /**
     * 访问代码快
     * @param ctx
     * @return
     */
    @Override
    public Object visitBlock(JavaScriptParser.BlockContext ctx) {
        RuntimeContext runtimeContext = new RuntimeContext(rctx);
        Object res = JavaScriptParserVisitorImpl.proxy(runtimeContext).visit(ctx.statementList());
        return res;
    }

    @Override
    public Object visitProgram(JavaScriptParser.ProgramContext ctx) {
        JavaScriptParser.SourceElementsContext sourceElementsContext = ctx.sourceElements();
        Object o = this.visitValue(sourceElementsContext);
        if(o instanceof CtrlOptional){
            CtrlOptional ctrl = (CtrlOptional) o;
            if (Objects.equals(ctrl.target, rctx.id())) {
                if(ctrl.is(CtrlFlag.ctrl_break)){
                    return CtrlOptional.run;
                }
                if(ctrl.is(CtrlFlag.ctrl_continue)){
                    return CtrlOptional.run;
                }
                if(ctrl.is(CtrlFlag.ctrl_return)){
                    if(rctx.containsKey("return")){
                        return rctx.get("return");
                    }else{
                        return ctrl.data;
                    }
                }
            }
            return ctrl.data;
        }
        return o;
    }

    /**
     * 给已经存在的变量赋值
     * @param ctx
     * @return
     */
    @Override
    public Object visitAssignmentExpression(JavaScriptParser.AssignmentExpressionContext ctx) {
        JavaScriptParser.SingleExpressionContext singleExpressionContext = ctx.singleExpression(0);
        String text = singleExpressionContext.getText();
        Object visit = this.visit(ctx.singleExpression(0));
        if(visit instanceof PropertyOperator){
            PropertyOperator operator = (PropertyOperator) visit;
            Object value = this.visitValue(ctx.singleExpression(1));
            operator.set(value);
        } else if(visit instanceof FunctionDef){
            String text1 = ((FunctionDef) visit).functionBodyContext.getText();
            logger().warn("function cannot set value :"+text1);
        }
        return null;
    }

    /**
     * 变量的声明表达式如 var a 或者  var a = 1
     * @param ctx
     * @return
     */
    @Override
    public Object visitVariableDeclaration(JavaScriptParser.VariableDeclarationContext ctx) {
        JavaScriptParser.AssignableContext assignable = ctx.assignable();
        JavaScriptParser.SingleExpressionContext singleExpressionContext = ctx.singleExpression();
        if(singleExpressionContext == null){
            return null;
        }
        Object value = this.visitValue(singleExpressionContext);
        if(assignable.Identifier()!= null){
            String id = assignable.Identifier().getText();
            HashMap<String, Object> variables = new HashMap<String, Object>();
            rctx.findAndPut(id, value);
            variables.put(id, value);
            return variables;
        }
        return super.visitVariableDeclaration(ctx);
    }

    /**
     *  连续的定义变量
     * @param ctx
     * @return
     */
    @Override
    public Object visitVariableDeclarationList(JavaScriptParser.VariableDeclarationListContext ctx) {
            Map<String,Object> maps = new HashMap<>();
            for (int i = 0; i < ctx.variableDeclaration().size(); i++) {
                JavaScriptParser.VariableDeclarationContext variableDeclarationContext = ctx.variableDeclaration(i);
                try{
                    Map<String,Object> o = (Map<String,Object>)this.visitValue(ctx.variableDeclaration(i));
                    if(o == null){
                        continue;
                    }
                    maps.putAll(o);
                }catch (Throwable e){
                    throw  new RuntimeException(variableDeclarationContext.getText(),e);
                }
            }
            return maps;
    }

    /**
     * 同时声明多个变量 如  var a=1,b,c=2;
     * @param ctx
     * @return
     */
    @Override
    public Object visitVariableStatement(JavaScriptParser.VariableStatementContext ctx) {
        Map<String,Object> object = (Map<String,Object>)this.visit(ctx.variableDeclarationList());
        if(object == null){
            return null;
        }
        JavaScriptParser.VarModifierContext modifier = ctx.varModifier();
        if (modifier.Const()!=null) {
            rctx.parentTop().putAll(object);
            return null;
        }
        if(modifier.Let()!= null){
            rctx.putAll(object);
            return null;
        }
        if(modifier.Var()!= null){
            rctx.putAll(object);
            return null;
        }

        return object;
    }

    /**
     * 获得变量或者方法的名称
     * @param ctx
     * @return
     */
    @Override
    public Object visitIdentifierExpression(JavaScriptParser.IdentifierExpressionContext ctx) {
        String id = ctx.Identifier().getText();
        return new IdentifierDef(rctx, id);
    }

    @Override
    public Object visitFunctionExpression(JavaScriptParser.FunctionExpressionContext ctx) {
        return super.visitFunctionExpression(ctx);
    }

    /**
     * 得到函数表达式声明
     * @param ctx
     * @return
     */
    @Override
    public Object visitFunctionDeclaration(JavaScriptParser.FunctionDeclarationContext ctx) {
        String name = ctx.Identifier().getText();
        JavaScriptParser.FunctionBodyContext functionBodyContext = ctx.functionBody();
        FunctionDef functionDef = buildFunctionDef(ctx.formalParameterList(), functionBodyContext);
        if(StringUtils.isNotEmpty(name)){
            functionDef.name = name;
            rctx.put(name,functionDef);
        }
        return functionDef;
    }

    /**
     *  匿名函数的声明
     * @param ctx
     * @return
     */
    @Override
    public Object visitAnoymousFunctionDecl(JavaScriptParser.AnoymousFunctionDeclContext ctx) {
        JavaScriptParser.FormalParameterListContext formalParameterListContext = ctx.formalParameterList();
        return buildFunctionDef(formalParameterListContext,ctx.functionBody());
    }

    /**
     * 创建函数对象
     * @param parametersContext
     * @param functionBodyContext
     * @return
     */
    private FunctionDef buildFunctionDef(ParserRuleContext parametersContext, ParserRuleContext functionBodyContext ){
        List args = parametersContext == null ? new ArrayList():(List)this.visit(parametersContext);

        RuntimeContext runtimeContext = new RuntimeContext(rctx);
        runtimeContext.setFunctionContextId(runtimeContext.id());
        for (int i = 0; i < args.size(); i++) {
            String s = args.get(i).toString();
            runtimeContext.put(s, new ArgsRef(i));
        }
        runtimeContext.put("arguments", new ArrayList());
        FunctionDef functionDef = new FunctionDef(runtimeContext, functionBodyContext);
        return functionDef;
    }

    /**
     * 访问函数的方法体, 也就是执行函数
     * @param ctx
     * @return
     */
    @Override
    public Object visitFunctionBody(JavaScriptParser.FunctionBodyContext ctx) {
        Object value = rctx.findValue("this");
        if(value == null){
            value = new HashMap<>();
            rctx.put("this", value);
        }
        Object o = super.visitFunctionBody(ctx);
        if(o instanceof CtrlOptional){
            return o;
        }
        return new CtrlOptional(CtrlFlag.ctrl_return, o, rctx.getFunctionContextId());
    }

    /**
     *  返回值表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitReturnStatement(JavaScriptParser.ReturnStatementContext ctx) {
        Object v = this.visitValue(ctx.singleExpression());
        rctx.put("return", v);
        return new CtrlOptional(CtrlFlag.ctrl_return,v, rctx.getFunctionContextId());
    }

    @Override
    public Object visitBreakStatement(JavaScriptParser.BreakStatementContext ctx) {
        TerminalNode identifier = ctx.Identifier();
        if (identifier != null) {
            return new CtrlOptional(CtrlFlag.ctrl_break,null, identifier.getText());
        }
        return new CtrlOptional(CtrlFlag.ctrl_break,null, rctx.id());
    }

    @Override
    public Object visitContinueStatement(JavaScriptParser.ContinueStatementContext ctx) {
        TerminalNode identifier = ctx.Identifier();
        if (identifier != null) {
            return new CtrlOptional(CtrlFlag.ctrl_continue,null, identifier.getText());
        }
        return new CtrlOptional(CtrlFlag.ctrl_continue,null, rctx.id());
    }

    /**
     *  由上到下依次执行代码段
     * @param ctx
     * @return
     */
    @Override
    public Object visitSourceElements(JavaScriptParser.SourceElementsContext ctx) {
        List<JavaScriptParser.SourceElementContext> elements = ctx.sourceElement();
        Object value = null;
        if(elements.size() ==1){
            value = this.visit(elements.get(0));
            return value;
        } else{

            for (int i = 0; i < elements.size(); i++) {
                JavaScriptParser.SourceElementContext element = elements.get(i);
                value = this.visit(element);
                if(value instanceof CtrlOptional ){
                    CtrlOptional ctrlOptional = (CtrlOptional)value;
                    if (ctrlOptional.is(CtrlFlag.ctrl_return)) {
                        return value;
                    }
                }
            }
        }
        return value;
    }


    /**
     * if 表达式, null, 0 , false 都表示否
     * @param ctx
     * @return
     */
    @Override
    public Object visitIfStatement(JavaScriptParser.IfStatementContext ctx) {
        Object o = this.visitValue(ctx.expressionSequence());
        if(o == null){
            System.out.println(ctx.getText());
        }
        Boolean b = true;
        if(o == null){
            b = false;
        }
        if(o instanceof Boolean){
            b = (Boolean)o;
        } else if(Objects.equals(o,0)){
            b = false;
        }

        if(b){
            return this.visit(ctx.statement(0));
        }else if(ctx.statement().size()>1){
            return this.visit(ctx.statement(1));
        }
        return CtrlOptional.run;
    }

    /**
     * 执行带参数的表达式(也就是函数,包括构造函数)
     * @param ctx
     * @return
     */
    @Override
    public Object visitArgumentsExpression(JavaScriptParser.ArgumentsExpressionContext ctx) {
        String text = ctx.getText();
        JavaScriptParser.ArgumentsContext arguments = ctx.arguments();
        List args = (List)this.visit(arguments);
        Object collect = args.stream().map(e -> parseValue(e)).collect(Collectors.toList());
        JavaScriptParser.SingleExpressionContext singleExpressionContext = ctx.singleExpression();

        Object value = null;
        if (singleExpressionContext instanceof JavaScriptParser.IdentifierExpressionContext) {
            JavaScriptParser.IdentifierExpressionContext identifierExpressionContext = (JavaScriptParser.IdentifierExpressionContext)ctx.singleExpression();
            String name = identifierExpressionContext.Identifier().getText();
            if(Objects.equals(name,"print")){
                logger().info(args.size()>0?args.get(0).toString():"");
                return CtrlOptional.run;
            }
            value = rctx.findValue(name);
        }else{
            value = this.visitValue(ctx.singleExpression());
            if(value instanceof CtrlOptional){
                value = ((CtrlOptional)value).data;
            }
        }
        if(value instanceof FunctionDef){
            FunctionDef f =(FunctionDef)value;
            RuntimeContext runtimeContext = new RuntimeContext(f.runtimeContext.parent);
            runtimeContext.put("logger", rctx.findValue("logger"));
            RuntimeContext frctx = new RuntimeContext(runtimeContext);
            frctx.putAll(f.runtimeContext.context);
            f = new FunctionDef(frctx, f.functionBodyContext);
            f.bindArgs(args);
            return new JsFunctionOperator(f, null);
        }
        if(value instanceof JavaMethodDef){
            return ((JavaMethodDef)value).invoke((List) collect);
        }
        if(value instanceof ExtendsFunctionDef){
            try{
                Object result = ((ExtendsFunctionDef) value).invoke(args);
                return result;
            } catch (Throwable e){
                Logger log = (Logger)rctx.findValue("logger");
                if(log == null){
                    log = logger;
                }
                log.error("扩展方法执行错误", e);
            }
            return  null;
        }
        if(value instanceof SQLDef){
            Object o = args.stream().findFirst().orElse(Collections.EMPTY_MAP);
            DSLTemplate template = ((SQLDef) value).build((Map<String, Object>) o);
            TransactionContext transactionContext = (TransactionContext)rctx.findValue("$transaction");
            return transactionContext.execute(template);
        }
        if(value instanceof Function){
            return ((Function)value).apply(args.get(0));
        }
        return value;
    }

    /**
     *  定义箭头函数, e=> e+1
     * @param ctx
     * @return
     */
    @Override
    public Object visitArrowFunction(JavaScriptParser.ArrowFunctionContext ctx) {
        JavaScriptParser.ArrowFunctionParametersContext parametersContext = ctx.arrowFunctionParameters();
        JavaScriptParser.ArrowFunctionBodyContext bodyContext = ctx.arrowFunctionBody();

        FunctionDef functionDef = buildFunctionDef(parametersContext, bodyContext);
        return functionDef;
    }

    /**
     * 获取箭头函数的参数列表
     * @param ctx
     * @return
     */
    @Override
    public Object visitArrowFunctionParameters(JavaScriptParser.ArrowFunctionParametersContext ctx) {
        if(ctx.formalParameterList()!= null){
            return this.visit(ctx.formalParameterList());
        }
        if(ctx.Identifier()!= null){
            return Arrays.asList(ctx.Identifier().getText());
        }
        return super.visitArrowFunctionParameters(ctx);
    }

    /**
     * 执行箭头函数
     * @param ctx
     * @return
     */
    @Override
    public Object visitArrowFunctionBody(JavaScriptParser.ArrowFunctionBodyContext ctx) {
        Object value = rctx.findValue("this");
        if(value == null){
            value = new HashMap<>();
            rctx.put("this", value);
        }
        if (ctx.singleExpression()!= null) {
            return this.visit(ctx.singleExpression());
        }
        if(ctx.functionBody()!= null){
            return this.visit(ctx.functionBody());
        }
        return null;
    }

    /**
     *  解析参数列表获得参数的值
     * @param ctx
     * @return
     */
    @Override
    public Object visitArguments(JavaScriptParser.ArgumentsContext ctx) {
        List args = new ArrayList();
        if(ctx == null){
            return args;
        }
        List<JavaScriptParser.ArgumentContext> arguments = ctx.argument();
        for (int i = 0; i < arguments.size(); i++) {
            Object v = this.visitValue(arguments.get(i));
            args.add(v);
        }
        return args;
    }

    @Override
    public Object visitArgument(JavaScriptParser.ArgumentContext ctx) {
        return super.visitArgument(ctx);
    }

    /**
     * 解析参数名称
     * @param ctx
     * @return
     */
    @Override
    public Object visitFormalParameterArg(JavaScriptParser.FormalParameterArgContext ctx) {
        return ctx.assignable().Identifier().getText();
    }

    /**
     *  获得参数名称列表
     * @param ctx
     * @return
     */
    @Override
    public Object visitFormalParameterList(JavaScriptParser.FormalParameterListContext ctx) {
        List<JavaScriptParser.FormalParameterArgContext> args = ctx.formalParameterArg();
        List<Object> argsList = new ArrayList<>();
        for (JavaScriptParser.FormalParameterArgContext arg : args) {
            Object visit = this.visit(arg);
            argsList.add(visit);
        }
        return argsList;
    }

    @Override
    public Object visit(ParseTree tree) {
        Object obj = super.visit(tree);
        return obj;
    }
    public Object visitValue(ParseTree tree) {
        Object obj = parseValue(this.visit(tree));
        return obj;
    }

    /**
     * 处理各个参数或表达式的最终结果值
     * @param obj
     * @return
     */
    private Object parseValue(Object obj){

        if (obj instanceof PropertyOperator ) {
            obj = ((PropertyOperator)obj).get();
            return parseValue(obj);
        } else
        if (obj instanceof FunctionResult){
            return ((FunctionResult)obj).result;
        } else
        if (obj instanceof CtrlOptional){
            return ((CtrlOptional)obj).data;
        } else
        if (obj instanceof Number && !(obj instanceof BigDecimal)){
            obj = new BigDecimal(obj.toString());
        }
        return obj;

    }

    /**
     * 比较大小
     * @param ctx
     * @return
     */
    @Override
    public Object visitRelationalExpression(JavaScriptParser.RelationalExpressionContext ctx) {
        Object a = this.visitValue(ctx.singleExpression(0));
        Object b = this.visitValue(ctx.singleExpression(1));
        int compare = compare(a, b);
        if(ctx.GreaterThanEquals()!= null){
            return compare >=0;
        }
        if(ctx.LessThanEquals()!= null){
            return compare <=0;
        }
        if(ctx.LessThan() != null){
            return compare <0;
        }
        if (ctx.MoreThan()!=null) {
            return compare >0;
        }
        return super.visitRelationalExpression(ctx);
    }

    /**
     * 逻辑运算 &&
     * @param ctx
     * @return
     */
    @Override
    public Object visitLogicalAndExpression(JavaScriptParser.LogicalAndExpressionContext ctx) {
        Boolean a = (Boolean) this.visitValue(ctx.singleExpression(0));
        Boolean b = (Boolean) this.visitValue(ctx.singleExpression(1));
        return a&&b;
    }

    @Override
    public Object visitLogicalOrExpression(JavaScriptParser.LogicalOrExpressionContext ctx) {
        Boolean a = (Boolean) this.visitValue(ctx.singleExpression(0));
        Boolean b = (Boolean) this.visitValue(ctx.singleExpression(1));
        return a||b;
    }

    @Override
    public Object visitNotExpression(JavaScriptParser.NotExpressionContext ctx) {
        Boolean a = (Boolean) this.visitValue(ctx.singleExpression());
        return !a;
    }

    /**
     * 带括号的表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitParenthesizedExpression(JavaScriptParser.ParenthesizedExpressionContext ctx) {
        RuntimeContext runtimeContext = new RuntimeContext(rctx);
        runtimeContext.setFunctionContextId(runtimeContext.id());
        Object value = ctx.expressionSequence().accept(JavaScriptParserVisitorImpl.proxy(runtimeContext));
        return value;
    }

    /**
     * 处理for循环
     * @param ctx
     * @return
     *
     * todo 这里应该会有优化的空间, 执行1000000 次累加, 执行时间超过1.5秒
     */
    @Override
    public Object visitForStatement(JavaScriptParser.ForStatementContext ctx) {
        JavaScriptParser.VariableStatementContext var = ctx.variableStatement();
        JavaScriptParser.ExpressionSequenceContext condition = ctx.expressionSequence(0);
        JavaScriptParser.ExpressionSequenceContext change = ctx.expressionSequence(1);
        JavaScriptParser.BlockContext block = ctx.statement().block();

        RuntimeContext runtimeContext = new RuntimeContext(rctx);
        JavaScriptParserVisitor visitor = JavaScriptParserVisitorImpl.proxy(runtimeContext);
        visitor.visit(var);
        for1:
        while ((Boolean) condition.accept(visitor)){
            List<JavaScriptParser.StatementContext> statement = block.statementList().statement();
            Object accept= null;
            for (int i = 0; i < statement.size(); i++) {
                JavaScriptParser.StatementContext statementContext = statement.get(i);
                accept = statementContext.accept(visitor);
                if(accept==null){
                }else if(accept instanceof CtrlOptional){
                    CtrlOptional co = (CtrlOptional)accept;
                    if(co.is(CtrlFlag.ctrl_break)){
                        break for1;
                    }
                    if(co.is(CtrlFlag.ctrl_continue)){
                        break;
                    }
                    if(co.is(CtrlFlag.ctrl_return)){
                        return co;
                    }
                }
            }
            visitor.visit(change);
        }

        return CtrlOptional.run;
    }

    /**
     * for (var i in list){
     *     ...
     * }
     * @param ctx
     * @return
     */
    @Override
    public Object visitForInStatement(JavaScriptParser.ForInStatementContext ctx) {
        JavaScriptParser.VariableStatementContext var = ctx.variableStatement();
        JavaScriptParser.SingleExpressionContext singleExpressionContext = ctx.singleExpression();
        JavaScriptParser.ExpressionSequenceContext expressionSequence = ctx.expressionSequence();
        JavaScriptParser.BlockContext block = ctx.statement().block();

        RuntimeContext runtimeContext = new RuntimeContext(rctx);
        JavaScriptParserVisitorForValue visitor = JavaScriptParserVisitorImpl.proxy(runtimeContext);
        JavaScriptParser.VariableDeclarationListContext varList = var.variableDeclarationList();
        JavaScriptParser.VariableDeclarationContext varDecl = varList.variableDeclaration(0);
        String kName = varDecl.assignable().getText();
        Object o = visitor.visitValue(expressionSequence);
        if(o == null){
            return CtrlOptional.run;
        }
        Map<String,Object> target = null;
        if(o instanceof List){
            target = new TreeMap<>();
            List list = (List)o;
            if(CollectionUtils.isEmpty(list)){
                return CtrlOptional.run;
            }
            for (int i = 0; i < list.size(); i++) {
                target.put(String.valueOf(i), list.get(i));
            }
        } else if(o instanceof Map){
            target = (Map<String,Object>)o;
        }
        Object[] entries = target.entrySet().toArray();
        int size = entries.length;
        for1:
        for (int index = 0; index < size; index++) {
            Map.Entry<String,Object> entry = (Map.Entry<String,Object>)entries[index];
            runtimeContext.put(kName, entry.getKey());
            List<JavaScriptParser.StatementContext> statement = block.statementList().statement();
            Object accept= null;
            for (int i = 0; i < statement.size(); i++) {
                JavaScriptParser.StatementContext statementContext = statement.get(i);
                accept = statementContext.accept(visitor);
                if(accept==null){
                }else if(accept instanceof CtrlOptional){
                    CtrlOptional co = (CtrlOptional)accept;
                    if(co.is(CtrlFlag.ctrl_break)){
                        break for1;
                    }
                    if(co.is(CtrlFlag.ctrl_continue)){
                        break;
                    }
                    if(co.is(CtrlFlag.ctrl_return)){
                        return co;
                    }
                }
            }
        }

        return CtrlOptional.run;
    }

    @Override
    public Object visitPostIncrementExpression(JavaScriptParser.PostIncrementExpressionContext ctx) {
        BigDecimal accept = (BigDecimal)this.visitValue(ctx.singleExpression());
        if (ctx.singleExpression() instanceof JavaScriptParser.IdentifierExpressionContext) {
            TerminalNode identifier = ((JavaScriptParser.IdentifierExpressionContext) ctx.singleExpression()).Identifier();
            rctx.findAndPut(identifier.getText(),accept.add(BigDecimal.ONE));
        }
        return accept ;
    }

    /**
     * 等式或者不等式
     * @param ctx
     * @return
     */
    @Override
    public Object visitEqualityExpression(JavaScriptParser.EqualityExpressionContext ctx) {
        Object a = this.visitValue(ctx.singleExpression(0));
        Object b = this.visitValue(ctx.singleExpression(1));
        if(ctx.Equals_()!=null){
            return Objects.equals(a,b);
        }
        if(ctx.IdentityEquals()!= null){
            return Objects.equals(a,b);
        }
        if(ctx.NotEquals()!= null){
            return !Objects.equals(a,b);
        }
        if(ctx.IdentityNotEquals()!= null){
            return !Objects.equals(a,b);
        }
        return false;
    }

    @Override
    public Object visitObjectLiteralExpression(JavaScriptParser.ObjectLiteralExpressionContext ctx) {
        return super.visitObjectLiteralExpression(ctx);
    }

    @Override
    public Object visitObjectLiteral(JavaScriptParser.ObjectLiteralContext ctx) {
        Map<String,Object> json = new HashMap<>();
        for (JavaScriptParser.PropertyAssignmentContext propertyAssignmentContext : ctx.propertyAssignment()) {
            Pair<String,Object> p = (Pair)this.visit(propertyAssignmentContext);
            Object value = parseValue(p.getValue());
            if(value instanceof FunctionDef){
                ((FunctionDef)value).runtimeContext.put("this", json);
            }
            json.put(p.getKey(),value);
        }
        return json;
    }

    @Override
    public Object visitPropertyExpressionAssignment(JavaScriptParser.PropertyExpressionAssignmentContext ctx) {
        return new Pair(this.visitValue(ctx.propertyName()), this.visitValue(ctx.singleExpression()));
    }

    @Override
    public Object visitComputedPropertyExpressionAssignment(JavaScriptParser.ComputedPropertyExpressionAssignmentContext ctx) {
        return new Pair(this.visit(ctx.singleExpression(0)), this.visitValue(ctx.singleExpression(1)));
    }


    @Override
    public Object visitFunctionProperty(JavaScriptParser.FunctionPropertyContext ctx) {
        String name = ctx.propertyName().getText();
        JavaScriptParser.FormalParameterListContext formalParameterListContext = ctx.formalParameterList();
        JavaScriptParser.FunctionBodyContext functionBodyContext = ctx.functionBody();
        FunctionDef functionDef = buildFunctionDef(formalParameterListContext, functionBodyContext);
        return new Pair(name, functionDef);
    }

    @Override
    public Object visitPropertyName(JavaScriptParser.PropertyNameContext ctx) {
        if (ctx.StringLiteral()!= null) {
            String text = ctx.StringLiteral().getSymbol().getText();
            return text.substring(1,text.length()-1);
        }
        if(ctx.singleExpression()!= null){
            return this.visitValue(ctx.singleExpression());
        }
        return super.visitPropertyName(ctx);
    }

    @Override
    public Object visitIdentifierName(JavaScriptParser.IdentifierNameContext ctx) {
        return ctx.getText();
    }



    @Override
    public Object visitPropertyGetter(JavaScriptParser.PropertyGetterContext ctx) {
        String name = ctx.getter().Identifier().getText();
        JavaScriptParser.FunctionBodyContext functionBodyContext = ctx.functionBody();
        FunctionDef functionDef = buildFunctionDef(null, functionBodyContext);
        return new Pair<>("__getter__"+name, functionDef);
    }

    @Override
    public Object visitPropertySetter(JavaScriptParser.PropertySetterContext ctx) {
        String name = ctx.setter().Identifier().getText();
        JavaScriptParser.FunctionBodyContext functionBodyContext = ctx.functionBody();
        FunctionDef functionDef = buildFunctionDef(null, functionBodyContext);
        return new Pair<>("__setter__"+name, functionDef);
    }

    /**
     * list[0]
     * 通过index从集合中获取元素
     * @param ctx
     * @return
     */
    @Override
    public Object visitMemberIndexExpression(JavaScriptParser.MemberIndexExpressionContext ctx) {
        String text = ctx.getText();
        Object obj = this.visitValue(ctx.singleExpression());
        Object name = this.visitValue(ctx.memberPropertyName());
        if(name == null){
            throw  new RuntimeException(String.format(" property name is null in %d: %s", ctx.getAltNumber(), text));
        }
        return  JavaExtendsMethodHandler.memberField(obj,name.toString());
    }

    /**
     * obj.name
     * 通过 . 获取对象的属性
     * @param ctx
     * @return
     */
    @Override
    public Object visitMemberDotExpression(JavaScriptParser.MemberDotExpressionContext ctx) {
        String name = ctx.identifierName().getText();
        JavaScriptParser.SingleExpressionContext singleExpressionContext = ctx.singleExpression();
        Object obj = this.visitValue(singleExpressionContext);
        return  JavaExtendsMethodHandler.memberField(obj,name);
    }

    @Override
    public Object visitArrayLiteral(JavaScriptParser.ArrayLiteralContext ctx) {
        JavaScriptParser.ElementListContext elementListContext = ctx.elementList();
        return this.visit(elementListContext);
    }

    @Override
    public Object visitElementList(JavaScriptParser.ElementListContext ctx) {
        List<JavaScriptParser.ArrayElementContext> arrayElementContexts = ctx.arrayElement();
        List list = new ArrayList(arrayElementContexts.size());
        for (JavaScriptParser.ArrayElementContext arrayElementContext : arrayElementContexts) {
            list.add(this.visitValue(arrayElementContext));
        }
        return list;
    }

    @Override
    public Object visitArrayElement(JavaScriptParser.ArrayElementContext ctx) {
        return this.visit(ctx.singleExpression());
    }

    /**
     * new 表达式
     * @param ctx
     * @return
     */
    @Override
    public Object visitNewExpression(JavaScriptParser.NewExpressionContext ctx) {
        Object value = this.visitValue(ctx.Identifier());

        List args = (List)this.visitArguments(ctx.arguments());
        int size = 0;
        if(CollectionUtils.isEmpty(args)){
            size = args.size();
        }
        if(value instanceof Class){
            Class clazz = (Class)value;
            if(clazz.isAssignableFrom(LocalDateTime.class)){
                return LocalDateTime.now();
            }
                Constructor[] constructors = ((Class) value).getConstructors();
                for (Constructor constructor : constructors) {
                    int parameterCount = constructor.getParameterCount();
                    if(parameterCount == size){
                        Object[] params = new Object[size];
                        if(size >0){
                            Class[] parameterTypes = constructor.getParameterTypes();
                            params = paramsParseForInvoke(parameterTypes, args);
                        }
                        try{
                            return constructor.newInstance(params);
                        }catch (Exception e){
                            continue;
                        }
                    }
                }
                throw new RuntimeException(String.format("class[%s] instance failed, no constructor matches ", ((Class)value).getSimpleName()));
        }
        if(value instanceof JsFunctionOperator){
            JsFunctionOperator fop = (JsFunctionOperator)value;
            Map<String,Object> jsonObject = new HashMap<>();
            fop.target = jsonObject;
            Object invoke = parseValue(fop);
            if(invoke != null){
                return invoke;
            }else{
                return jsonObject;
            }
        }
        if(value instanceof FunctionResult){
            FunctionResult result = (FunctionResult) value;
            FunctionDef f = result.functionDef;
            Map<String,Object> json = (Map<String,Object>)f.runtimeContext.findValue("this");
            if(result.result == null){
                return json;
            }else {
                return result.result;
            }
        }
        if(value instanceof ClassDef){
            Map<String,Object> jsonObject = ((ClassDef) value).newInstance(args);
            return jsonObject;
        }
        return null;
    }

    @Override
    public Object visitThisExpression(JavaScriptParser.ThisExpressionContext ctx) {
        Object value = rctx.findValue("this");
        if(value == null){
            value = new HashMap<>();
            rctx.put("this", value);
        }
        return value;
    }

    @Override
    public Object visitMemberPropertyName(JavaScriptParser.MemberPropertyNameContext ctx) {
        return super.visitMemberPropertyName(ctx);
    }

    /**
     * 三元运算符
     * @param ctx
     * @return
     */
    @Override
    public Object visitTernaryExpression(JavaScriptParser.TernaryExpressionContext ctx) {
        JavaScriptParser.SingleExpressionContext s0 = ctx.singleExpression(0);
        JavaScriptParser.SingleExpressionContext s1 = ctx.singleExpression(1);
        JavaScriptParser.SingleExpressionContext s2 = ctx.singleExpression(2);
        Boolean b = (Boolean)this.visitValue(s0);
        if(b){
            return this.visit(s1);
        } else {
            return this.visit(s2);
        }
    }

    @Override
    public Object visitAssignmentOperatorExpression(JavaScriptParser.AssignmentOperatorExpressionContext ctx) {
        JavaScriptParser.SingleExpressionContext var = ctx.singleExpression(0);
        JavaScriptParser.SingleExpressionContext valueExpr = ctx.singleExpression(1);
        String text = ctx.assignmentOperator().getText();
        Object visit = this.visit(var);
        BigDecimal value = (BigDecimal)this.visitValue(valueExpr);
        if(value == null){
            return NaNEnum.NaN;
        }
        if( value != null && visit instanceof PropertyOperator ){
            PropertyOperator op = (PropertyOperator) visit;
            BigDecimal a = (BigDecimal)op.get();
            if(a == null){
                return NaNEnum.NaN;
            }
            BigDecimal res = null;
            switch (text){
                case "+=": res = a.add(value);break;
                case "-=": res =  a.subtract(value);break;
                case "*=": res =  a.multiply(value);break;
                case "/=": res =  a.divide(value,2, BigDecimal.ROUND_HALF_UP);break;
                default: throw new RuntimeException(String.format("assignmentOperator %s is undefined", text));
            }
            op.set(res);
        } else {
            throw new RuntimeException(String.format("variable in assignmentOperatorExpression is not PropertyOperator %s", text));
        }
        return super.visitAssignmentOperatorExpression(ctx);
    }

    @Override
    public Object visitAssignmentOperator(JavaScriptParser.AssignmentOperatorContext ctx) {
        return super.visitAssignmentOperator(ctx);
    }
    static Pattern number = Pattern.compile("[\\-]?[1-9]?[0-9]+(\\.[0-9]*)?");

    /**
     * 比较两个对象的大小
     * @param a
     * @param b
     * @return
     */
    private int compare(Object a,Object b){
        if(Objects.equals(a,b)){
            return 0;
        }
        if(b == null || b == NaNEnum.NaN|| b == Undefined.getUndefined()){
            return -1;
        }
        if(a == null || a == NaNEnum.NaN|| a == Undefined.getUndefined()){
            return 1;
        }
        if(a instanceof Comparable && b instanceof Comparable){
            Comparable aValue = (Comparable)a;
            Comparable bValue = (Comparable)b;
            return aValue.compareTo(bValue);
        }
        if(a instanceof String && b instanceof String){
            String aStr = a.toString();
            String bStr = b.toString();
            if(number.matcher(aStr).matches() && number.matcher(bStr).matches() ){
                Double aDouble = Double.valueOf(aStr);
                Double bDouble = Double.valueOf(bStr);
                int i = aDouble.compareTo(bDouble);
                return i;
            }
            Comparator cmp = Collator.getInstance(Locale.CHINA);
            return cmp.compare(aStr, bStr);
        }

        return 0;
    }

    @Override
    public Object visitSqlBodyItem(JavaScriptParser.SqlBodyItemContext ctx) {
        return  ctx.getText();
    }
    List<String> strings = Arrays.asList(".", "/", "#", "<", "?","$","{","}","@","??",",");
    @Override
    public Object visitSqlBody(JavaScriptParser.SqlBodyContext ctx) {

        return ctx.sqlBodyItem().stream()
                .map(e-> visitSqlBodyItem(e).toString())
                .reduce((a,b)-> {
                    if(strings.contains(b)){
                        return a+ b;
                    }
                    return  a+" "+b;
                })
                .get();
    }

    @Override
    public Object visitSqlDeclaration(JavaScriptParser.SqlDeclarationContext ctx) {
        String s = ctx.getRuleContext().toString();
        JavaScriptParser.SqlBodyContext sqlBodyContext = ctx.sqlBody();
        int startIndex = sqlBodyContext.getStart().getStartIndex();
        int stopIndex = sqlBodyContext.getStop().getStopIndex();
        CharStream inputStream = sqlBodyContext.getStart().getInputStream();
        String text = inputStream.getText(new Interval(startIndex,stopIndex));


        String name = ctx.Identifier().getText();

//        String sqlBody = (String)visitSqlBody(sqlBodyContext);
        String sqlBody = text;
        SQLDef sqlDef = new SQLDef(name, sqlBody);
        rctx.put(name, sqlDef);
        return sqlDef;
    }

    public static JavaScriptParserVisitorForValue proxy(RuntimeContext runtimeContext){
        Class<JavaScriptParserVisitorImpl> clazz = JavaScriptParserVisitorImpl.class;
        JavaScriptParserVisitorImpl visitor = new JavaScriptParserVisitorImpl(runtimeContext);
        JavaScriptParserVisitorForValue visitorProxy = (JavaScriptParserVisitorForValue)Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), new JavaScriptParserVisitorProxy(visitor));
        return visitorProxy;
    }
}
