package com.leyunone.servicecenter.core.util;


import com.leyunone.servicecenter.api.bean.ResponseCode;
import com.leyunone.servicecenter.core.exception.ServiceCenterException;

/**
 * @author leyunone
 * @create 2021-08-13 09:31
 *
 * 报错处理
 */
public class AssertUtil {

    public static void isFalse(boolean condition, ResponseCode code){
        isFalse(condition,code.getDesc());
    }

    public static void isFalse(boolean condition,String message){
        isFalse(condition,new ServiceCenterException(message));
    }

    public static void isFalse(boolean condition){
        isFalse(condition,new ServiceCenterException("system error"));
    }

    public static void isFalse(boolean condition,ServiceCenterException ex){
        isTrue(!condition,ex);
    }

    public static void isTrue(boolean condition,ServiceCenterException ex){
        if(!condition){
            throw ex;
        }
    }

    public static void isTrue(boolean condition,String msg) throws ServiceCenterException {
        if(!condition){
            throw new ServiceCenterException(msg);
        }
    }

    public static void isTrue(boolean condition){
        if(!condition){
            throw new ServiceCenterException("系统异常");
        }
    }

    public static void isTrue(String msg){
        throw new ServiceCenterException(msg);
    }
}
