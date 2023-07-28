package com.leyunone.servicecenter.api.service;


import com.leyunone.servicecenter.api.dto.MailSendDTO;

/**
 * :)
 *
 * @Author pengli
 * @Date 2023/7/11 15:53
 */
public interface MessageMailService {

    boolean sendSimpleMail(String subject, String text, String[] tos);

    boolean sendMail(MailSendDTO sendDTO);
}
