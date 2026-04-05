package cn.iocoder.yudao.module.courtcase.dal.mysql.cases;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCasePageReqVO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CourtCaseMapper extends BaseMapperX<CourtCaseDO> {

    default PageResult<CourtCaseDO> selectPage(CourtCasePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CourtCaseDO>()
                .likeIfPresent(CourtCaseDO::getCaseNo, reqVO.getCaseNo())
                .likeIfPresent(CourtCaseDO::getOrderNo, reqVO.getOrderNo())
                .likeIfPresent(CourtCaseDO::getCustomerName, reqVO.getCustomerName())
                .eqIfPresent(CourtCaseDO::getCurrentStage, reqVO.getCurrentStage())
                .eqIfPresent(CourtCaseDO::getCurrentDeptId, reqVO.getCurrentDeptId())
                .eqIfPresent(CourtCaseDO::getCurrentAssigneeId, reqVO.getCurrentAssigneeId())
                .eqIfPresent(CourtCaseDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(CourtCaseDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(CourtCaseDO::getId));
    }
}
