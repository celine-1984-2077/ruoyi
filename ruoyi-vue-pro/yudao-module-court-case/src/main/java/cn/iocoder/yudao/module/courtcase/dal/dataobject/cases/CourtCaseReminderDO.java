package cn.iocoder.yudao.module.courtcase.dal.dataobject.cases;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@TableName("court_case_reminder")
@KeySequence("court_case_reminder_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class CourtCaseReminderDO extends BaseDO {

    @TableId
    private Long id;

    private Long caseId;

    private String reminderType;

    private LocalDateTime remindTime;

    private String content;

    private String priority;

    private String status;

    private Long creatorId;

    private LocalDateTime processedTime;
}
