package com.leyunone.servicecenter.api.dto;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.leyunone.servicecenter.api.bean.ResponseCell;
import com.leyunone.servicecenter.api.dto.MailFileDTO;
import com.leyunone.servicecenter.api.enums.MailHtmlEnum;
import com.leyunone.servicecenter.api.enums.SolverDataTypeEnum;
import com.leyunone.servicecenter.api.enums.SolverEnum;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * :)
 *
 * @Author leyunone
 * @Date 2023/7/11 11:45
 */
@Getter
public class MailSendDTO implements Serializable {
    
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private String messageId;

    /**
     * 邮件主题
     */
    private String subject;

    /**
     * 邮件内容
     */
    private String text;

    /**
     * 附件路径
     */
    private ResponseCell<SolverEnum,List<MailFileDTO>> annexFiles;

    /**
     * 内联文件
     */
    private ResponseCell<SolverEnum,Map<String,byte[]>> inLineFiles;


    /**
     * html
     */
    private String html;

    /**
     * 发送时间
     */
    private Date sendDate;

    /**
     * 接收人邮箱地址
     */
    private String[] tos;


    /**
     * 待处理数据
     * 见 
     * @see SolverDataTypeEnum
     */
    private Map<SolverDataTypeEnum,Object> datas = new HashMap<>();

    private MailSendDTO() {
    }

    /**
     * 唯一构造 ： 主题为必填值
     *
     * @param subject 主题
     */
    private MailSendDTO(String subject,String text, String... tos) {
        this.subject = subject;
        this.text = text;
        this.tos = tos;
        this.messageId = UUID.fastUUID().toString();
    }

    /**
     * 简单邮件
     * @param subject 主题
     * @param text 内容
     * @param tos 接收人
     * @return
     */
    public static MailSendDTO build(String subject,String text, String... tos) {
        return new MailSendDTO(subject,text, tos);
    }

    /**
     * html模板邮件
     * @param subject 主题
     * @param html 简单模板/模板文件
     * @param mailHtmlEnum 类型
     * @param paramMap 附加参数
     * @param tos 接收人
     * @return
     */
    public static MailSendDTO buildHtml(String subject, String html, MailHtmlEnum mailHtmlEnum, Map<String, Object> paramMap, String... tos) {
        MailSendDTO mailSendDTO = new MailSendDTO(subject,"", tos);
        mailSendDTO.html(html,mailHtmlEnum,paramMap);
        return mailSendDTO;
    }

    public void sendDate(Date date) {
        this.sendDate = date;
    }

    /**
     * 系统默认时区
     *
     * @param date 时间
     */
    public void sendDate(LocalDateTime date) {
        LocalDateTime dateTime = LocalDateTime.now();
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        Instant instant2 = zonedDateTime.toInstant();
        this.sendDate = Date.from(instant2);
    }

    /**
     * 简单html模板内容
     *
     * @param html
     * @param mailHtmlEnum 简单类型
     */
    private void html(String html, MailHtmlEnum mailHtmlEnum) {
        if (mailHtmlEnum == MailHtmlEnum.EASY) {
            this.html = html;
        }
    }

    /**
     * 模板文件+附加内容
     *
     * @param html         模板内容
     * @param mailHtmlEnum 类型
     * @param paramMap     填充值
     */
    private void html(String html, MailHtmlEnum mailHtmlEnum, Map<String, Object> paramMap) {
        switch (mailHtmlEnum) {
            case EASY:
                this.html(html, MailHtmlEnum.EASY);
                break;
            case VM_FILE:
                if (StrUtil.isBlank(html)) return;
                if (CollectionUtil.isEmpty(paramMap)) this.html = html;
                this.html = this.vmParse(html, paramMap);
                break;
        }
    }

    public void annexFile(SolverEnum solver,MailFileDTO ... mailFileDTO){
        if(ObjectUtil.isEmpty(mailFileDTO)) return;
        switch (solver) {
            case NONE:
                break;
            case PROVIDER:
                //服务提供方处理文件，但文件已存在时
                this.annexFile(SolverEnum.NONE,mailFileDTO);
                break;
            case  INTERNET:
                break;
        }

        this.annexFiles = ResponseCell.build(solver,CollectionUtil.newArrayList(mailFileDTO));
    }

