package com.jiawa.train.generator.server;


import com.jiawa.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class ServerGenerator {
    static String toPath = "generator\\src\\main\\java\\com\\jiawa\\train\\generator\\test\\";
    static {
        new File(toPath).mkdirs();
    }

    public static void main(String[] args) throws IOException, TemplateException {
        FreemarkerUtil.initConfig("test.ftl");
        HashMap<String, Object> param = new HashMap<>();
        param.put("domain","Test");
        FreemarkerUtil.generator(toPath + "Test.java",param);
    }
}