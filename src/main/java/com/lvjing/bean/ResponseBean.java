package com.lvjing.bean;

/**
 * @Author:HuangHua
 * @Descirption: 服务接口返回bean
 * @Date: Created by huanghua on 2018/6/5.
 * @Modified By:
 */
public class ResponseBean {

    /**状态码:0为成功,-1为失败**/
    private int code;

    /**返回信息**/
    private String msg;

    /**返回的数据**/
    private Object data;

    /**默认构造函数**/
    public ResponseBean(int code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