    /**
     *
     * @param solver 文件处理方
     * @param classFilePaths 项目内相对路径
     */
    public void annexFile(SolverEnum solver, String ... classFilePaths) {
        if(ObjectUtil.isEmpty(classFilePaths)) return;
        List<MailFileDTO> toFile = new ArrayList<>();
        try {
            switch (solver) {
                case NONE:
                    //Dubbo文件传输 - byte[]
                    for(String filePath: classFilePaths){
                        File file = new File(filePath);
                        MailFileDTO mailFileDTO = new MailFileDTO();
                        mailFileDTO.setFileName(file.getName());
                        if(!file.exists()){
                            //项目路径名
                            ClassPathResource resource = new ClassPathResource(filePath);
                            mailFileDTO.setArrays(FileCopyUtils.copyToByteArray(resource.getInputStream()));
                        }else{
                            byte[] bFile = Files.readAllBytes(file.toPath());
                            mailFileDTO.setArrays(bFile);
                        }
                        toFile.add(mailFileDTO);
                    }
                    this.annexFile(solver,toFile.toArray(new MailFileDTO[]{}));
                    return;
                case PROVIDER:
                    Map<SolverDataTypeEnum, Object> datas = this.datas;
                    List<String> serverFiles = Arrays.asList(classFilePaths);
                    if(datas.containsKey(SolverDataTypeEnum.ANNEX_FILE)){
                        List<String> annexFiles = (List<String>)datas.get(SolverDataTypeEnum.ANNEX_FILE);
                        annexFiles.addAll(serverFiles);
                    }else{
                        datas.put(SolverDataTypeEnum.ANNEX_FILE,serverFiles);
                    }
                    break;
                case  INTERNET:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("mail file io is fail , e:");
        }
        this.annexFiles = ResponseCell.build(solver,toFile);
    }

    /**
     *
     * @param solver 文件处理方
     * @param fileMap 系统盘路径/网络资源
     */
    public void inLineFiles(SolverEnum solver,Map<String,String> fileMap) {
        if(CollectionUtil.isEmpty(fileMap)) return;
        Map<String,byte[]> files = new HashMap<>();
        try {
            switch (solver){
                case NONE:
                    for(String key: fileMap.keySet()){
                        File file = new File(fileMap.get(key));
                        byte[] bFile = null;
                        if(!file.exists()){
                            //项目路径名
                            ClassPathResource resource = new ClassPathResource(fileMap.get(key));
                            bFile = FileCopyUtils.copyToByteArray(resource.getInputStream());
                        }else{
                            bFile = Files.readAllBytes(file.toPath());
                        }
                        files.put(key,bFile);
                    }
                    break;
                case PROVIDER:
                    Map<SolverDataTypeEnum, Object> datas = this.datas;
                    if(datas.containsKey(SolverDataTypeEnum.INLINE_FILE)){
                        Map<String,String> inLineMap = (Map<String,String>)datas.get(SolverDataTypeEnum.ANNEX_FILE);
                        inLineMap.putAll(fileMap);
                    }else{
                        datas.put(SolverDataTypeEnum.INLINE_FILE,fileMap);
                    }
                    break;
                case  INTERNET:
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.error("mail file io is fail");
        }
        this.inLineFiles = ResponseCell.build(solver,files);
    }

    private String vmParse(String content, Map<String, Object> kvs) {
        Map<String, Object> reMap = new HashMap<>();
        for (String key : kvs.keySet()) {
            reMap.put("${" + key + "}", kvs.get(key));
        }

        String pattern = "\\$\\{(.*?)}";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        StringBuffer sr = new StringBuffer();
        while (m.find()) {
            String group = m.group();
            m.appendReplacement(sr, reMap.get(group).toString());
        }
        m.appendTail(sr);
        return sr.toString();
    }

}
