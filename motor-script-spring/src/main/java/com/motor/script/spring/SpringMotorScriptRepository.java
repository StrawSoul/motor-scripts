package com.motor.script.spring;

import com.motor.common.exception.BusinessRuntimeException;
import com.motor.script.core.MotorScriptsRepository;
import com.motor.script.core.model.ScriptEntity;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * ===========================================================================================
 * 设计说明
 * -------------------------------------------------------------------------------------------
 * <p>
 * ===========================================================================================
 * 方法简介
 * -------------------------------------------------------------------------------------------
 * {methodName}     ->  {description}
 * ===========================================================================================
 * 变更记录
 * -------------------------------------------------------------------------------------------
 * version: 0.0.0  2020/8/25 12:00  zlj
 * 创建
 * -------------------------------------------------------------------------------------------
 * version: 0.0.1  {date}       {author}
 * <p>
 * ===========================================================================================
 */
@Service
public class SpringMotorScriptRepository implements MotorScriptsRepository {

    @Autowired
    private SpringMotorScriptConfig scriptConfig;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ScriptEntity findByName(String namespace, String name) {
        String path = scriptConfig.getPath();
        Resource resource = applicationContext.getResource(path+ File.separator+ namespace+ File.separator+ name+ ".js");
        try {
            InputStream inputStream = resource.getInputStream();
            String scriptText = IOUtils.toString(inputStream, "utf-8");
            ScriptEntity scriptEntity = new ScriptEntity();
            scriptEntity.setNamespace(namespace);
            scriptEntity.setName(name);
            scriptEntity.setBody(scriptText);
            return scriptEntity;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
