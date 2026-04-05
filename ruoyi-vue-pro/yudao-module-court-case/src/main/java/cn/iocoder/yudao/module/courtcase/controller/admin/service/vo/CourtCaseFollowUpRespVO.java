package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CourtCaseFollowUpRespVO {

    private Long id;
    private Long caseId;
    private Long operatorId;
    private String operatorName;
    private String content;
    private String attachmentUrls;
    private LocalDateTime createTime;
}
