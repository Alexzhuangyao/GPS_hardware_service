package com.lvjing.common;

import org.apache.commons.lang.StringUtils;
import org.omg.CORBA.DATA_CONVERSION;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Author:HuangHua
 * @Descirption:
 * @Date: Created by zy on 2018/7/7.
 * @Modified By:
 */
@Component
public class PositionReader {

    private static final Logger logger = LoggerFactory.getLogger(PositionReader.class);

    public String transform(String location){
        /**
         * @Description: 将asc坐标转换为16进制坐标
         * @author: zy
         * @param:  [location]
         * @return: java.lang.String
         * @Date: 2018/7/7
         */
        String result = "";
        String dataPayLoad = "";
        //TODO 提高字符串拼接效率
        StringBuffer stringBuffer = new StringBuffer();
        if(StringUtils.isNotBlank(location)){
            dataPayLoad = location.substring(4);
            //System.out.println(dataPayLoad);
            for(int i = 0;i < dataPayLoad.length();i = i + 2){
                String str = dataPayLoad.substring(i,i+2);
                //System.out.println(str);
                if(StringUtils.equals("2E",str)){
                    result = result + ".";
                }else{
                    result = result + str.substring(1);
                }
            }
        }

        return result;
    }
}
