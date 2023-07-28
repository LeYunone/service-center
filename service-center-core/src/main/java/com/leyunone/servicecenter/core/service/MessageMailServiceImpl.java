package com.leyunone.servicecenter.core.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.leyunone.servicecenter.api.dto.MailSendDTO;
import com.leyunone.servicecenter.api.enums.SolverDataTypeEnum;
import com.leyunone.servicecenter.api.enums.SolverEnum;
import com.leyunone.servicecenter.api.service.MessageMailService;
import com.leyunone.servicecenter.core.manager.MailManager;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.ValidationUtils;

import java.io.File;
import java.util.*;

/**
 * :)
 * 邮箱推送服务
 *
 * @Author leyunone
 * @Date 2023/7/11 11:17
 */
@DubboService
@Service
public class MessageMailServiceImpl implements MessageMailService {

    @Autowired
    private MailManager mailManager;

    private final Logger logger = LoggerFactory.getLogger(MessageMailServiceImpl.class);

    private void checkMail(String subject, String text, String[] tos) {
//        ValidationUtils.notEmpty(text, new ValidationCode("title be can't null"));
//        ValidationUtils.notEmpty(subject, new ValidationCode("content be can't null"));
//        ValidationUtils.notEmpty(Arrays.asList(tos), new ValidationCode("content be can't null"));
    }

    @Override
    public boolean sendSimpleMail(String subject, String text, String[] tos) {
        this.checkMail(subject, text, tos);
        mailManager.send(subject, text, tos);
        return true;
    }

    @Override
    public boolean sendMail(MailSendDTO sendDTO) {
        this.checkMail(sendDTO.getSubject(), StringUtils.isEmpty(sendDTO.getText()) ? sendDTO.getHtml() : sendDTO.getText(), sendDTO.getTos());
        //处理服务器内附件
        if (ObjectUtil.isNotNull(sendDTO.getFiles()) && sendDTO.getFiles().getCellData() == SolverEnum.PROVIDER) {
            if (CollectionUtil.isNotEmpty(sendDTO.getDatas())) {
                Object o = sendDTO.getDatas().get(SolverDataTypeEnum.ANNEX_FILE);
                if (ObjectUtil.isNotNull(o)) {
                    List<String> files = (List<String>) o;
                    sendDTO.annexFile(SolverEnum.NONE, files.toArray(new String[]{}));
                }
            }
        }

        //处理服务器内行内文件
        if (ObjectUtil.isNotNull(sendDTO.getInLineFiles()) && sendDTO.getFiles().getCellData() == SolverEnum.PROVIDER) {
            if (CollectionUtil.isNotEmpty(sendDTO.getDatas())) {
                Object o = sendDTO.getDatas().get(SolverDataTypeEnum.INLINE_FILE);
                if (ObjectUtil.isNotNull(o)) {
                    Map<String, String> inLineMap = (Map<String, String>) o;
                    sendDTO.inLineFiles(SolverEnum.NONE, inLineMap);
                }
            }
        }

        mailManager.send(sendDTO);
        return true;
    }

}
