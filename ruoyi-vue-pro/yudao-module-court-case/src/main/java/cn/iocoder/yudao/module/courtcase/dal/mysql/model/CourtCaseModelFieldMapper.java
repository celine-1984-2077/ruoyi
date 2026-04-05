package cn.iocoder.yudao.module.courtcase.dal.mysql.model;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.model.CourtCaseModelFieldDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mapper
public interface CourtCaseModelFieldMapper extends BaseMapperX<CourtCaseModelFieldDO> {

    default List<CourtCaseModelFieldDO> selectListByModelId(Long modelId) {
        return selectList(new LambdaQueryWrapperX<CourtCaseModelFieldDO>()
                .eq(CourtCaseModelFieldDO::getModelId, modelId)
                .orderByAsc(CourtCaseModelFieldDO::getSortNo)
                .orderByAsc(CourtCaseModelFieldDO::getId));
    }

    default List<CourtCaseModelFieldDO> selectListByModelIds(Collection<Long> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return Collections.emptyList();
        }
        return selectList(new LambdaQueryWrapperX<CourtCaseModelFieldDO>()
                .in(CourtCaseModelFieldDO::getModelId, modelIds)
                .orderByAsc(CourtCaseModelFieldDO::getSortNo)
                .orderByAsc(CourtCaseModelFieldDO::getId));
    }

    default void deleteByModelId(Long modelId) {
        delete(new LambdaQueryWrapperX<CourtCaseModelFieldDO>()
                .eq(CourtCaseModelFieldDO::getModelId, modelId));
    }
}
