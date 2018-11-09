package com.lvjing.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @Author:HuangHua
 * @Descirption:
 * @Date: Created by huanghua on 2018/7/7.
 * @Modified By:
 */
@Component
public class VibrantReader {

    private static final Logger logger = LoggerFactory.getLogger(VibrantReader.class);

    public String transform(String isVibrant){
        if(StringUtils.isNotBlank(isVibrant)){
            if(StringUtils.equals(isVibrant,"1797000131")){
                return "1";
            }else if(StringUtils.equals(isVibrant,"1797000130")){
                return "0";
            }
        }
        return null;
    }
}
