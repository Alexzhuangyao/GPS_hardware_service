package com.lvjing.util;

import com.lvjing.dao.DatabaseConnection;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author:HuangHua
 * @Descirption: 16进制字符串处理
 * @Date: Created by huanghua on 2018/6/11.
 * @Modified By:
 */
@Component
public class HexDecoder {

    @Resource
    CRCModbus crcModbus = new CRCModbus();

    private static Logger logger = LoggerFactory.getLogger(HexDecoder.class);

    public String readRegisterFrame(String param) {
        /**
         * @Description: 读取注册帧
         * @author: Huang Hua
         * @param: [param]
         * @return: java.lang.String
         * @Date: 2018/6/11
         */

        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
        //logger.info("注册帧总长度为{}",param.length());
        if (StringUtils.isNotBlank(param)) {
            String header = param.substring(0, 36);
            String sn = param.substring(36, 56);
            String signStart = param.substring(56, 66);
            String productKey = param.substring(66, 86);
            String custSn = param.substring(86, 106);
            String deviceSn = param.substring(106, 126);
            String imei = param.substring(126, 164);
            String iccid = param.substring(164, 212);
            String swvsion = param.substring(212, 228);
            String upgradeFlag = param.substring(228, 238);
            String signEnd = param.substring(238, 248);
            String crc = param.substring(256, 260);
            String result_crc = "";//返回的数据的crc
            //byte[] result_byte= new byte[124];
            result = "7E7E232355544E110001000000000000003E" + sn + "0005000102" + "00040006" + sdf.format(new Date()) + "0008000400000000" + "000600011B";
            //System.out.println(result);
            result_crc = crcModbus.getCRC(toBytes(result));
            //System.out.println(result_crc);
            result = result + "00070002" + result_crc;
            //System.out.println(result);
            //logger.info("7E7E232355544E110001000000000000003E"+sn.toString()+"0005000102"+"00040006"+sdf.format(new Date()).toString()+"0008000400000000"+"000600011B"+"00070002"+result_crc.toString());
            //result = result_byte.toString();
        }
        //System.out.println(result);
        return result;
    }

    public String readDataFrame(String param) {
        String result = "";
        //logger.info("数据帧总长度为{}",param.length());

        if (StringUtils.isNotBlank(param)) {
//            String header = param.substring(0, 36);
            String sn = param.substring(36, 56);
//            String signStart = param.substring(56, 66);
            //String data = param.substring(66,280);
//            String stationLatitude = param.substring(66, 98);
//            String stationLongitude = param.substring(98, 130);
//            String GPSLatitude = param.substring(130, 162);
//            String GPSLongititude = param.substring(162, 194);
//            String batteryVoltage = param.substring(194, 222);
//            String runTime = param.substring(222, 238);
//            String signalIntensity = param.substring(238, 252);
//            String isVibrant = param.substring(252, 262);
//            String signEnd = param.substring(262, 272);
            String data_crc = param.substring(280, 284);
            String data_need_judge = param.substring(0, 272);//原数据中未含crc数据
            String crc_judge = "";//根据原数据产生的crc
            String crc_judged_result = "";//根据原数据产生的crc==数据中的crc 00失败，01成功。
            String result_data_crc = "";//返回的数据的crc
            byte[] pre_bytes = new byte[1024];
            //pre_bytes = crcModbus.preHandler(data_need_judge);
            crc_judge = crcModbus.getCRC(toBytes(data_need_judge));

            if (data_crc.equalsIgnoreCase(crc_judge)) {
                crc_judged_result = "01";
            } else {
                crc_judged_result = "00";
                logger.error("本次数据帧CRC校验出错！ 数据帧：" + param);
            }
            ;
            String result1 = "";//未含crc的返回数据
            result1 = ("7E7E232355544EEE00010000000000000031" + sn + "0005000102" + "000e0001" + crc_judged_result.toString() + "000600011B");

            //byte[] result_crc_pre = new byte[1024];
            //result_crc_pre = crcModbus.preHandler(result1);
            result_data_crc = crcModbus.getCRC(toBytes(result1));
            //logger.info(result1+result_crc.toString());
            result = result1 + "00070002" + result_data_crc;
        }

        //System.out.println(result);
        return result;

    }

