package cn.iocoder.yudao.module.courtcase.dal.mysql.cases;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseTransferDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourtCaseTransferMapper extends BaseMapperX<CourtCaseTransferDO> {

    default CourtCaseTransferDO selectLatestByCaseId(Long caseId) {
        return selectOne(new LambdaQueryWrapperX<CourtCaseTransferDO>()
                .eq(CourtCaseTransferDO::getCaseId, caseId)
                .orderByDesc(CourtCaseTransferDO::getId)
                .last("LIMIT 1"));
    }
}
