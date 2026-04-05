package cn.iocoder.yudao.module.courtcase.service.cases;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCaseAdvanceReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCaseCreateReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCasePageReqVO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFlowLogDO;

import javax.validation.Valid;
import java.util.List;

public interface CourtCaseService {

    Long createCase(Long userId, @Valid CourtCaseCreateReqVO createReqVO);

    CourtCaseDO getCase(Long id);

    void deleteCase(Long id);

    PageResult<CourtCaseDO> getCasePage(CourtCasePageReqVO pageReqVO);

    PageResult<CourtCaseDO> getWorkbenchPage(Long userId, CourtCasePageReqVO pageReqVO);

    void advanceCase(Long userId, @Valid CourtCaseAdvanceReqVO advanceReqVO);

    List<CourtCaseFlowLogDO> getCaseFlowLogs(Long caseId);
}
