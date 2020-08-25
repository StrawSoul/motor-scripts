package com.motor.script.core.antlr4.impl;

import jdk.nashorn.internal.runtime.Undefined;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.http.client.utils.CloneUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ibdp.kanban.calculate.domain.javascript.antlr4.impl.NaNEnum.NaN;

public class JavaExtendsMethodHandler {
    public static Pattern compile_number = Pattern.compile("[1-9]?[0-9]+");
    public static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析属性对象,如果是复杂的表达式,不立即执行,只获取表达式的声明对象, 在下次计算,Return或赋值给其他变量时才计算
     * @param obj
     * @param name
     * @return
     */
    public static Object memberField(Object obj, String name){
        if(obj instanceof  PropertyOperator){
            obj = ((PropertyOperator)obj).get();
        }
        if(obj != null && obj != Undefined.getUndefined()){

            if(obj instanceof Map){
                return new JsonFieldDef((Map) obj, name);
            }
            if(obj instanceof List && compile_number.matcher(name).matches()){
                return new ArrayDef((List)obj, Integer.valueOf(name));
            }
            if(obj instanceof MapElementDef){
                return new MapElementDef((Map)obj, name);
            }
            Class<?> clazz = null;
            if(obj instanceof Class){
                clazz = (Class)obj;
            } else{
                clazz = obj.getClass();
            }
//        Field[] fields = clazz.getFields();
//        for (Field field : fields) {
//            field.setAccessible(true);
//            if (Objects.equals(field.getName(), name)) {
//                return new JavaFieldDef(obj,name.toString(), field);
//            }
//        }
            Object extendsMethod =  extend(obj,clazz,name);
            if(extendsMethod != null){
                return extendsMethod;
            }
            Method[] methods = clazz.getMethods();
            List<Method> ms = new ArrayList<>();
            List<Method> getset_ms = new ArrayList<>();
            for (Method method : methods) {
                if( Objects.equals(method.getName().toLowerCase(), "get"+name.toLowerCase())
                        ||Objects.equals(method.getName().toLowerCase(), "set"+name.toLowerCase())){
                    getset_ms.add(method);
                } else
                if (Objects.equals(method.getName(), name)) {
                    ms.add(method);
                }
            }
            if(!CollectionUtils.isEmpty(ms)){
                return new JavaMethodDef(obj, name, ms);
            }
            if(!CollectionUtils.isEmpty(getset_ms)){
                return new JavaGetSetMethodDef(obj, name, getset_ms);
            }
            throw new RuntimeException(String.format("%s中 属性 %s 不存在", clazz.getSimpleName(),name));
        }else{
            if(Objects.equals(name, "toString")){
                return "null";
            }else
            if(Objects.equals(name,  "orElse")){
                try {
                    List<Method> ms = Arrays.asList(Optional.class.getMethod("orElse", Object.class));
                    return new JavaMethodDef(Optional.empty(), name, ms);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
            throw new RuntimeException(String.format("null中 属性 %s 不存在",name));
        }
    }

    /**
     *  为java对象或类扩展函数,方便在脚本内执行
     * @param obj
     * @param clazz
     * @param methodName
     * @return
     * todo 这里扩展性需要更灵活一些,做到不改懂代码就能扩展新的函数,覆盖已有的默认函数
     */
    private static Object extend(Object obj, Class clazz, String methodName){
        if(obj instanceof String){
            String str = (String)obj;
            if(Objects.equals(methodName, "split")){
                return new ExtendsFunctionDef<String>((it,args)-> {
                    String[] arr = it.split((String) args.get(0));
                    List<String> list = new ArrayList<>();
                    Collections.addAll(list, arr);
                    return list;
                }, str);
            }

        }else if(obj == NaN){
            /**
             *  计算得到 NaN, toFixed 会报错, 这里直接返回 NaN
             */
            if(Objects.equals(methodName, "toFixed")){
                return new ExtendsFunctionDef<NaNEnum>((it,args)-> {return NaN;}, obj);
            }
        }else
        if(List.class.isInstance(obj)){
            List list = (List)obj;
            if(Objects.equals(methodName, "push")){
                return new ExtendsFunctionDef<List>((it,args)-> {return it.add(args.get(0));}, list);
            }
            if(Objects.equals("length", methodName)){
                return BigDecimal.valueOf(list.size());
            }
            if(Objects.equals("forEach", methodName)){
                return new ExtendsFunctionDef<List>((it,args)->{
                    it.forEach((e)-> {
                        ((FunctionDef) args.get(0)).invoke(null, Arrays.asList(e));
                    });
                    return null;
                }, list);
            }
            if(Objects.equals("sort", methodName)){
                return new ExtendsFunctionDef<List>((it,args)->{
                    it.sort((a,b)-> {
                        return ((Number)((FunctionDef) args.get(0)).invoke(null, Arrays.asList(a,b))).intValue();
                    });
                    return it;
                }, list);
            }
            if(Objects.equals("first", methodName)){
                return new ExtendsFunctionDef<List>((it,args)->{
                    return CollectionUtils.isEmpty(it) ? null: it.get(0);
                }, list);
            }
        } else if(Map.class.isInstance(obj)){
            Map map = (Map)obj;
            if(Objects.equals("cloneObj", methodName)){
                return new ExtendsFunctionDef<Map>((it,args)->{
                    if(MapUtils.isEmpty(it) ){
                        return Collections.EMPTY_MAP;
                    }
                    Object newMap = null;
                    try {
                        newMap = CloneUtils.clone(map);
                    } catch (CloneNotSupportedException e) {
                        throw new RuntimeException(e);
                    }
                    return newMap;
                }, map);
            }
        }
        if(obj instanceof Stream){
            Stream stream = (Stream)obj;
            if(Objects.equals("map", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.map(e-> {
                        Object value = ((FunctionDef) args.get(0)).invoke(null, Arrays.asList(e));
                        return value;
                    });
                }, stream);
            }
            if(Objects.equals("reduce", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.filter(e -> e != null).reduce((a,b)-> {
                        Object value = ((FunctionDef) args.get(0)).invoke(null, Arrays.asList(a, b));
                        return value;
                    });
                }, stream);
            }
            if(Objects.equals("forEach", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    it.forEach((e)-> {
                       ((FunctionDef) args.get(0)).invoke(null, Arrays.asList(e));
                    });
                    return null;
                }, stream);
            }
            if(Objects.equals("skip", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.skip((new BigDecimal(args.get(0).toString())).intValue());
                }, stream);
            }
            if(Objects.equals("limit", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.limit((new BigDecimal(args.get(0).toString())).intValue());
                }, stream);
            }
            if(Objects.equals("filter", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.filter(e-> {
                        return (Boolean) ((FunctionDef) args.get(0)).invoke(null, Arrays.asList(e));
                    });
                }, stream);
            }
            if(Objects.equals("toList", methodName)){
                return new ExtendsFunctionDef<Stream>((it,args)->{
                    return it.collect(Collectors.toList());
                }, stream);
            }
