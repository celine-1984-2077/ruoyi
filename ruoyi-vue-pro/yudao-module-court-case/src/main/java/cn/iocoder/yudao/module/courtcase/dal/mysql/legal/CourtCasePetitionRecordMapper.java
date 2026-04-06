package cn.iocoder.yudao.module.courtcase.dal.mysql.legal;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCasePetitionRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCasePetitionRecordMapper extends BaseMapperX<CourtCasePetitionRecordDO> {

    default List<CourtCasePetitionRecordDO> selectListByCaseId(Long caseId) {
        return selectList(new LambdaQueryWrapperX<CourtCasePetitionRecordDO>()
                .eq(CourtCasePetitionRecordDO::getCaseId, caseId)
                .orderByDesc(CourtCasePetitionRecordDO::getVersionNo)
                .orderByDesc(CourtCasePetitionRecordDO::getId));
    }

    default CourtCasePetitionRecordDO selectLatestByCaseId(Long caseId) {
        return selectOne(new LambdaQueryWrapperX<CourtCasePetitionRecordDO>()
                .eq(CourtCasePetitionRecordDO::getCaseId, caseId)
                .orderByDesc(CourtCasePetitionRecordDO::getVersionNo)
                .last("LIMIT 1"));
    }
}
