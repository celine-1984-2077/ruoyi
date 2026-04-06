package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtCasePetitionTemplateRespVO {

    private Long id;
    private String name;
    private String templateFileName;
    private String templateFileUrl;
    private String templateContent;
    private String placeholderDesc;
    private Boolean enabled;
    private LocalDateTime updateTime;
}