//            if(Objects.equals("toMap", methodName)){
//                return new ExtendsFunctionDef<Stream>((it,args)->{
//                    return it.collect(Collectors.toMap(e->{},f->f));
//                }, stream);
//            }

        }
        if(obj instanceof ParameterOptional){
            ParameterOptional op = (ParameterOptional)obj;
            if(Objects.equals("orElse",methodName)){
                return new ExtendsFunctionDef<ParameterOptional>((it,args)->{
                    return it.orElse(args.get(0));
                }, op);
            }
            if(Objects.equals("empty",methodName)){
                return new ExtendsFunctionDef<ParameterOptional>((it,args)->{
                    if (it.isEmpty()) {
                        ((FunctionDef) args.get(0)).invoke(null );
                    }
                    return it;
                }, op);
            }
            if(Objects.equals("notEmpty",methodName)){
                return new ExtendsFunctionDef<ParameterOptional>((it,args)->{
                    if (!it.isEmpty()) {

                        ((FunctionDef) args.get(0)).invoke(null , Arrays.asList(op.value()));
                    }
                    return it;
                }, op);
            }

        }
        if(obj instanceof Optional){
            Optional op = (Optional)obj;
            if(Objects.equals("orElseGet",methodName)){
                return new ExtendsFunctionDef<Optional>((it,args)->{
                    return it.orElseGet(()-> ((FunctionDef)args.get(0)).invoke(null, Arrays.asList()));
                }, op);
            }
        }
        if(obj instanceof BigDecimal){
            BigDecimal bigDecimal = (BigDecimal) obj;
            if(Objects.equals(methodName, "toFixed")){
                return new ExtendsFunctionDef<BigDecimal>((it,args)-> {return it.setScale(Integer.valueOf(args.get(0).toString()),BigDecimal.ROUND_HALF_UP);},bigDecimal);
            }
        }
        if(obj instanceof Date){
            Date date = (Date)obj;
            if(Objects.equals("getFullYear", methodName)){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                return new ExtendsFunctionDef<Date>((it,args)-> {return simpleDateFormat.format(date).substring(0,4);},date);
            }
        }
        if(obj instanceof LocalDateTime){
            LocalDateTime date = (LocalDateTime)obj;
            if(Objects.equals("getFullYear", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.toString().substring(0,4);},date);
            }
            if(Objects.equals("getTime", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.toInstant(ZoneOffset.of("+8")).toEpochMilli();},date);
            }
            if(Objects.equals("getMonth", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {
                    return date.getMonthValue();
                    },date);
            }
            if(Objects.equals("getDate", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.getDayOfMonth();},date);
            }
            if(Objects.equals("getHours", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.getHour();},date);
            }
            if(Objects.equals("getMinutes", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.getMinute();},date);
            }
            if(Objects.equals("yearAndMonth", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.toString().substring(0,7);},date);
            }
            if(Objects.equals("setMonth", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.withMonth(((Number)args.get(0)).intValue());},date);
            }
            if(Objects.equals("setYear", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.withYear(((Number)args.get(0)).intValue());},date);
            }
            if(Objects.equals("setDate", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.withDayOfMonth(((Number)args.get(0)).intValue());},date);
            }
            if(Objects.equals("minusDays", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return date.minusDays(((Number)args.get(0)).intValue());},date);
            }
            if(Objects.equals("toDateTimeString", methodName)){
                return new ExtendsFunctionDef<LocalDateTime>((it,args)-> {return dateTimeFormatter.format(date);},date);
            }

        }
        if(obj instanceof Optional){
            Optional optional = (Optional)obj;
            if(Objects.equals("map", methodName)){
                return new ExtendsFunctionDef<Optional>((it,args)-> {
                    Object o = args.get(0);
                    if(o instanceof  FunctionDef){
                        FunctionDef f = (FunctionDef)o;
                        return it.map(e->{
                            return f.invoke(null, Arrays.asList(e));
                        });
                    }
                    throw new RuntimeException("Optional.map 不支持参数:"+ o.getClass());
                    },optional);
            }
        }

        return null;
    }

    public static void main(String[] args) {
        long time = new Date().getTime();
        LocalDateTime now = LocalDateTime.now();
        long epochSecond = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        System.out.println(time);
        System.out.println(epochSecond);
        System.out.println(now.getMonth().getValue());
        System.out.println(now.getDayOfMonth());
        System.out.println(now.getYear());
    }
}
