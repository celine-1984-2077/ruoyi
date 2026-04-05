package cn.iocoder.yudao.module.courtcase.controller.admin.model;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelConfigRespVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelDraftSaveReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelRespVO;
import cn.iocoder.yudao.module.courtcase.service.model.CourtCaseModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 案件模型")
@RestController
@RequestMapping("/court-case/model")
@Validated
public class CourtCaseModelController {

    @Resource
    private CourtCaseModelService courtCaseModelService;

    @GetMapping("/config")
    @PreAuthorize("@ss.hasPermission('court-case:model:query')")
    @Operation(summary = "获取案件模型配置")
    public CommonResult<CourtCaseModelConfigRespVO> getModelConfig() {
        return success(courtCaseModelService.getModelConfig());
    }

    @PostMapping("/draft/save")
    @PreAuthorize("@ss.hasPermission('court-case:model:update')")
    @Operation(summary = "保存案件模型草稿")
    public CommonResult<CourtCaseModelRespVO> saveDraft(@Valid @RequestBody CourtCaseModelDraftSaveReqVO reqVO) {
        return success(courtCaseModelService.saveDraft(reqVO));
    }

    @PostMapping("/draft/publish")
    @PreAuthorize("@ss.hasPermission('court-case:model:publish')")
    @Operation(summary = "发布案件模型草稿")
    public CommonResult<CourtCaseModelRespVO> publishDraft(@Valid @RequestBody CourtCaseModelDraftSaveReqVO reqVO) {
        return success(courtCaseModelService.publishDraft(reqVO));
    }

    @DeleteMapping("/draft/delete")
    @PreAuthorize("@ss.hasPermission('court-case:model:update')")
    @Operation(summary = "删除案件模型草稿")
    public CommonResult<Boolean> deleteDraft(@RequestParam("id") Long id) {
        courtCaseModelService.deleteDraft(id);
        return success(true);
    }
}
