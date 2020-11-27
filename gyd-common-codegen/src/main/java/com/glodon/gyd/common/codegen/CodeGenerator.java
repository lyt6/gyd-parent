package com.glodon.gyd.common.codegen;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;
import java.util.Scanner;

public class CodeGenerator {

    /**
     * <p>
     * 读取控制台内容
     * </p>
     */
    private static String scanner(String tip) {
        Scanner scanner = new Scanner(System.in);
        System.out.println(("请输入" + tip + "："));
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        throw new MybatisPlusException("请输入正确的" + tip + "！");
    }

    private static void cleanSourceCode(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children == null) {
                return;
            }
            for (String aChildren : children) {
                cleanSourceCode(new File(dir, aChildren));
            }
        }
        dir.delete();
    }

    /**
     * RUN THIS
     */
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        String outputPath = projectPath + "/gyd-common-codegen/target/codegen";
        cleanSourceCode(new File(outputPath));

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setOutputDir(outputPath);
        gc.setAuthor(scanner("作者名"));
        gc.setOpen(false);
        gc.setServiceName("%sService");
        gc.setDateType(DateType.ONLY_DATE);
        mpg.setGlobalConfig(gc);
        // 数据源配置
        DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl("jdbc:mysql://localhost:3306/cloud2020?useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&transformedBitIsBoolean=true&allowMultiQueries=true&serverTimezone=Asia/Shanghai");
        dsc.setDriverName("com.mysql.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("root");
        mpg.setDataSource(dsc);
        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setModuleName(scanner("模块名"));
        pc.setParent("com.glodon.gyd");
        pc.setEntity("entities");
        pc.setController("rest");
        mpg.setPackageInfo(pc);
        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setNaming(NamingStrategy.underline_to_camel)
                .setColumnNaming(NamingStrategy.underline_to_camel)
                .setSuperEntityClass("com.glodon.gyd.common.db.entities.BaseEntity")
                .setEntityLombokModel(true)
                .setInclude(scanner("表名").split(","))
                .setTablePrefix(scanner("表前缀"))
                .setSuperEntityColumns("id", "create_time", "update_time")
                .setControllerMappingHyphenStyle(true)
                .setRestControllerStyle(true);
        mpg.setStrategy(strategy);
        // 选择 freemarker 引擎需要指定如下加，注意 pom 依赖必须有！
        mpg.setTemplateEngine(new CustomFreemarkerTemplateEngine());
        mpg.execute();

        cleanSourceCode(new File(outputPath + "/com"));
    }

}
