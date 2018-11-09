package com.lvjing.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static Connection conn = null;

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    public static Connection getCon() {
        try {
            Class.forName("com.mysql.jdbc.Driver"); //加载数据库连接驱动
            String user = "test";
            String psw = "test123!@#";  //XXX为自己的数据库的密码//test for Lvjing root for meigao
//            String url = "jdbc:mysql://rm-bp170nx0g4w9s946x0o.mysql.rds.aliyuncs.com:3306/lvjingcleaning?useUnicode=true&characterEncoding=UTF-8&useSSL=true";                   //ZZZ为连接的名字
            String url = "jdbc:mysql://39.106.10.110:3306/lvjingcleaning?useUnicode=true&characterEncoding=UTF-8&useSSL=true";                   //ZZZ为连接的名字
            conn = DriverManager.getConnection(url, user, psw);  //获取连接
        } catch (Exception e) {
            logger.error("数据库连接失败: ",e.getMessage());
        }
        return conn;
    }
}