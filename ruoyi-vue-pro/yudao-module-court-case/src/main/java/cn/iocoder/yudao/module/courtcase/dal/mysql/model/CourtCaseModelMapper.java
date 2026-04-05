package cn.iocoder.yudao.module.courtcase.dal.mysql.model;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.model.CourtCaseModelDO;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseModelStatusEnum;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCaseModelMapper extends BaseMapperX<CourtCaseModelDO> {

    default CourtCaseModelDO selectLatestPublished() {
        return selectOne(new LambdaQueryWrapperX<CourtCaseModelDO>()
                .eq(CourtCaseModelDO::getStatus, CourtCaseModelStatusEnum.PUBLISHED.name())
                .orderByDesc(CourtCaseModelDO::getVersionNo)
                .last("LIMIT 1"));
    }

    default CourtCaseModelDO selectLatestDraft() {
        return selectOne(new LambdaQueryWrapperX<CourtCaseModelDO>()
                .eq(CourtCaseModelDO::getStatus, CourtCaseModelStatusEnum.DRAFT.name())
                .orderByDesc(CourtCaseModelDO::getVersionNo)
                .last("LIMIT 1"));
    }

    default Integer selectMaxVersionNo() {
        List<CourtCaseModelDO> list = selectList(new LambdaQueryWrapperX<CourtCaseModelDO>()
                .select(CourtCaseModelDO::getVersionNo)
                .orderByDesc(CourtCaseModelDO::getVersionNo)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0).getVersionNo();
    }

    default List<CourtCaseModelDO> selectVersionList() {
        return selectList(new LambdaQueryWrapperX<CourtCaseModelDO>()
                .orderByDesc(CourtCaseModelDO::getVersionNo)
                .orderByDesc(CourtCaseModelDO::getId));
    }
}
