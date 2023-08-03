package com.leyunone.servicecenter.api.service;


import com.leyunone.servicecenter.api.bean.DataResponse;
import com.leyunone.servicecenter.api.dto.MailSendDTO;

/**
 * :)
 *
 * @Author leyunone
 * @Date 2023/7/11 15:53
 */
public interface MessageMailService {

    DataResponse<?> sendSimpleMail(String subject, String text, String[] tos);

    DataResponse<?> sendMail(MailSendDTO sendDTO);
}
