package com.jiawa.train.generator.server;


import com.jiawa.train.generator.util.DbUtil;
import com.jiawa.train.generator.util.Field;
import com.jiawa.train.generator.util.FreemarkerUtil;
import freemarker.template.TemplateException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerGenerator {
    static String serverPath = "[module]/src/main/java/com/jiawa/train/[module]/";
    static String pomPath = "generator\\pom.xml";
    static {
        new File(serverPath).mkdirs();
    }

    public static void main(String[] args) throws Exception{
        // 获取mybatis-generator
        String generatorPath = getGeneratorPath();
        //比如 generator-config-member.xml，得到模块名module = member
        String module = generatorPath.replace("src/main/resources/generator-config-","")
                                     .replace(".xml","");

        System.out.println("module: " + module);
        serverPath = serverPath.replace("[module]",module);
        System.out.println("servicePath: " + serverPath);

        // 读取table节点
        Document document = new SAXReader().read("generator/" + generatorPath);
        Node table = document.selectSingleNode("//table");
        System.out.println("table: "+ table);
        //读取table节点后拿到两个属性 tableName与domainOjectName
        Node tableName = table.selectSingleNode("@tableName");
        Node domainObjectName = table.selectSingleNode("@domainObjectName");
        System.out.println(tableName.getText() + "/" + domainObjectName.getText());

        //从配置文件获取连接数据
        Node jdbcConnection = document.selectSingleNode("//jdbcConnection");
        System.out.println(jdbcConnection);
        Node driverClass = jdbcConnection.selectSingleNode("@driverClass");
        Node connectionURL = jdbcConnection.selectSingleNode("@connectionURL");
        Node userId = jdbcConnection.selectSingleNode("@userId");
        Node password = jdbcConnection.selectSingleNode("@password");
        System.out.println("connectionURL = " + connectionURL);
        System.out.println("userId = " + userId);
        System.out.println("password = " + password);
        DbUtil.ConnectionInfo(connectionURL.getText(), userId.getText(), password.getText());

        // 示例：表名 jian_test
        // Domain = JianTest
        String Domain = domainObjectName.getText();
        // domain = jianTest
        String domain = Domain.substring(0, 1).toLowerCase() + Domain.substring(1);
        // do_main = jian-test  controller层url用到
        String do_main = tableName.getText().replaceAll("_", "-");

        //表中文名  获得表注释
        String tableNameCn = DbUtil.getTableComment(tableName.getText());
        List<Field> fieldList = DbUtil.getColumnByTableName(tableName.getText());
        System.out.println(tableName.getName()+"----"+tableName.getText());


        FreemarkerUtil.initConfig("service.ftl");
        // 组装参数
        Map<String, Object> param = new HashMap<>();
        param.put("module", module);
        param.put("Domain", Domain);
        param.put("domain", domain);
        param.put("do_main", do_main);
        System.out.println("组装参数：" + param);

        //gen(Domain, param,"service");
        //gen(Domain, param,"controller");
    }

    /**
     * 生成,想生成啥就改动里面的target就行
     * @param Domain
     * @param param
     * @throws IOException
     * @throws TemplateException
     */
    private static void gen(String Domain, Map<String, Object> param,String target) throws IOException, TemplateException {
        FreemarkerUtil.initConfig(target + ".ftl");
        String toPath = serverPath + target + "/";
        new File(toPath).mkdirs();
        String Target = target.substring(0, 1).toUpperCase() + target.substring(1);
        String fileName = toPath + Domain + Target + ".java";
        System.out.println("开始生成: " + fileName);
        FreemarkerUtil.generator(fileName, param);
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