package com.lvjing.socket;

import com.lvjing.dao.DatabaseConnection;
import com.lvjing.util.GpsOperation;
import com.lvjing.util.HexDecoder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author:HuangHua
 * @Descirption:
 * @Date: Created by huanghua on 2018/6/15.
 * @Modified By:
 */
public class ServerThread implements Runnable {

    HexDecoder hexDecoder = new HexDecoder();

    GpsOperation gpsOperation = new GpsOperation();

    private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);

    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;

//    /**版本控制**/
//    private static final String version = "18073102";

    /**带参数的构造函数**/
    public  ServerThread(Socket s) {
        this.socket = s;
    }

    @Override
    public void run(){
        logger.info("process started......");

        try {
            int i = 0;
            //获取服务端输入的消息
            in = socket.getInputStream();
            //服务端返回的消息
            out = socket.getOutputStream();
            //设置超时时间为6分钟，若超时触发Read timed out异常，关闭socket连接
            socket.setSoTimeout(360*1000);

            byte[] bytes = new byte[512];
            byte[] result_a = new byte[512];
            byte result = 0;
            int len = 0;
            /**采用这个方式判断比较简练,注意in.read(bytes)不能在一个循环里使用两次,要不然相当于读取了两次流,有一次结果必然是0**/
            while ((len = in.read(bytes)) > 0){
                logger.info("the length of data is:{}", len);
                String data = byteArrayToHexStr(bytes);
                //logger.info(data);//data
                //**用于格式转换**//
                String tmp = "";
                String dvc = "";
                //返回给客户端的消息
                switch (len){
                    //注册帧
                    case 130:
                        gpsOperation.saveGPSRegister(data);
                        tmp = hexDecoder.readRegisterFrame(data);
                        out.write(toBytes(tmp));
                        break;
//                    String swvsion = byteArrayToHexStr(bytes).substring(220,228);
//                    if (!swvsion.equalsIgnoreCase(version)){
//                        suc = hexDecoder.sendUpgradeCommand(byteArrayToHexStr(bytes));
//                        out.write(toBytes(suc));
//                        logger.info("update command: "+suc);
//                    };
//                  数据帧
                    case 142:
                        gpsOperation.saveGPSData(data);//数据库保存
                        tmp = hexDecoder.readDataFrame(data);
                        out.write(toBytes(tmp));
                        break;
//                    //对时帧
//                    case 76:
//                        //只做保存操作；
//                        gpsOperation.saveCheckTime(data);
//                        break;
                };
                logger.info("output response data:" + tmp);

                //对时帧 TODO 顺序
                if (len == 76 || len == 74){
//                if (data.substring(44,56).equals("180718010034") || data.substring(44,56).equals("180718010035")){
                    //只做保存操作；
                    //logger.info("进入修改参数操作函数。1");
                    gpsOperation.saveCheckTime(data);
                    //logger.info("进入修改参数操作函数。2");
                    try {
                        //判断DEVICE_PARAM_CONFIG表内是否有需要修改的参数,有则下发第一个。
                        dvc = hexDecoder.sendDeviceParamConfigChangeCommand(data);
                        //System.out.println(dvc);
                        out.write(toBytes(dvc));
                        //阻塞线程，30秒后break并关闭此线程，在接收data_frame之后不接受任何消息。
                        //Thread.currentThread().sleep(30000);//毫秒
                        logger.info("修改参数下发："+dvc);
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                    }
                }

                if (data.contains("F01") || data.contains("F10") || data.contains("F07")){
                    String cmd = "";
                    Connection conn = null;
                    //当前时间
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        conn = DatabaseConnection.getCon();  //建立数据库连接
                        String deviceId = "D"+data.substring(44,56);

                        if (data.contains("F015000C")) {
                            //设置绑定机器号成功
                            String machineId_raw = data.substring(84,108);
                            //在字符串拼接,builder较buffer有速度优势，但在多线程情况下建议用StringBuffer
                            StringBuffer machineId = new StringBuffer();
                            for (int j =0;j<machineId_raw.length();++j){
                                machineId.append(machineId_raw.subSequence(j+1,j+2));
                                j++;
                            }
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "2");
                            updateCmd.setString(4, machineId.toString());
                            //执行sql语句
                            System.out.println(updateCmd);
                            updateCmd.executeUpdate();
                        } else if (data.contains("F0120008")) {
                            //修改主要活动时间
                            //String timeSpan = data.substring(84,100);
                            //builder较buffer有速度优势，但在多线程情况下建议用StringBuffer
                            StringBuffer timeSpan = new StringBuffer();
                            for (int j =84;j<100;++j){
                                timeSpan.append(data.subSequence(j+1,j+2));
                                j++;
                            }
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "5");
                            updateCmd.setString(4, timeSpan.toString());
                            //执行sql语句
                            System.out.println(updateCmd);

                            updateCmd.executeUpdate();
                        } else if (data.contains("F0130001")) {
                            //打开/关闭GPS定位
                            String GPSSwitch = data.substring(85,86);
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "6");
                            updateCmd.setString(4, GPSSwitch);
                            //执行sql语句
                            System.out.println(updateCmd);
                            updateCmd.executeUpdate();
                        }else if (data.contains("F1020001")) {
                            //打开/关闭远程启动 TODO 这里datacontain有问题
                            String fireSwitch = data.substring(85,86);
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "8");
                            updateCmd.setString(4, fireSwitch);
                            //执行sql语句
                            System.out.println(updateCmd);
                            updateCmd.executeUpdate();
                        }

                        else if (data.contains("F0140002")) {
                            //设置休眠间隔
                            //十六进制转为十进制
                            String restTime = Integer.valueOf(data.substring(84,88),16).toString();
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "7");
                            updateCmd.setString(4, restTime);
                            //执行sql语句
                            System.out.println(updateCmd);
                            updateCmd.executeUpdate();
                        }else if (data.contains("F0100010")) {
                            //设置服务器IP 属不可逆操作，又有效率考虑,放在最后。
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? ;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "3");
                            //执行sql语句
                            updateCmd.executeUpdate();
                        } else if (data.contains("F0110006")) {
                            //设置服务器端口 属不可逆操作，又有效率考虑,放在最后。
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? ;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "4");
                            //执行sql语句
                            updateCmd.executeUpdate();
                        }
                    }
                    catch (Exception e){
                        logger.error(e.getMessage());
                    }
                }
            }
        }catch(IOException e){
            logger.error(e.getMessage());
        }finally {
            try {
                logger.info(socket.getInetAddress()+" :connection closed...");
                in.close();
                out.close();
                socket.close();
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
    }

    public static String strToHex(String param){
        /**
         * @Description: 将普通字符串转换为16进制字符串
         * @author: zhuang yao
         * @param:  [param]
         * @return: java.lang.String
         * @Date: 2018/6/20
         */
        String str = "";
        //TODO 提高字符串拼接效率
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < param.length(); i++) {
            int ch = (int) param.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null){
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] toBytes(String str) {
        if(str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for(int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}