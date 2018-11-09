package com.lvjing.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author:HuangHua
 * @Descirption:
 * @Date: Created by huanghua on 2018/6/11.
 * @Modified By:
 */

@RunWith(SpringRunner.class)
@SpringBootTest

public class HexDecoderTest {
    //@Resource
    //HexDecoder hexDecoder;
    private static HexDecoder hexDecoder = new HexDecoder();

    @Test
    public void Test(){
//        String param1 = "7E 7E 23 23 55 544E 20 00 03 00 00 00 00 00 00 00 82 00 01 00 06 00 12 21 A0 0B 00" +
//                "00 05 00 01 02 F4 01 00 06 72 C3 1A 48 79 D4F4 02 00 06 00 12 21 A0 0B 00F4 03 00 06       00 12 21 A0 0B 00 F4 10 00 0F 38 36 37 31 38 36 30 33 30 38 32 39 33 38 35 F4 12 00 14       38 39 38 36 30 32 42 37 30 39 31 37 30 31 34 32 35 34 37 35 F4 14 00 04 18 04 11 01 F1 01     00 01 00 00 06 00 01 0300 07 00 02 EB 4E";
//        String param2 = param1.replaceAll(" ","");
//
//        //System.out.println(param2);
//        hexDecoder.readRegisterFrame(param2);
//
//        String param3 = "7E 7E 23 23 55 54 4E 22 00 02 18 06 16 04 24 37 00 8E" +
//                "00 01 00 06 00 12 21 A0 0B 00" +
//                "00 05 00 01 02 " +
//                "17 90 00 0C 33 30 2E 32 37 39 37 32 34 00 00 00" +
//                "17 91 00 0C 31 32 30 2E 31 30 35 34 39 36 00 00" +
//                " 17 92 00 0C 30 2E 30 30 30 30 30 30 00 00 00 00" +
//                " 17 93 00 0C 30 2E 30 30 30 30 30 30 00 00 00 00" +
//                "17 94 00 0A 31 34 34 30 00 00 00 00 00 00" +
//                "17 95 00 04 32 33 00 00" +
//                "17 96 00 03 33 31 00" +
//                "17 97 00 01 30" +
//                " 00 06 00 01 03" +
//                " 00 07 00 02 B9 E6";
//        String param4 = param3.replaceAll(" ","");
//        hexDecoder.readDataFrame(param4);
        String i = "04B0";
        System.out.println(Integer.valueOf(i,16).toString());




        System.out.println(HexDecoder.bytes2HexString(i.getBytes()));
        //System.out.println(HexDecoder.bytes2HexString(toBytes(i)));
    }

}
