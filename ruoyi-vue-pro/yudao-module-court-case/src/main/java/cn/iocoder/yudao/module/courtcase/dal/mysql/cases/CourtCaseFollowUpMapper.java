package cn.iocoder.yudao.module.courtcase.dal.mysql.cases;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFollowUpDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCaseFollowUpMapper extends BaseMapperX<CourtCaseFollowUpDO> {

    default List<CourtCaseFollowUpDO> selectListByCaseId(Long caseId) {
        return selectList(new LambdaQueryWrapperX<CourtCaseFollowUpDO>()
                .eq(CourtCaseFollowUpDO::getCaseId, caseId)
                .orderByDesc(CourtCaseFollowUpDO::getCreateTime));
    }
}
