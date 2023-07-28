package com.leyunone.servicecenter.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * :)
 *
 * @Author pengli
 * @Date 2023/7/28 13:44
 */
@Getter
@Setter
public class MailFileDTO implements Serializable {

    private byte [] arrays;
    
    private String fileName;
}
