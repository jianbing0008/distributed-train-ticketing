package com.jiawa.train.generator.server;


import com.jiawa.train.generator.util.FreemarkerUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ServerGenerator {
    static String servicePath = "[module]/src/main/java/com/jiawa/train/[module]/service/";
    static String pomPath = "generator\\pom.xml";
    static {
        new File(servicePath).mkdirs();
    }

    public static void main(String[] args) throws Exception{
        // 获取mybatis-generator
        String generatorPath = getGeneratorPath();
        //比如 generator-config-member.xml，得到模块名module = member
        String module = generatorPath.replace("src/main/resources/generator-config-","")
                                     .replace(".xml","");

        System.out.println("module: " + module);
        servicePath = servicePath.replace("[module]",module);
        System.out.println("servicePath: " + servicePath);

        // 读取table节点
        Document document = new SAXReader().read("generator/" + generatorPath);
        Node table = document.selectSingleNode("//table");
        System.out.println(table);
        //读取table节点后拿到两个属性 tableName与domainOjectName
        Node tableName = table.selectSingleNode("@tableName");
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());

        // 示例：表名 jian_test
        // Domain = JianTest
        String Domain = domainObjectName.getText();
        // domain = jianTest
        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);
        // do_main = jian-test  controller层url用到
        String do_main = tableName.getText().replaceAll("_", "-");


        FreemarkerUtil.initConfig("service.ftl");
        // 组装参数
        Map<String, Object> param = new HashMap<>();
        param.put("module", module);
        param.put("Domain", Domain);
        param.put("domain", domain);
        param.put("do_main", do_main);
        System.out.println("组装参数：" + param);

        FreemarkerUtil.initConfig("service.ftl");
        FreemarkerUtil.generator(servicePath + Domain + "Service.java",param);
    }

    private static String getGeneratorPath() throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Map<String, String> map = new HashMap<String, String>();
        map.put("pom", "http://maven.apache.org/POM/4.0.0");
        saxReader.getDocumentFactory().setXPathNamespaceURIs(map);
        //读取定义好的pom文件
        Document document = saxReader.read(pomPath);
        //使用XPath快速定位节点和属性  //：从根目录下寻找，pom是xml命名空间  configurationFile:在命名空间下寻找configurationFile的节点
        Node node = document.selectSingleNode("//pom:configurationFile");
        System.out.println(node.getText());
        return node.getText();
    }
}