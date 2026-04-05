package cn.iocoder.yudao.module.courtcase.controller.admin.service;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.service.vo.*;
import cn.iocoder.yudao.module.courtcase.service.service.CourtCaseServiceWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 客服工作台")
@RestController
@RequestMapping("/court-case/service-workbench")
@Validated
public class CourtCaseServiceWorkbenchController {

    @Resource
    private CourtCaseServiceWorkbenchService courtCaseServiceWorkbenchService;

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得客服工作台汇总")
    public CommonResult<CourtCaseServiceWorkbenchSummaryRespVO> getSummary() {
        return success(courtCaseServiceWorkbenchService.getSummary(getLoginUserId()));
    }

    @GetMapping("/reminder-page")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得还款提醒分页")
    public CommonResult<PageResult<CourtCaseServiceWorkbenchRespVO>> getReminderPage(@Valid CourtCaseServiceWorkbenchPageReqVO reqVO) {
        return success(courtCaseServiceWorkbenchService.getReminderPage(getLoginUserId(), reqVO));
    }

    @GetMapping("/overdue-page")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得逾期客户分页")
    public CommonResult<PageResult<CourtCaseServiceWorkbenchRespVO>> getOverduePage(@Valid CourtCaseServiceWorkbenchPageReqVO reqVO) {
        return success(courtCaseServiceWorkbenchService.getOverduePage(getLoginUserId(), reqVO));
    }

    @GetMapping("/follow-up/list")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得跟进记录列表")
    public CommonResult<List<CourtCaseFollowUpRespVO>> getFollowUpList(@RequestParam("caseId") Long caseId) {
        return success(courtCaseServiceWorkbenchService.getFollowUpList(caseId));
    }

    @PostMapping("/follow-up/create")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "新增跟进记录")
    public CommonResult<Boolean> createFollowUp(@Valid @RequestBody CourtCaseFollowUpCreateReqVO reqVO) {
        courtCaseServiceWorkbenchService.createFollowUp(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/reminder/create")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "新增临时提醒")
    public CommonResult<Boolean> createReminder(@Valid @RequestBody CourtCaseReminderCreateReqVO reqVO) {
        courtCaseServiceWorkbenchService.createCustomReminder(getLoginUserId(), reqVO);
        return success(true);
    }

    @PostMapping("/mark-repaid")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "标记已还款")
    public CommonResult<Boolean> markRepaid(@RequestParam("caseId") Long caseId) {
        courtCaseServiceWorkbenchService.markRepaid(getLoginUserId(), caseId);
        return success(true);
    }

    @PostMapping("/transfer-legal")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "移交法诉")
    public CommonResult<Boolean> transferLegal(@Valid @RequestBody CourtCaseTransferReqVO reqVO) {
        courtCaseServiceWorkbenchService.transferToLegal(getLoginUserId(), reqVO);
        return success(true);
    }
}
