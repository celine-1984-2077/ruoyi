package cn.iocoder.yudao.module.courtcase.dal.mysql.legal;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCasePetitionTemplateDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CourtCasePetitionTemplateMapper extends BaseMapperX<CourtCasePetitionTemplateDO> {

    default List<CourtCasePetitionTemplateDO> selectListByEnabled(Boolean enabled) {
        return selectList(new LambdaQueryWrapperX<CourtCasePetitionTemplateDO>()
                .eqIfPresent(CourtCasePetitionTemplateDO::getEnabled, enabled)
                .orderByDesc(CourtCasePetitionTemplateDO::getUpdateTime)
                .orderByDesc(CourtCasePetitionTemplateDO::getId));
    }
}
