package com.leyunone.servicecenter.core.manager;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.leyunone.servicecenter.api.dto.MailFileDTO;
import com.leyunone.servicecenter.api.dto.MailSendDTO;
import com.leyunone.servicecenter.api.enums.SolverEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Map;

/**
 * :)
 *
 * @Author leyunone
 * @Date 2023/7/11 13:30
 */
@Service
public class MailManagerImpl implements MailManager {

    private final Logger logger = LoggerFactory.getLogger(MailManagerImpl.class);

//    @Value("${spring.mail.username:1}")
    private String from;

    @Autowired
    private JavaMailSender javaMailSender;

    /**
     * 简单型发送邮件
     *
     * @param title
     * @param content
     * @param tos
     */
    @Override
    public void send(String title, String content, String[] tos) {
        try {
            // new 一个简单邮件消息对象
            SimpleMailMessage message = new SimpleMailMessage();
            // 和配置文件中的的username相同，相当于发送方
            message.setFrom(from);
            // 收件人邮箱
            message.setTo(tos);
            // 标题
            message.setSubject(title);
            // 正文
            message.setText(content);
            // 发送
            javaMailSender.send(message);
            logger.info("发送邮件[{}]成功", title);
        } catch (Exception e) {
            logger.error("邮件发送失败,title[" + title + "]", e);
        }
    }

    /**
     * @param mailSend
     */
    @Override
    public void send(MailSendDTO mailSend) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = null;
        try {
            mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            // 邮件发送来源
            mimeMessageHelper.setFrom(from);
            // 邮件发送目标
            mimeMessageHelper.setTo(mailSend.getTos());
            // 设置标题
            mimeMessageHelper.setSubject(mailSend.getSubject());
            mimeMessageHelper.setText(mailSend.getText());
            //发送日期
            if (ObjectUtil.isNotNull(mailSend.getSendDate())) {
                mimeMessageHelper.setSentDate(mailSend.getSendDate());
            }
            //HTML文本
            if (StrUtil.isNotBlank(mailSend.getHtml())) {
                mimeMessageHelper.setText(mailSend.getHtml(), true);
            }
            //附件
            if (ObjectUtil.isNotNull(mailSend.getFiles()) &&
                    mailSend.getFiles().getCellData() == SolverEnum.NONE &&
                    CollectionUtil.isNotEmpty(mailSend.getFiles().getMateDate())) {
                for (MailFileDTO fileDTO : mailSend.getFiles().getMateDate()) {
                    File file = File.createTempFile("temp",null,null);
                    try {
                        FileCopyUtils.copy(fileDTO.getArrays(), file);
                        mimeMessageHelper.addAttachment(fileDTO.getFileName(), file);
                    }finally {
                        file.deleteOnExit();
                    }
                }
            }
            //行内文件
            if(ObjectUtil.isNotNull(mailSend.getInLineFiles()) &&
                    mailSend.getInLineFiles().getCellData() == SolverEnum.NONE && CollectionUtil.isNotEmpty(mailSend.getInLineFiles().getMateDate())){
                Map<String, byte[]> mateDate = mailSend.getInLineFiles().getMateDate();
                for(String key : mateDate.keySet()){
                    File file = File.createTempFile("temp",null,null);
                    try {
                        FileCopyUtils.copy(mateDate.get(key), file);
                        mimeMessageHelper.addInline(key,file);
                    }finally {
                        file.deleteOnExit();
                    }
                }
            }
            javaMailSender.send(mimeMessage);
            logger.info("发送邮件[{}]成功", mailSend.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("邮件发送失败,title[" + mailSend.getSubject() + "]", e);
        }
    }
}
