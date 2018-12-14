package com.lvjing.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Author:zy
 * @Descirption:
 * @Date: Created by zy on 2018/12/14.
 * @Modified By:
 */
@Component
public class RunTimeReader {

    private static final Logger logger = LoggerFactory.getLogger(RunTimeReader.class);
    public String transform(String runtime){
        String result = runtime.substring(4);
        StringBuffer resultString = new StringBuffer();
        logger.info(result);
        logger.info("len:",result.length());
        for(int i = 0;i <= result.length()-2;i = i + 2){
            String str = result.substring(i,i+2);
            if(!StringUtils.equals(str,"00")){
                resultString.append(str.substring(1));
            }
        }
        return resultString.toString();
}
}
