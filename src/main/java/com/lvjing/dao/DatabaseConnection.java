package com.lvjing.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvjing.bean.ConnectionSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
//    @Autowired
//    private static ConnectionSettings cs;

    private static Connection conn = null;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
//    //测试
//    public static String url = "jdbc:mysql://39.106.10.110:3306/lvjingcleaning?useUnicode=true&characterEncoding=UTF-8&useSSL=true";
//    public static String userName = "test";
//    public static String psw = "test123!@#";
    //线上
    public static String url = "jdbc:mysql://rm-bp170nx0g4w9s946x0o.mysql.rds.aliyuncs.com:3306/lvjingcleaning?useUnicode=true&characterEncoding=UTF-8&useSSL=true";
    public static String userName = "root";
    public static String psw = "root123!@#";

    public static Connection getCon() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); //加载数据库连接驱动
            conn = DriverManager.getConnection(url, userName, psw);  //获取连接
        } catch (Exception e) {
            logger.error("数据库连接失败: ",e.getMessage());
        }
        return conn;
    }
}
