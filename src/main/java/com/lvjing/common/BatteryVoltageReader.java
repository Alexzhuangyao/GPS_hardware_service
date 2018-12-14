package com.lvjing.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Author:HuangHua
 * @Descirption: 电池电压转换
 * @Date: Created by huanghua on 2018/7/7.
 * @Modified By:
 */
@Component
public class BatteryVoltageReader {
    private static final Logger logger = LoggerFactory.getLogger(BatteryVoltageReader.class);

    public String transform(String voltage){
        /**
         * @Description: 将电压转换为十进制
         * @author: Huang Hua
         * @param:  [voltage]
         * @return: java.lang.String
         * @Date: 2018/7/7
         */
        String result = "";
        String dataPayLoad = "";
        //TODO 提高字符串拼接效率
        StringBuffer stringBuffer = new StringBuffer();
        if(StringUtils.isNotBlank(voltage)){
            dataPayLoad = voltage.substring(4);
            for(int i = 0;i < dataPayLoad.length();i = i + 2){
                String str = dataPayLoad.substring(i,i+2);
                if(!StringUtils.equals(str,"00")){
                    result = result + str.substring(1);
                }
            }
        }
        String ret0 = result.substring(0,4);
        if (result.contains("E") || result.contains("e")) {
            result = result.replaceAll("E", "");
            result = result.replaceAll("e", "");
            ret0 = result.substring(0,5);
        };
        int tmp = Integer.parseInt(ret0) ;
        return Integer.toString(tmp);
    }
}
