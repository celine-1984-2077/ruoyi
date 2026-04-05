package cn.iocoder.yudao.module.courtcase.dal.mysql.cases;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFlowLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCaseFlowLogMapper extends BaseMapperX<CourtCaseFlowLogDO> {

    default List<CourtCaseFlowLogDO> selectListByCaseId(Long caseId) {
        return selectList(CourtCaseFlowLogDO::getCaseId, caseId);
    }

    default void deleteByCaseId(Long caseId) {
        delete(CourtCaseFlowLogDO::getCaseId, caseId);
    }
}
