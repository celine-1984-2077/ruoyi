package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtCaseEvidenceRespVO {

    private Long id;
    private Long caseId;
    private String category;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Boolean canDelete;
    private LocalDateTime createTime;
}
