package cn.iocoder.yudao.module.courtcase.dal.mysql.cases;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseReminderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface CourtCaseReminderMapper extends BaseMapperX<CourtCaseReminderDO> {

    default List<CourtCaseReminderDO> selectPendingListByCaseIds(Collection<Long> caseIds) {
        if (caseIds == null || caseIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CourtCaseReminderDO>()
                .in(CourtCaseReminderDO::getCaseId, caseIds)
                .eq(CourtCaseReminderDO::getStatus, "PENDING")
                .orderByDesc(CourtCaseReminderDO::getRemindTime));
    }
}
