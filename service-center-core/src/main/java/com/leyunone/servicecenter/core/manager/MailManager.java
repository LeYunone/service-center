package com.leyunone.servicecenter.core.manager;


import com.leyunone.servicecenter.api.dto.MailSendDTO;

/**
 * :)
 *
 * @Author leyunone
 * @Date 2023/7/11 13:29
 */
public interface MailManager {

    /**
     * 简单邮件发送
     * @param title
     * @param content
     * @param tos
     */
    void send(String title,String content,String [] tos);

    /**
     * 自定义类型体发送
     * @param mailSend
     */
    boolean send(MailSendDTO mailSend);
}
