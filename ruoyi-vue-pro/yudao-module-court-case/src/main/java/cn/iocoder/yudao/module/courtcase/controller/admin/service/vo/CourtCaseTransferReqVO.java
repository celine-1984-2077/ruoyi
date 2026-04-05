package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CourtCaseTransferReqVO {

    @NotNull(message = "案件编号不能为空")
    private Long caseId;

    @NotNull(message = "接收法务人员不能为空")
    private Long receiverUserId;

    @NotBlank(message = "移交原因不能为空")
    private String reason;

    @Size(max = 500, message = "补充说明不能超过 500 字")
    private String extraNote;
}
