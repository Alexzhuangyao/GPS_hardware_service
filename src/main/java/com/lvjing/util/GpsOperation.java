package com.lvjing.util;

import com.lvjing.common.BatteryVoltageReader;
import com.lvjing.common.PositionReader;
import com.lvjing.common.VibrantReader;
import com.lvjing.common.RunTimeReader;
import com.lvjing.dao.DatabaseConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

//EmployeeOperation类用于操作数据库，单例模式。
@Component
public class GpsOperation {

    private static final Logger logger = LoggerFactory.getLogger(GpsOperation.class);

    @Resource
    CRCModbus crcModbus;

    @Resource
    PositionUtil positionUtil;

    PositionReader positionReader = new PositionReader();

    RunTimeReader runTimeReader = new RunTimeReader();

    BatteryVoltageReader batteryVoltageReader = new BatteryVoltageReader();

    VibrantReader vibrantReader = new VibrantReader();

    @Resource
    Matcher matcher;
    @Resource
    Pattern pattern;

    public boolean saveGPSRegister(String param) {   //向数据库中加入数据
        boolean result = false;
        Connection conn = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        //logger.info("注册帧总长度为{}",param.length());

        String header = param.substring(0, 36);
        String deviceId = "D"+param.substring(44,56);
        String signStart = param.substring(56, 66);
        String productKey = param.substring(66, 86);
        String custSn = param.substring(86, 106);   //device
        String deviceSn = param.substring(106, 126);//machine
        String imei = param.substring(126, 164);
        String iccid = param.substring(164, 212);
        String swvsion = param.substring(212, 228);
        String upgradeFlag = param.substring(228, 238);
        String signEnd = param.substring(238, 248);
        String crc = param.substring(248, 260);

        try {
            conn = DatabaseConnection.getCon();  //建立数据库连接
            String sqlInset =
                    "insert into REGISTER_FRAME(header,sn,sign_start,product_key,cust_sn,device_sn,imei,iccid,swvision,upgrade_flag,sign_end,crc,create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

            PreparedStatement stmt = conn.prepareStatement(sqlInset);   //会抛出异常

            stmt.setString(1, header);         //设置SQL语句第一个“？”的值
            stmt.setString(2, deviceId);    //设置SQL语句第二个“？”的值
            stmt.setString(3, signStart);        //设置SQL语句第三个“？”的值
            stmt.setString(4, productKey);     //设置SQL语句第四个“？”的值
            stmt.setString(5, custSn);         //设置SQL语句第5个“？”的值
            stmt.setString(6, deviceSn);    //设置SQL语句第6个“？”的值
            stmt.setString(7, imei);        //设置SQL语句第7个“？”的值
            stmt.setString(8, iccid);     //设置SQL语句第8个“？”的值
            stmt.setString(9, swvsion);         //设置SQL语句第9个“？”的值
            stmt.setString(10, upgradeFlag);    //设置SQL语句第10个“？”的值
            stmt.setString(11, signEnd);        //设置SQL语句第11个“？”的值
            stmt.setString(12, crc);     //设置SQL语句第12个“？”的值
            stmt.setString(13, sdf.format(new Date()));     //设置SQL语句第13个“？”的值

            int i = stmt.executeUpdate();            //执行插入数据操作，返回影响的行数
            if (i == 1) {
                result = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally { //finally的用处是不管程序是否出现异常，都要执行finally语句，所以在此处关闭连接
            try {
                conn.close(); //打开一个Connection连接后，最后一定要调用它的close（）方法关闭连接，以释放系统资源及数据库资源
            } catch(SQLException e) {
                logger.error(e.getMessage());
            }
        }

        return result;

    }


    public boolean saveGPSData(String param) {   //向数据库中加入数据
        boolean result = false;
        Connection conn=null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        //这里采用正则匹配，增加兼容性。
        pattern = Pattern.compile("(.*)(00010006)(.*)(0005000102)(.*)(1790)(.*)(1791)(.*)(1792)(.*)(1793)(.*)(1794)(.*)(1795)(.*)(1796)(.*)(1797)(.*)(00070002)(.*)");
        matcher = pattern.matcher(param);
        System.out.println(matcher.matches());
        String header = matcher.group(1);
        String deviceId = "D"+matcher.group(3);//sn0
        String signStart = "0005000102";
        String stationLatitude = positionReader.transform(matcher.group(7));//基站纬度
        String stationLongitude = positionReader.transform(matcher.group(9));//基站精度
        String GPSLatitude = positionReader.transform(matcher.group(11));//GPS经度
        String GPSLongitude = positionReader.transform(matcher.group(13));//GPS纬度
        String batteryVoltage = batteryVoltageReader.transform(matcher.group(15));//电池电压
        String signalIntensity = matcher.group(17);//信号强度
        String runTime = runTimeReader.transform(matcher.group(19));//步数1796
        String isVibrant = vibrantReader.transform(matcher.group(21));//是否震动
        String signEnd = "0006000103";
        String data_crc = matcher.group(23).substring(0,4);//CRC
        //2018/12/13之前版本
//        String header = param.substring(0, 36);
//        String deviceId = "D" + param.substring(44, 56);
//        String signStart = param.substring(56, 66);
//        String stationLatitude = positionReader.transform(param.substring(66, 98));
//        String stationLongitude = positionReader.transform(param.substring(98, 130));
//        String GPSLatitude = positionReader.transform(param.substring(130, 162));
//        String GPSLongitude = positionReader.transform(param.substring(162, 194));

        stationLatitude = String.valueOf(positionUtil.transform(Double.parseDouble(stationLatitude), Double.parseDouble(stationLongitude)).getWgLat());
        if (stationLatitude.length() > 12) {
            stationLatitude = stationLatitude.substring(0, 12);
        }
        stationLongitude = String.valueOf(positionUtil.transform(Double.parseDouble(stationLatitude), Double.parseDouble(stationLongitude)).getWgLon());
        if (stationLongitude.length() > 12) {
            stationLongitude = stationLongitude.substring(0, 12);
        }
        GPSLatitude = String.valueOf(positionUtil.transform(Double.parseDouble(GPSLatitude), Double.parseDouble(GPSLongitude)).getWgLat());
        if (GPSLatitude.length() > 12) {
            GPSLatitude = GPSLatitude.substring(0, 12);
        }
        GPSLongitude = String.valueOf(positionUtil.transform(Double.parseDouble(GPSLatitude), Double.parseDouble(GPSLongitude)).getWgLon());
        if (GPSLongitude.length() > 12) {
            GPSLongitude = GPSLongitude.substring(0, 12);
        }

//        String batteryVoltage = batteryVoltageReader.transform(param.substring(194, 222));
//        String signalIntensity = param.substring(230, 234);//1795:信号强度
//        String runTime = param.substring(238, 252);//1796:步数
//        String isVibrant = vibrantReader.transform(param.substring(252, 262));
//        String data_crc = param.substring(280, 284);
        signalIntensity = (signalIntensity.substring(5, 6) + signalIntensity.substring(7, 8));
        int signallevel = Integer.parseInt(signalIntensity);
        //判断信号强弱
        if (signallevel >= 30) {
            signallevel = 4;
        } else if (signallevel >= 15) {
            signallevel = 3;
        } else if (signallevel >= 10) {
            signallevel = 2;
        } else if (signallevel >= 2) {
            signallevel = 1;
        } else {
            signallevel = 0;
        }
        try {
            conn = DatabaseConnection.getCon();  //建立数据库连接
            //在DATA_FRAME中添加数据帧记录
            String Insert =
                    "INSERT into DATA_FRAME(header,sn,sign_start,station_latitude,station_longitude,GPS_latitude,GPS_longitude," +
                            "battery_voltage,run_time,signal_intensity,is_vibrant,sign_end,crc,create_time) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            //在USER和DEVICE_USER_RELATIONSHIP表中找出指定device_id对应的USER表中对应的user_name,即最后使用人
            String GetLastUserId = "SELECT user_name FROM `USER` WHERE user_number=(SELECT user_id FROM DEVICE_USER_RELATIONSHIP WHERE device_id = ?)";
            //非正常数据帧，更新DEVICE_STATUS表中信号，电量，运行时间，最后使用人，更新时间，是否在线，是否震动
            String Update0 =
                    "UPDATE DEVICE_STATUS SET signal_intensity=?," +
                            "battery_voltage=?,run_time=?,last_user=?,update_time=?,is_online = ?,is_vibrant = ? where device_id=?";
            //正常数据帧，更新DEVICE_STATUS表中信号，电量，运行时间，最后使用人，更新时间，是否在线，是否震动
            String Update =
                    "UPDATE DEVICE_STATUS SET station_latitude=?,station_longitude=?,GPS_latitude=?,GPS_longitude=?,signal_intensity=?," +
                            "battery_voltage=?,run_time=?,last_user=?,update_time=?,is_online = ?,is_vibrant = ? where device_id=?";

            PreparedStatement stmt = conn.prepareStatement(Insert);   //若错，会抛出异常
            PreparedStatement stmg = conn.prepareStatement(GetLastUserId);   //若错，会抛出异常
            stmg.setString(1, deviceId); //**执行stmg获取lastUser**/
            ResultSet lastUser = stmg.executeQuery();

            //非正常数据帧更新DATA_STATUS表
            PreparedStatement stmu0 = conn.prepareStatement(Update0);   //若错，会抛出异常
            //正常数据帧更新DATA_STATUS表
            PreparedStatement stmu = conn.prepareStatement(Update);   //若错，会抛出异常

            stmt.setString(1, header);         //设置SQL语句第一个“？”的值
            stmt.setString(2, deviceId);    //设置SQL语句第二个“？”的值
            stmt.setString(3, signStart);        //设置SQL语句第三个“？”的值
            stmt.setString(4, stationLatitude);     //设置SQL语句第四个“？”的值
            stmt.setString(5, stationLongitude);         //设置SQL语句第5个“？”的值
            stmt.setString(6, GPSLatitude);    //设置SQL语句第6个“？”的值
            stmt.setString(7, GPSLongitude);        //设置SQL语句第7个“？”的值
            stmt.setString(8, batteryVoltage);     //设置SQL语句第8个“？”的值
            stmt.setString(9, runTime);         //设置SQL语句第9个“？”的值
            stmt.setString(10, String.valueOf(signallevel));    //设置SQL语句第10个“？”的值
            stmt.setString(11, isVibrant);    //设置SQL语句第11个“？”的值
            stmt.setString(12, signEnd);        //设置SQL语句第12个“？”的值
            stmt.setString(13, data_crc);     //设置SQL语句第13个“？”的值
            stmt.setString(14, sdf.format(new Date()));     //设置SQL语句第14个“？”的值

            if (stationLatitude.equals("0.0") && stationLongitude.equals("0.0")
                    && GPSLatitude.equals("0.0") && GPSLongitude.equals("0.0")) {
                //**非正常数数据帧更新设备状态表**/
                stmu0.setString(1, String.valueOf(signallevel));//signal_intensity
                stmu0.setString(2, batteryVoltage);//battery_voltage
                stmu0.setString(3, runTime);//run_time
                stmu0.setString(4, "");
                stmu0.setString(8, deviceId);//device_id
                stmu0.setString(5, sdf.format(new Date()));//update_time
                stmu0.setString(6, "1");//is_online
                stmu0.setString(7, isVibrant);//is_vibrant
                stmu0.executeUpdate();
            } else {
                //**正常数据帧更新设备状态表**/
                stmu.setString(1, stationLatitude);//station_latitude
                stmu.setString(2, stationLongitude);//station_longitude
                stmu.setString(3, GPSLatitude);//GPS_latitude
                stmu.setString(4, GPSLongitude);//GPS_longitude
                stmu.setString(5, String.valueOf(signallevel));//signal_intensity
                stmu.setString(6, batteryVoltage);//battery_voltage
                stmu.setString(7, runTime);//run_time
                stmu.setString(8, "");
                stmu.setString(12, deviceId);//device_id
                stmu.setString(9, sdf.format(new Date()));//update_time
                stmu.setString(10, "1");//is_online
                stmu.setString(11, isVibrant);//is_vibrant
                while (lastUser.next()) {
                    if (!lastUser.getString(1).equals("") && lastUser.getString(1) != null) {
                        stmu.setString(8, lastUser.getString(1));//last_user
                    } else {
                        stmu.setString(8, "");
                    }
                }
                stmu.executeUpdate();
            }
            int i = stmt.executeUpdate();            //执行插入数据操作，返回影响的行数
            if (i == 1) {
                result = true;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error(e.getSQLState());
            logger.error(e.getMessage());
        } finally { //finally的用处是不管程序是否出现异常，都要执行finally语句，所以在此处关闭连接
            try {
                conn.close(); //打开一个Connection连接后，最后一定要调用它的close（）方法关闭连接，以释放系统资源及数据库资源
            } catch (SQLException e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }

    public boolean saveCheckTime(String param) {   //向数据库中加入数据
        boolean result = false;
        Connection conn = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        //logger.info("注册帧总长度为{}",param.length());
        String header = param.substring(0, 36);
        String deviceId = "D"+param.substring(44,56);
        String signStart = param.substring(56, 66);
        String commuteTime_raw = param.substring(74, 90);//通勤时间
        String intervals_raw = param.substring(92, 102);//间隔时间
        String GPSSwitch = param.substring(105, 106);//case 30 31
        String fireSwitch = param.substring(109, 110);//case 30 31
        if (fireSwitch.equals("30")){
            fireSwitch = "0";
        }else if(fireSwitch.equals("31")){
            fireSwitch = "1";
        }
        //隔位切出commuteTime
        String commuteTime  ="";
        for (int j =0;j<commuteTime_raw.length();++j){
            commuteTime +=commuteTime_raw.substring(j+1,j+2);
            j++;
        }
        //隔位切出intervals
        String intervals  ="";
        for (int j =0;j<intervals_raw.length();++j){
            intervals +=intervals_raw.substring(j+1,j+2);
            j++;
        }
        try {
            conn = DatabaseConnection.getCon();  //建立数据库连接
            String sqlUpdate =
                    "UPDATE DEVICE_STATUS SET commute_time = ? , sleep_time =  ? , GPS_switch = ? ,fire_switch = ? , check_time = ?  where device_id=?";

            PreparedStatement stmt = conn.prepareStatement(sqlUpdate);   //会抛出异常

            stmt.setString(1, commuteTime);         //设置SQL语句第一个“？”的值
            stmt.setString(2, intervals);    //sleep_time
            stmt.setString(3, GPSSwitch);        //设置SQL语句第三个“？”的值
            stmt.setString(4, fireSwitch);      //点火状态
            stmt.setString(5, sdf.format(new Date()));    //当前时间
            stmt.setString(6, deviceId);         //设备ID

            int i = stmt.executeUpdate();            //执行插入数据操作，返回影响的行数
            if (i == 1) {
                result = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally { //finally的用处是不管程序是否出现异常，都要执行finally语句，所以在此处关闭连接
            try {
                conn.close(); //打开一个Connection连接后，最后一定要调用它的close（）方法关闭连接，以释放系统资源及数据库资源
            } catch(Exception e) {
                logger.error(e.getMessage());
            }
        }

        return result;

    }

}