    //发送升级指令
    public String sendUpgradeCommand(String param) {
        String result = "";
        if (StringUtils.isNotBlank(param)) {
            String sn = param.substring(36, 56);
            String result_crc = "";//返回的数据的crc
            result = "7E7E232355544E1400010000000000000031" + sn + "0005000102F0010001010006000102";
            result_crc = crcModbus.getCRC(toBytes(result));
            result = result + "00070002" + result_crc;
            System.out.println(result);
        }
        return result;
    }

    //发送修改设备参数修改指令
    public String sendDeviceParamConfigChangeCommand(String param) {
        String result = "";
        String result0 = "";
        String result_crc = "";//返回的数据的crc
        if (StringUtils.isNotBlank(param)) {
            //获取param中的sn
            String sn = param.substring(36, 56);
            String deviceId = "D" + param.substring(44, 56);
            Connection conn = null;
            try {
                //建立数据库连接
                conn = DatabaseConnection.getCon();
                //查询所有参数修改项
                String configCommand = "SELECT operation_type,content FROM DEVICE_PARAM_CONFIG WHERE device_id = ? and ord_status = '0';";

                PreparedStatement stmt = conn.prepareStatement(configCommand);
                stmt.setString(1, deviceId);
                System.out.println(stmt);
                //执行语句后获取结果
                ResultSet resultSet = stmt.executeQuery();
                Integer i = 0;
                String str0 = "";
                String hex = "";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                while (resultSet.next()) {
                    //System.out.println(resultSet.getString(1));
                    switch (resultSet.getString(1)){
                        case "1":
                            //在线升级，不考虑第二个column
                            //升级成功
                            String cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? ;";
                            PreparedStatement updateCmd = conn.prepareStatement(cmd);
                            updateCmd.setString(1, sdf.format(new Date()));
                            updateCmd.setString(2, deviceId);
                            updateCmd.setString(3, "1");
                            //执行sql语句
                            System.out.println(updateCmd);
                            updateCmd.executeUpdate();
                            return sendUpgradeCommand(param);
                        case "2":
                            //绑定机器即machineId,考虑在GPS_wx_service中在设备绑定时添加D_P_C的新纪录
                            String extra = "";
                            String machineId = resultSet.getString(2);
                            if (machineId.startsWith("M")){
                                machineId = machineId.substring(1);
                                extra = "M";
                            }
                            char[] sArray = machineId.toCharArray();
                            String machine = "";
                            System.out.println(sArray[0]);
                            for (char s:sArray) {
                                machine +="3"+s;
                            }
                            str0 = "7E7E232355544EEF0001000000000000003C" + sn + "0005000102F015000C"+machine+"0006000103";
                            result_crc = crcModbus.getCRC(toBytes(str0));
                            result = str0 + "00070002" + result_crc;
                            cmd = "UPDATE DEVICE_STATUS SET bind_machine = ? ,update_time = ? WHERE device_id = ? ;";
                            PreparedStatement updateMachineCmd = conn.prepareStatement(cmd);
                            updateMachineCmd.setString(1, extra+machineId);
                            updateMachineCmd.setString(2, sdf.format(new Date()));
                            updateMachineCmd.setString(3, deviceId);
                            //执行sql语句
                            System.out.println(updateMachineCmd);
                            updateMachineCmd.executeUpdate();
                            break;
                        case "3":
                            //设置服务器IP，默认39.106.10.110
                            String ip = resultSet.getString(2);
                            char[] ipArray = ip.toCharArray();
                            String target_ip = "";
                            //System.out.println(ipArray[0]);
                            for (char s:ipArray) {
                                target_ip +="3"+s;
                            }
                            target_ip = target_ip+"0000000000";
                            target_ip = target_ip.replaceAll("3\\.","2E");
                            target_ip = target_ip.substring(0,34);
                            str0 = "7E7E232355544EEF00010000000000000041" + sn + "0005000102F0100010"+target_ip+"0006000103";
                            result_crc = crcModbus.getCRC(toBytes(str0));
                            result = str0 + "00070002" + result_crc;
                            //设置服务器IP 属不可逆操作。
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? ;";
                            PreparedStatement updateIPCmd = conn.prepareStatement(cmd);
                            updateIPCmd.setString(1, sdf.format(new Date()));
                            updateIPCmd.setString(2, deviceId);
                            updateIPCmd.setString(3, "3");
                            //执行sql语句
                            updateIPCmd.executeUpdate();
                            break;
                        case "4":
                            //设置服务器端口，默认8888
                            String port = resultSet.getString(2);
                            char[] portArray = port.toCharArray();
                            String target_port = "";
                            for (char s:portArray) {
                                target_port +="3"+s;
                            }
                            target_port = target_port+"0000000000";
                            target_port = target_port.substring(0,12);
                            str0 = "7E7E232355544EEF00010000000000000036" + sn + "0005000102F0110006"+target_port+"0006000103";
                            result_crc = crcModbus.getCRC(toBytes(str0));
                            result = str0 + "00070002" + result_crc;
                            //设置服务器端口 属不可逆操作，又有效率考虑,放在最后。
                            cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 2 ,update_time = ? WHERE device_id = ? and operation_type= ? ;";
                            PreparedStatement updatePortCmd = conn.prepareStatement(cmd);
                            updatePortCmd.setString(1, sdf.format(new Date()));
                            updatePortCmd.setString(2, deviceId);
                            updatePortCmd.setString(3, "4");
                            //执行sql语句
                            updatePortCmd.executeUpdate();
                            break;
                        case "5":
                            //设置活动时间,08001600
                            String workTime = resultSet.getString(2);
                            char[] timeArray = workTime.toCharArray();
                            String work_time = "";
                            System.out.println(timeArray[0]);
                            for (char s:timeArray) {
                                work_time +="3"+s;
                            }
                            str0 = "7E7E232355544EEF00010000000000000038" + sn + "0005000102F0120008"+work_time+"0006000103";
                            result_crc = crcModbus.getCRC(toBytes(str0));
                            result = str0 + "00070002" + result_crc;
                            cmd = "UPDATE DEVICE_STATUS SET commute_time = ? ,update_time = ? WHERE device_id = ? ;";
                            PreparedStatement updateWorkTimeCmd = conn.prepareStatement(cmd);
                            updateWorkTimeCmd.setString(1, workTime);
                            updateWorkTimeCmd.setString(2, sdf.format(new Date()));
                            updateWorkTimeCmd.setString(3, deviceId);
                            //执行sql语句
                            updateWorkTimeCmd.executeUpdate();
                            //workTime1Cmd 用以修改当前状态，从0->1即从0未修改变为1正在修改状态；
                            String workTime1Cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 1 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateWorkTime1Cmd = conn.prepareStatement(workTime1Cmd);
                            updateWorkTime1Cmd.setString(1, sdf.format(new Date()));
                            updateWorkTime1Cmd.setString(2, deviceId);
                            updateWorkTime1Cmd.setString(3, "5");
                            updateWorkTime1Cmd.setString(4, workTime);//工作时间
                            //执行sql语句
                            updateWorkTime1Cmd.executeUpdate();
                            break;
                        case "6":
                            //GPS开关
                            String GPSSwitch = resultSet.getString(2);
                            switch (GPSSwitch){
                                case "1":
                                    str0 = "7E7E232355544EEF00010000000000000031" + sn + "0005000102F0130001310006000103";
                                    result_crc = crcModbus.getCRC(toBytes(str0));
                                    result = str0 + "00070002" + result_crc;
                                    break;
                                case "0":
                                    str0 = "7E7E232355544EEF00010000000000000031" + sn + "0005000102F0130001300006000103";
                                    result_crc = crcModbus.getCRC(toBytes(str0));
                                    result = str0 + "00070002" + result_crc;
                                    break;
                            }
                            cmd = "UPDATE DEVICE_STATUS SET GPS_switch = ? ,update_time = ? WHERE device_id = ? ;";
                            PreparedStatement updateGPSSwitchCmd = conn.prepareStatement(cmd);
                            updateGPSSwitchCmd.setString(1, GPSSwitch);
                            updateGPSSwitchCmd.setString(2, sdf.format(new Date()));
                            updateGPSSwitchCmd.setString(3, deviceId);
                            //执行sql语句
                            updateGPSSwitchCmd.executeUpdate();
                            //System.out.println(updateGPSSwitchCmd);
                            //workTime1Cmd 用以修改当前状态，从0->1即从0未修改变为1正在修改状态
                            String GPSSwitch1Cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 1 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateGPSSwitch1Cmd = conn.prepareStatement(GPSSwitch1Cmd);
                            updateGPSSwitch1Cmd.setString(1, sdf.format(new Date()));
                            updateGPSSwitch1Cmd.setString(2, deviceId);
                            updateGPSSwitch1Cmd.setString(3, "6");
                            updateGPSSwitch1Cmd.setString(4, GPSSwitch);
                            break;
                        case "7":
                            //设置休眠间隔，单位秒,如1200
                            String restTime = resultSet.getString(2);
                            i = Integer.parseInt(restTime);
                            hex = i.toHexString(i).toUpperCase();
                            //左填充
                            hex = ("0000"+hex);
                            hex = hex.substring(hex.length()-4);
                            str0 = "7E7E232355544EEF00010000000000000032" + sn + "0005000102F0140002" + hex + "0006000103";
                            result_crc = crcModbus.getCRC(toBytes(str0));
                            result = str0 + "00070002" + result_crc;
                            cmd = "UPDATE DEVICE_STATUS SET sleep_time = ?  WHERE device_id = ? ;";
                            PreparedStatement updateRestTimeCmd = conn.prepareStatement(cmd);
                            updateRestTimeCmd.setString(1, restTime);
                            updateRestTimeCmd.setString(2, deviceId);
                            //执行sql语句
                            updateRestTimeCmd.executeUpdate();
                            //System.out.println(updateRestTimeCmd);
                            //workTime1Cmd 用以修改当前状态，从0->1即从0未修改变为1正在修改状态
                            String RestTime1Cmd = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 1 ,update_time = ? WHERE device_id = ? and operation_type= ? and content = ?;";
                            PreparedStatement updateRestTime1Cmd = conn.prepareStatement(RestTime1Cmd);
                            updateRestTime1Cmd.setString(1, sdf.format(new Date()));
                            updateRestTime1Cmd.setString(2, deviceId);
                            updateRestTime1Cmd.setString(3, "7");
                            updateRestTime1Cmd.setString(4, restTime);
                            break;
                        case "8":
                            //远程启动开关 1为开 2为关
                            String fireSwitch = resultSet.getString(2);
                            String fireSwitch_state = "";
                            switch (fireSwitch){
                                case "1":
                                    str0 = "7E7E232355544EEF00010000000000000031" + sn + "0005000102F1020001310006000103";
                                    result_crc = crcModbus.getCRC(toBytes(str0));
                                    result = str0 + "00070002" + result_crc;
                                    fireSwitch_state = "1";
                                    break;
                                case "0":
                                    str0 = "7E7E232355544EEF00010000000000000031" + sn + "0005000102F1020001300006000103";
                                    result_crc = crcModbus.getCRC(toBytes(str0));
                                    result = str0 + "00070002" + result_crc;
                                    fireSwitch_state = "0";
                                    break;
                            }
                            cmd = "UPDATE DEVICE_STATUS SET fire_switch = ? WHERE device_id = ? ;";
                            PreparedStatement updateStartSwitchCmd = conn.prepareStatement(cmd);
                            updateStartSwitchCmd.setString(1, fireSwitch_state);
                            updateStartSwitchCmd.setString(2, deviceId);
                            //执行sql语句
                            System.out.println(updateStartSwitchCmd);
                            updateStartSwitchCmd.executeUpdate();
                            String cmd1 = "UPDATE DEVICE_PARAM_CONFIG SET ord_status = 1 ,update_time =?  WHERE device_id = ? and operation_type= ? and content = ? ;";
                            PreparedStatement updateCmd1 = conn.prepareStatement(cmd1);
                            updateCmd1.setString(1, sdf.format(new Date()));
                            updateCmd1.setString(2, deviceId);
                            updateCmd1.setString(3, "8");
                            updateCmd1.setString(4, fireSwitch);
                            //执行sql语句
                            System.out.println(updateCmd1);
                            updateCmd1.executeUpdate();
                            break;
                    }
                    break;
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        return result;
    }


    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }
    public static String bytes2HexString(byte[] b) {
        String ret = "";
        for (int i = 0; i < b.length; i++) {
            String hex = Integer.toHexString(b[ i ] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            ret += hex.toUpperCase();
        }
        return ret;
    }
}


