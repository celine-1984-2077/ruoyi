package cn.iocoder.yudao.module.courtcase.service.service;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.service.vo.*;

import javax.validation.Valid;
import java.util.List;

public interface CourtCaseServiceWorkbenchService {

    CourtCaseServiceWorkbenchSummaryRespVO getSummary(Long userId);

    PageResult<CourtCaseServiceWorkbenchRespVO> getReminderPage(Long userId, CourtCaseServiceWorkbenchPageReqVO reqVO);

    PageResult<CourtCaseServiceWorkbenchRespVO> getOverduePage(Long userId, CourtCaseServiceWorkbenchPageReqVO reqVO);

    List<CourtCaseFollowUpRespVO> getFollowUpList(Long userId, Long caseId);

    void createFollowUp(Long userId, @Valid CourtCaseFollowUpCreateReqVO reqVO);

    void createCustomReminder(Long userId, @Valid CourtCaseReminderCreateReqVO reqVO);

    void markRepaid(Long userId, Long caseId);

    void transferToLegal(Long userId, @Valid CourtCaseTransferReqVO reqVO);
}
