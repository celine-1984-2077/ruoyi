package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 案件模型 Response VO")
@Data
public class CourtCaseModelRespVO {

    private Long id;

    private Integer versionNo;

    private String status;

    private String changeSummary;

    private String impactSummary;

    private LocalDateTime publishedTime;

    private LocalDateTime createTime;

    private List<String> impactHints;

    private List<CourtCaseModelFieldRespVO> fields;
}
