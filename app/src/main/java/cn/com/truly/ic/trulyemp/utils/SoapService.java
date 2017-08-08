package cn.com.truly.ic.trulyemp.utils;

import android.util.Log;

import com.alibaba.fastjson.JSON;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import cn.com.truly.ic.trulyemp.models.ParamsModel;

public class SoapService {

    private int userid = 0;
    private String TAG = "SOAP";

    public SoapService() {
    }

    public SoapService(int _userid) {
        this.userid = _userid;
    }

    public String getSoapStringResult(String methodName,
                                      ParamsModel paramModel) throws Exception {

        //如果已经登陆，表示有了帐套信息和用户id，那就将这两个参数也设置一下。
        if (0 != userid) {
            paramModel.setArg0(userid);
        }
        Log.d(TAG, "userId:" + userid);

        String jsonParam = JSON.toJSONString(paramModel);
        Log.d(TAG, "jsonParam:" + jsonParam);
        jsonParam = MyUtils.AES.encrypt(jsonParam);
        SoapObject obj = getSoapObject(methodName, jsonParam);
        if (obj == null)
            return null;

        return obj.getProperty(0).toString();

    }

    private SoapObject getSoapObject(String methodName,
                                     String jsonParam) throws Exception {

        String nameSpace = "http://ic.truly.com.cn/";
        // EndPoint
        String endPoint = "http://59.37.42.23:80/EmpWebSvr/TrulyEmpSvr.asmx";
        // SOAP Action
        String soapAction = nameSpace + methodName;

        // 指定WebService的命名空间和调用的方法名
        SoapObject rpc = new SoapObject(nameSpace, methodName);

        // 设置需调用WebService接口需要传入的参数,将参数值AES加密

        rpc.addProperty("jsonParam", jsonParam);

        // 生成调用WebService方法的SOAP请求信息,并指定SOAP的版本
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);

        envelope.bodyOut = rpc;
        // 设置是否调用的是dotNet开发的WebService
        envelope.dotNet = true;
        // 等价于envelope.bodyOut = rpc;
        //envelope.setOutputSoapObject(rpc);

        HttpTransportSE transport = new HttpTransportSE(endPoint);
        try {
            // 调用WebService
            transport.call(soapAction, envelope);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // 获取返回的数据
        if (envelope.bodyIn instanceof SoapFault) {
            String str = ((SoapFault) envelope.bodyIn).faultstring;
            Log.e(TAG, str);
            return null;
        } else {
            // 获取返回的结果
            // String result = object.getProperty("33").toString();
            SoapObject obj = (SoapObject) envelope.bodyIn;
            Log.d(TAG, obj.toString());
            return obj;
        }
    }

}
