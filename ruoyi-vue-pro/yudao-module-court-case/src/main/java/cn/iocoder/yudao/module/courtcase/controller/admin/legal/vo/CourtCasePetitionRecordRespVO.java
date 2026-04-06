package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtCasePetitionRecordRespVO {

    private Long id;
    private Long caseId;
    private String templateName;
    private String outputType;
    private Integer versionNo;
    private String fileName;
    private String fileUrl;
    private Boolean overwritten;
    private LocalDateTime createTime;
}
