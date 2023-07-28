package com.leyunone.servicecenter.api.enums;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * :)
 * 处理方枚举
 * @Author leyunone
 * @Date 2023/7/11 16:26
 */
public enum SolverEnum implements Serializable {
    /**
     * 无处理
     */
    NONE,
    /**
     * 推送中心
     */
    PROVIDER,
    /**
     * 网络资源方
     */
    INTERNET;
}
