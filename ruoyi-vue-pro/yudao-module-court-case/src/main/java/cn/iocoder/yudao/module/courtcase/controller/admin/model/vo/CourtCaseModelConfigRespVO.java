package cn.iocoder.yudao.module.courtcase.controller.admin.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 案件模型配置 Response VO")
@Data
public class CourtCaseModelConfigRespVO {

    private CourtCaseModelRespVO published;

    private CourtCaseModelRespVO draft;

    private List<CourtCaseModelRespVO> versions;
}
