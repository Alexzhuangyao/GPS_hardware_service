package com.lvjing.socket;

import com.lvjing.common.BatteryVoltageReader;
import com.lvjing.common.PositionReader;
import com.lvjing.common.VibrantReader;
import com.lvjing.dao.DatabaseConnection;
import com.lvjing.util.GpsOperation;
import com.lvjing.util.HexDecoder;
import com.lvjing.util.PositionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;


/**
 * @Author:zy
 * @Descirption:
 * @Date: Created by zy on 2018/6/15.
 * @Modified By:
 */
public class ServerThread implements Runnable {

    HexDecoder hexDecoder = new HexDecoder();

    GpsOperation gpsOperation = new GpsOperation();

    private static final Logger logger = LoggerFactory.getLogger(ServerThread.class);

    private Socket socket = null;
    private InputStream in = null;
    private OutputStream out = null;
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
                logger.info("bytes：",bytes);
                logger.info("data：",data);//data
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
                    case 142:
                        logger.info("数据帧：",data);
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
                //对时帧
                if (len == 76 || len == 74){
                    gpsOperation.saveCheckTime(data);
                    try {
                        //判断DEVICE_PARAM_CONFIG表内是否有需要修改的参数,有则下发第一个。
                        dvc = hexDecoder.sendDeviceParamConfigChangeCommand(data);
                        out.write(toBytes(dvc));
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

    @Resource
    PositionReader positionReader;
    BatteryVoltageReader batteryVoltageReader;
    VibrantReader vibrantReader = new VibrantReader();
    PositionUtil positionUtil;

//    public boolean saveGPSData(String param) {   //向数据库中加入数据
//        boolean result = false;
//        Connection conn = null;
//        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
//        logger.info("数据帧:",param);
//        logger.info("数据帧:",param.getBytes());
//        logger.info(" len",param.length());
//        //这里采用正则匹配，增加兼容性。
//        Pattern pattern = Pattern.compile("(.*)(00010006)(.*)(00016000)(.*)(1790)(.*)(1791)(.*)(1792)(.*)(1793)(.*)(1794)(.*)(1795)(.*)(1796)(.*)(1797)(.*)(00070002)(.*)");
//        Matcher matcher = pattern.matcher(param);
//
//        String header = matcher.group(1);
//        String deviceId = "D"+matcher.group(3);//sn
//        String signStart = "0005000102";
//        String stationLatitude = positionReader.transform(matcher.group(7));//基站纬度
//        String stationLongitude = positionReader.transform(matcher.group(9));//基站精度
//        String GPSLatitude = positionReader.transform(matcher.group(11));//GPS经度
//        String GPSLongitude = positionReader.transform(matcher.group(13));//GPS纬度
//        String batteryVoltage = batteryVoltageReader.transform(matcher.group(15));//电池电压
//        String signalIntensity = matcher.group(17);//信号强度
//        String runTime = matcher.group(19);//步数
//        String isVibrant = vibrantReader.transform(matcher.group(21));//是否震动
//        String signEnd = "0006000103";
//        String data_crc = matcher.group(23);//CRC
//        //2018/12/13之前版本
////        String header = param.substring(0, 36);
////        String deviceId = "D" + param.substring(44, 56);
////        String signStart = param.substring(56, 66);
////        String stationLatitude = positionReader.transform(param.substring(66, 98));
////        String stationLongitude = positionReader.transform(param.substring(98, 130));
////        String GPSLatitude = positionReader.transform(param.substring(130, 162));
////        String GPSLongitude = positionReader.transform(param.substring(162, 194));
//
//        stationLatitude = String.valueOf(positionUtil.transform(Double.parseDouble(stationLatitude), Double.parseDouble(stationLongitude)).getWgLat());
//        if (stationLatitude.length() > 12) {
//            stationLatitude = stationLatitude.substring(0, 12);
//        }
//        stationLongitude = String.valueOf(positionUtil.transform(Double.parseDouble(stationLatitude), Double.parseDouble(stationLongitude)).getWgLon());
//        if (stationLongitude.length() > 12) {
//            stationLongitude = stationLongitude.substring(0, 12);
//        }
//        GPSLatitude = String.valueOf(positionUtil.transform(Double.parseDouble(GPSLatitude), Double.parseDouble(GPSLongitude)).getWgLat());
//        if (GPSLatitude.length() > 12) {
//            GPSLatitude = GPSLatitude.substring(0, 12);
//        }
//        GPSLongitude = String.valueOf(positionUtil.transform(Double.parseDouble(GPSLatitude), Double.parseDouble(GPSLongitude)).getWgLon());
//        if (GPSLongitude.length() > 12) {
//            GPSLongitude = GPSLongitude.substring(0, 12);
//        }
//
////        String batteryVoltage = batteryVoltageReader.transform(param.substring(194, 222));
////        String signalIntensity = param.substring(230, 234);//1795:信号强度
////        String runTime = param.substring(238, 252);//1796:步数
////        String isVibrant = vibrantReader.transform(param.substring(252, 262));
////        String data_crc = param.substring(280, 284);
//        signalIntensity = (signalIntensity.substring(1, 2) + signalIntensity.substring(3, 4));
//        int signallevel = Integer.parseInt(signalIntensity);
//        //判断信号强弱
//        if (signallevel >= 30) {
//            signallevel = 4;
//        } else if (signallevel >= 15) {
//            signallevel = 3;
//        } else if (signallevel >= 10) {
//            signallevel = 2;
//        } else if (signallevel >= 2) {
//            signallevel = 1;
//        } else {
//            signallevel = 0;
//        }
//
//        try {
//            conn = DatabaseConnection.getCon();  //建立数据库连接
//            //在DATA_FRAME中添加数据帧记录
//            String Insert =
//                    "INSERT into DATA_FRAME(header,sn,sign_start,station_latitude,station_longitude,GPS_latitude,GPS_longitude," +
//                            "battery_voltage,run_time,signal_intensity,is_vibrant,sign_end,crc,create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
//            //在USER和DEVICE_USER_RELATIONSHIP表中找出指定device_id对应的USER表中对应的user_name,即最后使用人
//            String GetLastUserId = "SELECT user_name FROM `USER` WHERE user_number=(SELECT user_id FROM DEVICE_USER_RELATIONSHIP WHERE device_id = ?)";
//            //非正常数据帧，更新DEVICE_STATUS表中信号，电量，运行时间，最后使用人，更新时间，是否在线，是否震动
//            String Update0 =
//                    "UPDATE DEVICE_STATUS SET signal_intensity=?," +
//                            "battery_voltage=?,run_time=?,last_user=?,update_time=?,is_online = ?,is_vibrant = ? where device_id=?";
//            //正常数据帧，更新DEVICE_STATUS表中信号，电量，运行时间，最后使用人，更新时间，是否在线，是否震动
//            String Update =
//                    "UPDATE DEVICE_STATUS SET station_latitude=?,station_longitude=?,GPS_latitude=?,GPS_longitude=?,signal_intensity=?," +
//                            "battery_voltage=?,run_time=?,last_user=?,update_time=?,is_online = ?,is_vibrant = ? where device_id=?";
//
//            PreparedStatement stmt = conn.prepareStatement(Insert);   //若错，会抛出异常
//            PreparedStatement stmg = conn.prepareStatement(GetLastUserId);   //若错，会抛出异常
//            stmg.setString(1, deviceId); //**执行stmg获取lastUser**/
//            ResultSet lastUser = stmg.executeQuery();
//
//            //非正常数据帧更新DATA_STATUS表
//            PreparedStatement stmu0 = conn.prepareStatement(Update0);   //若错，会抛出异常
//            //正常数据帧更新DATA_STATUS表
//            PreparedStatement stmu = conn.prepareStatement(Update);   //若错，会抛出异常
//
//            stmt.setString(1, header);         //设置SQL语句第一个“？”的值
//            stmt.setString(2, deviceId);    //设置SQL语句第二个“？”的值
//            stmt.setString(3, signStart);        //设置SQL语句第三个“？”的值
//            stmt.setString(4, stationLatitude);     //设置SQL语句第四个“？”的值
//            stmt.setString(5, stationLongitude);         //设置SQL语句第5个“？”的值
//            stmt.setString(6, GPSLatitude);    //设置SQL语句第6个“？”的值
//            stmt.setString(7, GPSLongitude);        //设置SQL语句第7个“？”的值
//            stmt.setString(8, batteryVoltage);     //设置SQL语句第8个“？”的值
//            stmt.setString(9, runTime);         //设置SQL语句第9个“？”的值
//            stmt.setString(10, String.valueOf(signallevel));    //设置SQL语句第10个“？”的值
//            stmt.setString(11, isVibrant);    //设置SQL语句第11个“？”的值
//            stmt.setString(12, signEnd);        //设置SQL语句第12个“？”的值
//            stmt.setString(13, data_crc);     //设置SQL语句第13个“？”的值
//            stmt.setString(14, sdf.format(new Date()));     //设置SQL语句第14个“？”的值
//
//            if (stationLatitude.equals("0.0") && stationLongitude.equals("0.0")
//                    && GPSLatitude.equals("0.0") && GPSLongitude.equals("0.0")) {
//                //**非正常数数据帧更新设备状态表**/
//                stmu0.setString(1, String.valueOf(signallevel));//signal_intensity
//                stmu0.setString(2, batteryVoltage);//battery_voltage
//                stmu0.setString(3, runTime);//run_time
//                stmu0.setString(4, "");
//                stmu0.setString(8, deviceId);//device_id
//                stmu0.setString(5, sdf.format(new Date()));//update_time
//                stmu0.setString(6, "1");//is_online
//                stmu0.setString(7, isVibrant);//is_vibrant
//                stmu0.executeUpdate();
//            } else {
//                //**正常数据帧更新设备状态表**/
//                stmu.setString(1, stationLatitude);//station_latitude
//                stmu.setString(2, stationLongitude);//station_longitude
//                stmu.setString(3, GPSLatitude);//GPS_latitude
//                stmu.setString(4, GPSLongitude);//GPS_longitude
//                stmu.setString(5, String.valueOf(signallevel));//signal_intensity
//                stmu.setString(6, batteryVoltage);//battery_voltage
//                stmu.setString(7, runTime);//run_time
//                stmu.setString(8, "");
//                stmu.setString(12, deviceId);//device_id
//                stmu.setString(9, sdf.format(new Date()));//update_time
//                stmu.setString(10, "1");//is_online
//                stmu.setString(11, isVibrant);//is_vibrant
//                while (lastUser.next()) {
//                    if (!lastUser.getString(1).equals("") && lastUser.getString(1) != null) {
//                        stmu.setString(8, lastUser.getString(1));//last_user
//                    } else {
//                        stmu.setString(8, "");
//                    }
//                }
//                stmu.executeUpdate();
//            }
//            int i = stmt.executeUpdate();            //执行插入数据操作，返回影响的行数
//            if (i == 1) {
//                result = true;
//            }
//        } catch (SQLException e) {
//            // TODO Auto-generated catch block
//            logger.error(e.getSQLState());
//            logger.error(e.getMessage());
//        } finally { //finally的用处是不管程序是否出现异常，都要执行finally语句，所以在此处关闭连接
//            try {
//                conn.close(); //打开一个Connection连接后，最后一定要调用它的close（）方法关闭连接，以释放系统资源及数据库资源
//            } catch (SQLException e) {
//                logger.error(e.getMessage());
//            }
//        }
//        return result;
//    }
}