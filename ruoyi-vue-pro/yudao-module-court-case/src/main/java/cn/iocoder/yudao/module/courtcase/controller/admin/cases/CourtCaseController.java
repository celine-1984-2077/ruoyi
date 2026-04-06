package cn.iocoder.yudao.module.courtcase.controller.admin.cases;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.*;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFlowLogDO;
import cn.iocoder.yudao.module.courtcase.service.cases.CourtCaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 案件")
@RestController
@RequestMapping("/court-case/case")
@Validated
public class CourtCaseController {

    @Resource
    private CourtCaseService courtCaseService;

    @PostMapping("/create")
    @PreAuthorize("@ss.hasPermission('court-case:case:create')")
    @Operation(summary = "创建案件")
    public CommonResult<Long> createCase(@Valid @RequestBody CourtCaseCreateReqVO createReqVO) {
        return success(courtCaseService.createCase(getLoginUserId(), createReqVO));
    }

    @PostMapping("/update")
    @PreAuthorize("@ss.hasPermission('court-case:case:update')")
    @Operation(summary = "更新案件")
    public CommonResult<Boolean> updateCase(@Valid @RequestBody CourtCaseUpdateReqVO updateReqVO) {
        courtCaseService.updateCase(getLoginUserId(), updateReqVO);
        return success(true);
    }

    @GetMapping("/get")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得案件")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    public CommonResult<CourtCaseRespVO> getCase(@RequestParam("id") Long id) {
        CourtCaseDO courtCase = courtCaseService.getCase(getLoginUserId(), id);
        return success(BeanUtils.toBean(courtCase, CourtCaseRespVO.class));
    }

    @DeleteMapping("/delete")
    @PreAuthorize("@ss.hasPermission('court-case:case:delete')")
    @Operation(summary = "删除案件")
    @Parameter(name = "id", description = "编号", required = true, example = "1")
    public CommonResult<Boolean> deleteCase(@RequestParam("id") Long id) {
        courtCaseService.deleteCase(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/page")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得案件分页")
    public CommonResult<PageResult<CourtCaseRespVO>> getCasePage(@Valid CourtCasePageReqVO pageReqVO) {
        PageResult<CourtCaseDO> pageResult = courtCaseService.getCasePage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, CourtCaseRespVO.class));
    }

    @GetMapping("/workbench/page")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得当前部门工作台案件分页")
    public CommonResult<PageResult<CourtCaseRespVO>> getWorkbenchPage(@Valid CourtCasePageReqVO pageReqVO) {
        PageResult<CourtCaseDO> pageResult = courtCaseService.getWorkbenchPage(getLoginUserId(), pageReqVO);
        return success(BeanUtils.toBean(pageResult, CourtCaseRespVO.class));
    }

    @PostMapping("/advance")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "推进案件")
    public CommonResult<Boolean> advanceCase(@Valid @RequestBody CourtCaseAdvanceReqVO advanceReqVO) {
        courtCaseService.advanceCase(getLoginUserId(), advanceReqVO);
        return success(true);
    }

    @GetMapping("/flow-log/list")
    @PreAuthorize("@ss.hasPermission('court-case:log:query')")
    @Operation(summary = "获得案件流转日志")
    @Parameter(name = "caseId", description = "案件编号", required = true, example = "1")
    public CommonResult<List<CourtCaseFlowLogRespVO>> getCaseFlowLogList(@RequestParam("caseId") Long caseId) {
        List<CourtCaseFlowLogDO> list = courtCaseService.getCaseFlowLogs(getLoginUserId(), caseId);
        return success(BeanUtils.toBean(list, CourtCaseFlowLogRespVO.class));
    }
}
