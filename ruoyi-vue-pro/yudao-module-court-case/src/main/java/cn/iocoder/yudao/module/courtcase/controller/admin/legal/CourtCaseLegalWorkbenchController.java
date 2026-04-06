package cn.iocoder.yudao.module.courtcase.controller.admin.legal;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo.*;
import cn.iocoder.yudao.module.courtcase.service.legal.CourtCaseLegalWorkbenchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 法务工作台")
@RestController
@RequestMapping("/court-case/legal-workbench")
@Validated
public class CourtCaseLegalWorkbenchController {

    @Resource
    private CourtCaseLegalWorkbenchService courtCaseLegalWorkbenchService;

    @GetMapping("/summary")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得法务工作台汇总")
    public CommonResult<CourtCaseLegalSummaryRespVO> getSummary() {
        return success(courtCaseLegalWorkbenchService.getSummary(getLoginUserId()));
    }

    @GetMapping("/case-page")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得法务案件分页")
    public CommonResult<PageResult<CourtCaseLegalCaseRespVO>> getCasePage(@Valid CourtCaseLegalCasePageReqVO reqVO) {
        return success(courtCaseLegalWorkbenchService.getCasePage(getLoginUserId(), reqVO));
    }

    @GetMapping("/evidence/list")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得案件证据材料列表")
    public CommonResult<java.util.List<CourtCaseEvidenceRespVO>> getEvidenceList(@RequestParam("caseId") Long caseId) {
        return success(courtCaseLegalWorkbenchService.getEvidenceList(getLoginUserId(), caseId));
    }

    @PostMapping("/evidence/create")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "新增案件证据材料")
    public CommonResult<Boolean> createEvidence(@Valid @RequestBody CourtCaseEvidenceCreateReqVO reqVO) {
        courtCaseLegalWorkbenchService.createEvidence(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/evidence/delete")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "删除案件证据材料")
    @Parameter(name = "id", required = true, description = "证据材料编号")
    public CommonResult<Boolean> deleteEvidence(@RequestParam("id") Long id) throws Exception {
        courtCaseLegalWorkbenchService.deleteEvidence(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/evidence/download-zip")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "打包下载案件证据材料")
    public ResponseEntity<byte[]> downloadEvidenceZip(@RequestParam("caseId") Long caseId) throws Exception {
        CourtCaseDownloadRespVO download = courtCaseLegalWorkbenchService.downloadEvidenceZip(getLoginUserId(), caseId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + download.getFileName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(download.getContent());
    }

    @GetMapping("/petition-template/list")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得诉状模板列表")
    public CommonResult<java.util.List<CourtCasePetitionTemplateRespVO>> getPetitionTemplateList() {
        return success(courtCaseLegalWorkbenchService.getPetitionTemplateList());
    }

    @PostMapping("/petition-template/create")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "创建诉状模板")
    public CommonResult<Long> createPetitionTemplate(@Valid @RequestBody CourtCasePetitionTemplateSaveReqVO reqVO) {
        return success(courtCaseLegalWorkbenchService.createPetitionTemplate(getLoginUserId(), reqVO));
    }

    @PutMapping("/petition-template/update")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "更新诉状模板")
    public CommonResult<Boolean> updatePetitionTemplate(@Valid @RequestBody CourtCasePetitionTemplateSaveReqVO reqVO) {
        courtCaseLegalWorkbenchService.updatePetitionTemplate(getLoginUserId(), reqVO);
        return success(true);
    }

    @DeleteMapping("/petition-template/delete")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "删除诉状模板")
    public CommonResult<Boolean> deletePetitionTemplate(@RequestParam("id") Long id) {
        courtCaseLegalWorkbenchService.deletePetitionTemplate(getLoginUserId(), id);
        return success(true);
    }

    @GetMapping("/petition/list")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得案件诉状记录列表")
    public CommonResult<java.util.List<CourtCasePetitionRecordRespVO>> getPetitionRecordList(@RequestParam("caseId") Long caseId) {
        return success(courtCaseLegalWorkbenchService.getPetitionRecordList(getLoginUserId(), caseId));
    }

    @PostMapping("/petition/generate")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "生成诉状")
    public CommonResult<Long> generatePetition(@Valid @RequestBody CourtCasePetitionGenerateReqVO reqVO) throws Exception {
        return success(courtCaseLegalWorkbenchService.generatePetition(getLoginUserId(), reqVO));
    }

    @PostMapping("/petition/override")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "覆盖诉状生成记录")
    public CommonResult<Boolean> overridePetition(@Valid @RequestBody CourtCasePetitionOverrideReqVO reqVO) {
        courtCaseLegalWorkbenchService.overridePetition(getLoginUserId(), reqVO);
        return success(true);
    }

    @GetMapping("/filing/get")
    @PreAuthorize("@ss.hasPermission('court-case:case:query')")
    @Operation(summary = "获得立案信息")
    public CommonResult<CourtCaseFilingRespVO> getFiling(@RequestParam("caseId") Long caseId) {
        return success(courtCaseLegalWorkbenchService.getFiling(getLoginUserId(), caseId));
    }

    @PostMapping("/filing/save")
    @PreAuthorize("@ss.hasPermission('court-case:case:advance')")
    @Operation(summary = "保存立案信息")
    public CommonResult<Boolean> saveFiling(@Valid @RequestBody CourtCaseFilingSaveReqVO reqVO) {
        courtCaseLegalWorkbenchService.saveFiling(getLoginUserId(), reqVO);
        return success(true);
    }
}
