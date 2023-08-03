package com.leyunone.servicecenter.core.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.leyunone.servicecenter.api.bean.DataResponse;
import com.leyunone.servicecenter.api.dto.MailSendDTO;
import com.leyunone.servicecenter.api.enums.SolverDataTypeEnum;
import com.leyunone.servicecenter.api.enums.SolverEnum;
import com.leyunone.servicecenter.api.service.MessageMailService;
import com.leyunone.servicecenter.core.manager.CacheManager;
import com.leyunone.servicecenter.core.manager.MailManager;
import com.leyunone.servicecenter.core.util.AssertUtil;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.ValidationUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * :)
 * 邮箱推送服务
 *
 * @Author leyunone
 * @Date 2023/7/11 11:17
 */
@DubboService(timeout = 3000,retries = 2)
@Service
public class MessageMailServiceImpl implements MessageMailService {

    @Autowired
    private MailManager mailManager;
    @Autowired
    private CacheManager cacheManager;

    private final ConcurrentHashMap<String,BlockingQueue<Thread>> lockMap = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(MessageMailServiceImpl.class);

    private void checkMail(String subject, String text, String[] tos) {
        AssertUtil.isFalse(StrUtil.isBlank(text),"title be can't null");
        AssertUtil.isFalse(StrUtil.isBlank(subject),"content be can't null");
        AssertUtil.isFalse(ObjectUtil.isEmpty(tos),"tos be can't null");
    }

    @Override
    public DataResponse<?> sendSimpleMail(String subject, String text, String[] tos) {
        this.checkMail(subject, text, tos);
        mailManager.send(subject, text, tos);
        return DataResponse.of();
    }

    @Override
    public DataResponse<?> sendMail(MailSendDTO sendDTO) {
        DataResponse<?> commonResult = lockMailService(sendDTO.getMessageId());
        if(ObjectUtil.isNotNull(commonResult)) return commonResult;

        cacheManager.add(sendDTO.getMessageId(), 0,6L);
        this.checkMail(sendDTO.getSubject(), StringUtils.isEmpty(sendDTO.getText()) ? sendDTO.getHtml() : sendDTO.getText(), sendDTO.getTos());
        String classPath = "";
        //处理服务器内附件
        if (ObjectUtil.isNotNull(sendDTO.getAnnexFiles()) && sendDTO.getAnnexFiles().getCellData() == SolverEnum.PROVIDER) {
            if (CollectionUtil.isNotEmpty(sendDTO.getDatas())) {
                Object o = sendDTO.getDatas().get(SolverDataTypeEnum.ANNEX_FILE);
                if (ObjectUtil.isNotNull(o)) {
                    List<String> files = (List<String>) o;
                    List<String> filePath = files.stream().map(path -> classPath + path).collect(Collectors.toList());
                    sendDTO.annexFile(SolverEnum.NONE, filePath.toArray(new String[]{}));
                }
            }
        }

        //处理服务器内行内文件
        if (ObjectUtil.isNotNull(sendDTO.getInLineFiles()) && sendDTO.getInLineFiles().getCellData() == SolverEnum.PROVIDER) {
            if (CollectionUtil.isNotEmpty(sendDTO.getDatas())) {
                Object o = sendDTO.getDatas().get(SolverDataTypeEnum.INLINE_FILE);
                if (ObjectUtil.isNotNull(o)) {
                    Map<String, String> inLineMap = (Map<String, String>) o;
                    inLineMap.keySet().forEach(key -> {
                        inLineMap.put(key, classPath + inLineMap.get(key));
                    });
                    sendDTO.inLineFiles(SolverEnum.NONE, inLineMap);
                }
            }
        }
        boolean send = mailManager.send(sendDTO);
        if(send){
            lockMap.remove(sendDTO.getMessageId());
        }
        return DataResponse.of();
    }

    private DataResponse<?> lockMailService(String messageId) {
        BlockingQueue<Thread> threads = lockMap.get(messageId);
        if(CollectionUtil.isEmpty(threads)){
            threads = new LinkedBlockingQueue<>();
        }
        /**
         * 幂等判断
         * 采取自旋等待模式 - 超时时间 3s
         * sendStatus 2:成功 1:失败
         */
        Thread thread = Thread.currentThread();
        if(cacheManager.exists(messageId)){
            threads.add(thread);
        }
        int count = 0;
        while (cacheManager.exists(messageId)) {
            Integer sendStatus = cacheManager.getData(messageId, int.class);
            if (sendStatus.equals(2)) {
                threads.remove(thread);
                return DataResponse.of();
            }
            if (sendStatus.equals(1)) {
                //推出一个线程执行重试
                Thread remove = threads.remove();
                if(remove == thread) break;
            }
            if(count>=3){
                threads.remove(thread);
                return DataResponse.buildFailure();
            }
            count++;
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
            }
        }
        return null;
    }
}
