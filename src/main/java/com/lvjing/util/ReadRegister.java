package com.lvjing.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ReadRegister {
    public byte[] readRegister(byte[] param) {
        SimpleDateFormat yy = new SimpleDateFormat("yy");
        SimpleDateFormat MM = new SimpleDateFormat("MM");
        SimpleDateFormat dd = new SimpleDateFormat("dd");
        SimpleDateFormat HH = new SimpleDateFormat("HH");
        SimpleDateFormat mm = new SimpleDateFormat("mm");
        SimpleDateFormat ss = new SimpleDateFormat("ss");

        yy.format(new Date());
        MM.format(new Date());
        dd.format(new Date());
        HH.format(new Date());
        mm.format(new Date());
        ss.format(new Date());
        byte[] result = new byte[]{0x7E,0x7E,0x23,0x23,0x55,0x54,0x4E,0x11,0x00,0x01,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x3E,0x00,0x01,0x00,0x06,0x00,0x12,0x21,0xA,0x0B,0x00,0x00,0x05,0x00,0x01,0x02,0x00,0x04,0x00,0x06,0x18,0x06,0x04,0x10,0x53,0x42,0x00,0x08,0x00,0x04,0x00,0x00,0x00,0x00,0x00,0x06,0x00,0x01, 0x1B,0x00,0x07,0x00,0x02,0x1E,0x37};
        for (int i = 0; (18 + i) < 28; i++) {
            result[i] = param[18 + i];
        }
        byte[] sign_start = new byte[5];
        for (int i = 0; (28 + i) < 33; i++) {
            sign_start[i] = param[28 + i];
        }
        byte[] product_key = new byte[10];
        for (int i = 0; (33 + i) < 43; i++) {
            product_key[i] = param[33 + i];
        }
        result[43] =(yy.toString().getBytes())[0];
        result[44] =(MM.toString().getBytes())[0];
        result[45] =(dd.toString().getBytes())[0];
        result[46] =(dd.toString().getBytes())[0];
        result[47] =(HH.toString().getBytes())[0];
        result[48] =(mm.toString().getBytes())[0];
        result[49] =(ss.toString().getBytes())[0];

        for(int i=0;i<60;i++) {
            System.out.printf("%2x", result[i]);
        }
        return result;
        }
}

