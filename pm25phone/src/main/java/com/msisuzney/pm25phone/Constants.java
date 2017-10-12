package com.msisuzney.pm25phone;

/**
 * Created by chenxin.
 * Date: 2017/10/12.
 * Time: 16:54.
 */

public class Constants {

    public static final String multicastHost = "224.0.0.1";//多播地址
    public static final int multicastHostPort = 8003;//多播地址端口

    //client,接受PM数据端口
    public static final int sReceivePMPort = 7930;
    public static final String sDataFormat = "PMClient,port:";
    public static final String sSendContent = sDataFormat + sReceivePMPort;
    public static final String sStopContent = "PMClient,stop";

    public static final String isRegister = "isRegister";
    public static final String msisuzney = "msisuzney";

}
