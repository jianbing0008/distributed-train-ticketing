package com.jiawa.train.generator.server;


import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {
    static String toPath = "generator\\src\\main\\java\\com\\jiawa\\train\\generator\\test\\";
    static String pomPath = "generator\\pom.xml";
    static {
        new File(toPath).mkdirs();
    }

    public static void main(String[] args) throws Exception{
//        FreemarkerUtil.initConfig("test.ftl");
//        HashMap<String, Object> param = new HashMap<>();
//        param.put("domain","Test");
//        FreemarkerUtil.generator(toPath + "Test.java",param);
        SAXReader saxReader = new SAXReader();
        Map<String, String> map = new HashMap<String, String>();
        map.put("pom", "http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        //读取定义好的pom文件
        Document document = saxReader.read(pomPath);
        //使用XPath快速定位节点和属性  //：从根目录下寻找，pom是xml命名空间  configuration:在命名空间下寻找configuration的节点
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
    }
}