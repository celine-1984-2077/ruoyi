package cn.iocoder.yudao.module.courtcase.controller.admin.service.vo;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class CourtCaseReminderCreateReqVO {

    @NotNull(message = "案件编号不能为空")
    private Long caseId;

    @NotNull(message = "提醒时间不能为空")
    private LocalDateTime remindTime;

    @NotBlank(message = "提醒内容不能为空")
    @Size(max = 500, message = "提醒内容不能超过 500 字")
    private String content;
}
