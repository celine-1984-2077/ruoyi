package cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;

import javax.validation.constraints.Max;

@Data
public class CourtCaseLegalCasePageReqVO extends PageParam {

    private String caseNo;

    private String customerName;

    private String currentStage;

    @Override
    @Max(value = 200, message = "每页条数最大值为 200")
    public Integer getPageSize() {
        return super.getPageSize();
    }
}
