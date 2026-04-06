package cn.iocoder.yudao.module.courtcase.dal.mysql.legal;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCaseFilingDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourtCaseFilingMapper extends BaseMapperX<CourtCaseFilingDO> {

    default CourtCaseFilingDO selectByCaseId(Long caseId) {
        return selectOne(new LambdaQueryWrapperX<CourtCaseFilingDO>()
                .eq(CourtCaseFilingDO::getCaseId, caseId)
                .last("LIMIT 1"));
    }
}
