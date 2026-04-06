package cn.iocoder.yudao.module.courtcase.dal.mysql.legal;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCaseEvidenceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCaseEvidenceMapper extends BaseMapperX<CourtCaseEvidenceDO> {

    default List<CourtCaseEvidenceDO> selectListByCaseId(Long caseId) {
        return selectList(new LambdaQueryWrapperX<CourtCaseEvidenceDO>()
                .eq(CourtCaseEvidenceDO::getCaseId, caseId)
                .orderByDesc(CourtCaseEvidenceDO::getCreateTime)
                .orderByDesc(CourtCaseEvidenceDO::getId));
    }
}
