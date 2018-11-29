package com.lvjing.util;

import com.lvjing.bean.ConnectionSettings;
import org.assertj.core.util.Compatibility;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class ConnectionSettingsTest {

    @Autowired
    private ConnectionSettings conn;

    @Test
    public void Test(){
        String pwd =  conn.getPassword();
        String usn =  conn.getUsername();
        System.out.println(pwd);
        System.out.println(usn);
    }

}
