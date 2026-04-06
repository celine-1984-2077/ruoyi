package cn.iocoder.yudao.module.courtcase.service.legal;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo.*;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFlowLogDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCaseEvidenceDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCaseFilingDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCasePetitionRecordDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.legal.CourtCasePetitionTemplateDO;
import cn.iocoder.yudao.module.courtcase.dal.mysql.cases.CourtCaseFlowLogMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.cases.CourtCaseMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.legal.CourtCaseEvidenceMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.legal.CourtCaseFilingMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.legal.CourtCasePetitionRecordMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.legal.CourtCasePetitionTemplateMapper;
import cn.iocoder.yudao.module.courtcase.enums.*;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileDO;
import cn.iocoder.yudao.module.infra.dal.mysql.file.FileMapper;
import cn.iocoder.yudao.module.infra.service.file.FileService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.courtcase.enums.ErrorCodeConstants.*;

@Service
@Validated
public class CourtCaseLegalWorkbenchServiceImpl implements CourtCaseLegalWorkbenchService {

    private static final List<String> LEGAL_STAGES = Arrays.asList(
            CourtCaseStageEnum.LEGAL.getStage(),
            CourtCaseStageEnum.LITIGATION.getStage(),
            CourtCaseStageEnum.ARCHIVED.getStage()
    );

    private static final String PETITION_DIRECTORY = "court-case/petition";

    @Resource
    private CourtCaseMapper courtCaseMapper;
    @Resource
    private CourtCaseFlowLogMapper courtCaseFlowLogMapper;
    @Resource
    private CourtCaseEvidenceMapper courtCaseEvidenceMapper;
    @Resource
    private CourtCasePetitionTemplateMapper courtCasePetitionTemplateMapper;
    @Resource
    private CourtCasePetitionRecordMapper courtCasePetitionRecordMapper;
    @Resource
    private CourtCaseFilingMapper courtCaseFilingMapper;
    @Resource
    private FileMapper fileMapper;
    @Resource
    private FileService fileService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public CourtCaseLegalSummaryRespVO getSummary(Long userId) {
        List<CourtCaseDO> cases = getLegalCases(userId, null);
        Map<Long, CourtCaseFilingDO> filingMap = getFilingMap(cases);
        CourtCaseLegalSummaryRespVO respVO = new CourtCaseLegalSummaryRespVO();
        long legalCount = cases.stream().filter(item -> ObjectUtil.equal(item.getCurrentStage(), CourtCaseStageEnum.LEGAL.getStage())).count();
        long litigationCount = cases.stream().filter(item -> ObjectUtil.equal(item.getCurrentStage(), CourtCaseStageEnum.LITIGATION.getStage())).count();
        long archivedCount = cases.stream().filter(item -> ObjectUtil.equal(item.getCurrentStage(), CourtCaseStageEnum.ARCHIVED.getStage())).count();
        long pendingFilingCount = cases.stream()
                .filter(item -> {
                    CourtCaseFilingDO filing = filingMap.get(item.getId());
                    return filing == null || ObjectUtil.equal(filing.getStatus(), CourtCaseFilingStatusEnum.PENDING.getStatus());
                }).count();
        long rejectedCount = filingMap.values().stream()
                .filter(Objects::nonNull)
                .filter(item -> ObjectUtil.equal(item.getStatus(), CourtCaseFilingStatusEnum.REJECTED.getStatus()))
                .count();
        respVO.setLegalCount(legalCount);
        respVO.setLitigationCount(litigationCount);
        respVO.setPendingFilingCount(pendingFilingCount);
        respVO.setRejectedCount(rejectedCount);
        respVO.setArchivedCount(archivedCount);
        return respVO;
    }

    @Override
    public PageResult<CourtCaseLegalCaseRespVO> getCasePage(Long userId, CourtCaseLegalCasePageReqVO reqVO) {
        List<CourtCaseDO> cases = getLegalCases(userId, reqVO);
        if (CollUtil.isEmpty(cases)) {
            return page(Collections.emptyList(), reqVO);
        }
        Map<Long, CourtCaseFilingDO> filingMap = getFilingMap(cases);
        Map<Long, Integer> evidenceCountMap = countByCaseId(courtCaseEvidenceMapper.selectList(Wrappers.<CourtCaseEvidenceDO>lambdaQuery()
                .in(CourtCaseEvidenceDO::getCaseId, extractCaseIds(cases))));
        Map<Long, Integer> petitionCountMap = countByCaseId(courtCasePetitionRecordMapper.selectList(Wrappers.<CourtCasePetitionRecordDO>lambdaQuery()
                .in(CourtCasePetitionRecordDO::getCaseId, extractCaseIds(cases))));
        List<CourtCaseLegalCaseRespVO> rows = cases.stream().map(item -> {
            CourtCaseLegalCaseRespVO respVO = new CourtCaseLegalCaseRespVO();
            respVO.setId(item.getId());
            respVO.setCaseNo(item.getCaseNo());
            respVO.setOrderNo(item.getOrderNo());
            respVO.setCustomerName(item.getCustomerName());
            respVO.setMobile(item.getMobile());
            respVO.setAmount(item.getAmount());
            respVO.setCurrentStage(item.getCurrentStage());
            respVO.setCreateTime(item.getCreateTime());
            CourtCaseFilingDO filing = filingMap.get(item.getId());
            respVO.setFilingStatus(filing != null ? filing.getStatus() : CourtCaseFilingStatusEnum.PENDING.getStatus());
            respVO.setFilingSubmitTime(filing != null ? filing.getSubmitTime() : null);
            respVO.setEvidenceCount(evidenceCountMap.getOrDefault(item.getId(), 0));
            respVO.setPetitionCount(petitionCountMap.getOrDefault(item.getId(), 0));
            return respVO;
        }).collect(Collectors.toList());
        return page(rows, reqVO);
    }

    @Override
    public List<CourtCaseEvidenceRespVO> getEvidenceList(Long userId, Long caseId) {
        CourtCaseDO courtCase = validateLegalCase(caseId);
        validateLegalOperator(userId, courtCase);
        boolean evidenceLocked = isEvidenceLocked(caseId);
        return courtCaseEvidenceMapper.selectListByCaseId(caseId).stream().map(item -> {
            CourtCaseEvidenceRespVO respVO = new CourtCaseEvidenceRespVO();
            respVO.setId(item.getId());
            respVO.setCaseId(item.getCaseId());
            respVO.setCategory(item.getCategory());
            respVO.setFileName(item.getFileName());
            respVO.setFileUrl(item.getFileUrl());
            respVO.setFileType(item.getFileType());
            respVO.setFileSize(item.getFileSize());
            respVO.setCanDelete(!evidenceLocked);
            respVO.setCreateTime(item.getCreateTime());
            return respVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createEvidence(Long userId, @Valid CourtCaseEvidenceCreateReqVO reqVO) {
        CourtCaseDO courtCase = validateLegalCase(reqVO.getCaseId());
        validateLegalOperator(userId, courtCase);
        CourtCaseEvidenceCategoryEnum categoryEnum = CourtCaseEvidenceCategoryEnum.valueOfCategory(reqVO.getCategory());
        if (categoryEnum == null) {
            throw exception(CASE_EVIDENCE_CATEGORY_INVALID);
        }
        List<String> fileUrls = splitUrls(reqVO.getFileUrls());
        if (CollUtil.isEmpty(fileUrls)) {
            throw exception(CASE_EVIDENCE_FILE_REQUIRED);
        }
        if (fileUrls.size() > 10) {
            throw exception(CASE_EVIDENCE_BATCH_LIMIT);
        }
        for (String fileUrl : fileUrls) {
            FileDO file = validateUploadedFile(fileUrl);
            courtCaseEvidenceMapper.insert(new CourtCaseEvidenceDO()
                    .setCaseId(reqVO.getCaseId())
                    .setCategory(categoryEnum.getCategory())
                    .setFileId(file.getId())
                    .setFileName(file.getName())
                    .setFileUrl(file.getUrl())
                    .setFileType(file.getType())
                    .setFileSize(file.getSize()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEvidence(Long userId, Long id) throws Exception {
        CourtCaseEvidenceDO evidence = validateEvidenceExists(id);
        CourtCaseDO courtCase = validateLegalCase(evidence.getCaseId());
        validateLegalOperator(userId, courtCase);
        if (isEvidenceLocked(evidence.getCaseId())) {
            throw exception(CASE_EVIDENCE_DELETE_FORBIDDEN);
        }
        courtCaseEvidenceMapper.deleteById(id);
        fileService.deleteFile(evidence.getFileId());
    }

    @Override
    public CourtCaseDownloadRespVO downloadEvidenceZip(Long userId, Long caseId) throws Exception {
        CourtCaseDO courtCase = validateLegalCase(caseId);
        validateLegalOperator(userId, courtCase);
        List<CourtCaseEvidenceDO> evidences = courtCaseEvidenceMapper.selectListByCaseId(caseId);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream, StandardCharsets.UTF_8)) {
            int index = 1;
            for (CourtCaseEvidenceDO evidence : evidences) {
                FileDO file = fileMapper.selectById(evidence.getFileId());
                if (file == null) {
                    continue;
                }
                String entryName = index + "-" + safeFileName(evidence.getFileName());
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                zipOutputStream.write(fileService.getFileContent(file.getConfigId(), file.getPath()));
                zipOutputStream.closeEntry();
                index++;
            }
            zipOutputStream.finish();
            String zipName = URLEncoder.encode(courtCase.getOrderNo() + "-证据材料-"
                    + LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".zip", StandardCharsets.UTF_8);
            return new CourtCaseDownloadRespVO().setFileName(zipName).setContent(outputStream.toByteArray());
        }
    }

    @Override
    public List<CourtCasePetitionTemplateRespVO> getPetitionTemplateList() {
        return courtCasePetitionTemplateMapper.selectListByEnabled(null).stream().map(item -> {
            CourtCasePetitionTemplateRespVO respVO = new CourtCasePetitionTemplateRespVO();
            respVO.setId(item.getId());
            respVO.setName(item.getName());
            respVO.setTemplateFileName(item.getTemplateFileName());
            respVO.setTemplateFileUrl(item.getTemplateFileUrl());
            respVO.setTemplateContent(item.getTemplateContent());
            respVO.setPlaceholderDesc(item.getPlaceholderDesc());
            respVO.setEnabled(item.getEnabled());
            respVO.setUpdateTime(item.getUpdateTime());
            return respVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPetitionTemplate(Long userId, @Valid CourtCasePetitionTemplateSaveReqVO reqVO) {
        CourtCasePetitionTemplateDO template = buildTemplateDO(reqVO);
        courtCasePetitionTemplateMapper.insert(template);
        return template.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePetitionTemplate(Long userId, @Valid CourtCasePetitionTemplateSaveReqVO reqVO) {
        CourtCasePetitionTemplateDO existing = validateTemplateExists(reqVO.getId());
        CourtCasePetitionTemplateDO update = buildTemplateDO(reqVO).setId(existing.getId());
        courtCasePetitionTemplateMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePetitionTemplate(Long userId, Long id) {
        validateTemplateExists(id);
        courtCasePetitionTemplateMapper.deleteById(id);
    }

    @Override
    public List<CourtCasePetitionRecordRespVO> getPetitionRecordList(Long userId, Long caseId) {
        CourtCaseDO courtCase = validateLegalCase(caseId);
        validateLegalOperator(userId, courtCase);
        return courtCasePetitionRecordMapper.selectListByCaseId(caseId).stream().map(item -> {
            CourtCasePetitionRecordRespVO respVO = new CourtCasePetitionRecordRespVO();
            respVO.setId(item.getId());
            respVO.setCaseId(item.getCaseId());
            respVO.setTemplateName(item.getTemplateName());
            respVO.setOutputType(item.getOutputType());
            respVO.setVersionNo(item.getVersionNo());
            respVO.setFileName(item.getFileName());
            respVO.setFileUrl(item.getFileUrl());
            respVO.setOverwritten(item.getOverwritten());
            respVO.setCreateTime(item.getCreateTime());
            return respVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generatePetition(Long userId, @Valid CourtCasePetitionGenerateReqVO reqVO) throws Exception {
        CourtCaseDO courtCase = validateLegalCase(reqVO.getCaseId());
        validateLegalOperator(userId, courtCase);
        CourtCasePetitionTemplateDO template = validateTemplateExists(reqVO.getTemplateId());
        String content = replacePlaceholders(template.getTemplateContent(), buildPlaceholderMap(courtCase));
        byte[] bytes = generateWordDocument(content);
        String fileName = courtCase.getOrderNo() + "-诉状-" + LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN) + ".docx";
        String fileUrl = fileService.createFile(bytes, fileName, PETITION_DIRECTORY,
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        FileDO file = validateUploadedFile(fileUrl);
        CourtCasePetitionRecordDO latest = courtCasePetitionRecordMapper.selectLatestByCaseId(reqVO.getCaseId());
        CourtCasePetitionRecordDO record = new CourtCasePetitionRecordDO()
                .setCaseId(reqVO.getCaseId())
                .setTemplateId(template.getId())
                .setTemplateName(template.getName())
                .setOutputType(CourtCasePetitionOutputTypeEnum.WORD.getType())
                .setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1)
                .setFileId(file.getId())
                .setFileName(file.getName())
                .setFileUrl(file.getUrl())
                .setGeneratedContent(content)
                .setOverwritten(Boolean.FALSE);
        courtCasePetitionRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void overridePetition(Long userId, @Valid CourtCasePetitionOverrideReqVO reqVO) {
        CourtCasePetitionRecordDO record = validatePetitionRecordExists(reqVO.getRecordId());
        CourtCaseDO courtCase = validateLegalCase(record.getCaseId());
        validateLegalOperator(userId, courtCase);
        FileDO file = validateUploadedFile(reqVO.getFileUrl());
        record.setFileId(file.getId());
        record.setFileName(file.getName());
        record.setFileUrl(file.getUrl());
        record.setOverwritten(Boolean.TRUE);
        courtCasePetitionRecordMapper.updateById(record);
    }

    @Override
    public CourtCaseFilingRespVO getFiling(Long userId, Long caseId) {
        CourtCaseDO courtCase = validateLegalCase(caseId);
        validateLegalOperator(userId, courtCase);
        CourtCaseFilingDO filing = courtCaseFilingMapper.selectByCaseId(caseId);
        CourtCaseFilingRespVO respVO = new CourtCaseFilingRespVO();
        respVO.setEvidenceLocked(isEvidenceLocked(caseId));
        respVO.setCaseId(caseId);
        if (filing == null) {
            respVO.setStatus(CourtCaseFilingStatusEnum.PENDING.getStatus());
            return respVO;
        }
        respVO.setId(filing.getId());
        respVO.setCourtName(filing.getCourtName());
        respVO.setFilingNo(filing.getFilingNo());
        respVO.setSubmitTime(filing.getSubmitTime());
        respVO.setStatus(filing.getStatus());
        respVO.setRejectReason(filing.getRejectReason());
        return respVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveFiling(Long userId, @Valid CourtCaseFilingSaveReqVO reqVO) {
        CourtCaseDO courtCase = validateLegalCase(reqVO.getCaseId());
        validateLegalOperator(userId, courtCase);
        CourtCaseFilingStatusEnum filingStatus = CourtCaseFilingStatusEnum.valueOfStatus(reqVO.getStatus());
        if (filingStatus == null) {
            throw exception(CASE_STAGE_NOT_MATCH);
        }
        if (filingStatus == CourtCaseFilingStatusEnum.REJECTED && StrUtil.isBlank(reqVO.getRejectReason())) {
            throw exception(CASE_FILING_REJECT_REASON_REQUIRED);
        }
        CourtCaseFilingDO filing = courtCaseFilingMapper.selectByCaseId(reqVO.getCaseId());
        if (filing == null) {
            filing = new CourtCaseFilingDO().setCaseId(reqVO.getCaseId());
            courtCaseFilingMapper.insert(fillFilingValues(filing, reqVO));
        } else {
            courtCaseFilingMapper.updateById(fillFilingValues(filing, reqVO));
        }
        syncCaseStageByFiling(userId, courtCase, filingStatus, reqVO.getSubmitTime());
    }

    private CourtCaseFilingDO fillFilingValues(CourtCaseFilingDO filing, CourtCaseFilingSaveReqVO reqVO) {
        filing.setCourtName(reqVO.getCourtName());
        filing.setFilingNo(reqVO.getFilingNo());
        filing.setSubmitTime(reqVO.getSubmitTime());
        filing.setStatus(reqVO.getStatus());
        filing.setRejectReason(ObjectUtil.equal(reqVO.getStatus(), CourtCaseFilingStatusEnum.REJECTED.getStatus())
                ? reqVO.getRejectReason() : null);
        return filing;
    }

    private void syncCaseStageByFiling(Long userId, CourtCaseDO courtCase, CourtCaseFilingStatusEnum filingStatus,
                                       LocalDateTime submitTime) {
        String fromStage = courtCase.getCurrentStage();
        if (filingStatus == CourtCaseFilingStatusEnum.CLOSED) {
            courtCase.setCurrentStage(CourtCaseStageEnum.ARCHIVED.getStage());
            courtCase.setStatus(CourtCaseStatusEnum.ARCHIVED.getStatus());
            courtCaseMapper.updateById(courtCase);
            if (!ObjectUtil.equal(fromStage, CourtCaseStageEnum.ARCHIVED.getStage())) {
                insertFlowLog(courtCase.getId(), CourtCaseActionEnum.ARCHIVE.getAction(), fromStage,
                        CourtCaseStageEnum.ARCHIVED.getStage(), userId, "案件立案流程已结案");
            }
            return;
        }
        if (submitTime != null && ObjectUtil.equal(courtCase.getCurrentStage(), CourtCaseStageEnum.LEGAL.getStage())) {
            courtCase.setCurrentStage(CourtCaseStageEnum.LITIGATION.getStage());
            courtCaseMapper.updateById(courtCase);
            insertFlowLog(courtCase.getId(), CourtCaseActionEnum.FILE_LAWSUIT.getAction(), fromStage,
                    CourtCaseStageEnum.LITIGATION.getStage(), userId, "法务填写立案信息并提交法院");
        }
    }

    private CourtCasePetitionTemplateDO buildTemplateDO(CourtCasePetitionTemplateSaveReqVO reqVO) {
        if (StrUtil.isBlank(reqVO.getTemplateContent())) {
            throw exception(CASE_PETITION_TEMPLATE_CONTENT_REQUIRED);
        }
        CourtCasePetitionTemplateDO template = new CourtCasePetitionTemplateDO()
                .setName(reqVO.getName())
                .setTemplateContent(reqVO.getTemplateContent())
                .setPlaceholderDesc(reqVO.getPlaceholderDesc())
                .setEnabled(reqVO.getEnabled() == null ? Boolean.TRUE : reqVO.getEnabled());
        if (StrUtil.isNotBlank(reqVO.getTemplateFileUrl())) {
            FileDO file = validateUploadedFile(reqVO.getTemplateFileUrl());
            template.setTemplateFileId(file.getId());
            template.setTemplateFileName(file.getName());
            template.setTemplateFileUrl(file.getUrl());
        }
        return template;
    }

    private Map<String, String> buildPlaceholderMap(CourtCaseDO courtCase) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("caseNo", defaultString(courtCase.getCaseNo()));
        map.put("orderNo", defaultString(courtCase.getOrderNo()));
        map.put("customerName", defaultString(courtCase.getCustomerName()));
        map.put("mobile", defaultString(courtCase.getMobile()));
        map.put("amount", courtCase.getAmount() != null ? courtCase.getAmount().stripTrailingZeros().toPlainString() : "0");
        map.put("repaymentDueDate", courtCase.getRepaymentDueDate() != null ? courtCase.getRepaymentDueDate().toString() : "-");
        map.put("overdueDays", String.valueOf(resolveOverdueDays(courtCase)));
        map.put("customerStatus", defaultString(courtCase.getCustomerStatus()));
        Map<String, Object> extMap = JsonUtils.parseObject(courtCase.getExtJson(), new TypeReference<Map<String, Object>>() {});
        if (extMap != null) {
            extMap.forEach((key, value) -> map.putIfAbsent(key, value == null ? "-" : String.valueOf(value)));
        }
        map.putIfAbsent("identityNo", "-");
        return map;
    }

    private String replacePlaceholders(String templateContent, Map<String, String> placeholderMap) {
        String result = templateContent;
        for (Map.Entry<String, String> entry : placeholderMap.entrySet()) {
            result = StrUtil.replace(result, "{{" + entry.getKey() + "}}", defaultString(entry.getValue()));
            result = StrUtil.replace(result, "${" + entry.getKey() + "}", defaultString(entry.getValue()));
        }
        return result;
    }

    private byte[] generateWordDocument(String content) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (String line : StrUtil.splitToArray(StrUtil.blankToDefault(content, ""), '\n')) {
                XWPFParagraph paragraph = document.createParagraph();
                XWPFRun run = paragraph.createRun();
                run.setText(line);
            }
            document.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private Map<Long, CourtCaseFilingDO> getFilingMap(List<CourtCaseDO> cases) {
        if (CollUtil.isEmpty(cases)) {
            return Collections.emptyMap();
        }
        return courtCaseFilingMapper.selectList(Wrappers.<CourtCaseFilingDO>lambdaQuery()
                        .in(CourtCaseFilingDO::getCaseId, extractCaseIds(cases)))
                .stream().collect(Collectors.toMap(CourtCaseFilingDO::getCaseId, item -> item, (a, b) -> b));
    }

    private List<CourtCaseDO> getLegalCases(Long userId, CourtCaseLegalCasePageReqVO reqVO) {
        AdminUserRespDTO operator = validateOperator(userId);
        String caseNo = reqVO != null ? reqVO.getCaseNo() : null;
        String customerName = reqVO != null ? reqVO.getCustomerName() : null;
        String currentStage = reqVO != null ? reqVO.getCurrentStage() : null;
        return courtCaseMapper.selectList(Wrappers.<CourtCaseDO>lambdaQuery()
                .eq(CourtCaseDO::getCurrentDeptId, operator.getDeptId())
                .in(CourtCaseDO::getCurrentStage, LEGAL_STAGES)
                .like(StrUtil.isNotBlank(caseNo), CourtCaseDO::getCaseNo, caseNo)
                .like(StrUtil.isNotBlank(customerName), CourtCaseDO::getCustomerName, customerName)
                .eq(StrUtil.isNotBlank(currentStage), CourtCaseDO::getCurrentStage, currentStage)
                .orderByDesc(CourtCaseDO::getUpdateTime)
                .orderByDesc(CourtCaseDO::getId));
    }

    private CourtCaseDO validateLegalCase(Long caseId) {
        CourtCaseDO courtCase = courtCaseMapper.selectById(caseId);
        if (courtCase == null) {
            throw exception(CASE_NOT_EXISTS);
        }
        if (!LEGAL_STAGES.contains(courtCase.getCurrentStage())) {
            throw exception(CASE_LEGAL_STAGE_REQUIRED);
        }
        return courtCase;
    }

    private CourtCaseEvidenceDO validateEvidenceExists(Long id) {
        CourtCaseEvidenceDO evidence = courtCaseEvidenceMapper.selectById(id);
        if (evidence == null) {
            throw exception(CASE_EVIDENCE_NOT_EXISTS);
        }
        return evidence;
    }

    private CourtCasePetitionTemplateDO validateTemplateExists(Long id) {
        CourtCasePetitionTemplateDO template = courtCasePetitionTemplateMapper.selectById(id);
        if (template == null) {
            throw exception(CASE_PETITION_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    private CourtCasePetitionRecordDO validatePetitionRecordExists(Long id) {
        CourtCasePetitionRecordDO record = courtCasePetitionRecordMapper.selectById(id);
        if (record == null) {
            throw exception(CASE_PETITION_RECORD_NOT_EXISTS);
        }
        return record;
    }

    private FileDO validateUploadedFile(String url) {
        FileDO file = fileMapper.selectByUrl(url);
        if (file == null) {
            throw exception(CASE_EVIDENCE_FILE_REQUIRED);
        }
        return file;
    }

    private void validateLegalOperator(Long userId, CourtCaseDO courtCase) {
        AdminUserRespDTO operator = validateOperator(userId);
        if (!ObjectUtil.equal(courtCase.getCurrentDeptId(), operator.getDeptId())) {
            throw exception(CASE_OPERATOR_NOT_IN_DEPT);
        }
    }

    private AdminUserRespDTO validateOperator(Long userId) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        adminUserApi.validateUser(userId);
        return user;
    }

    private boolean isEvidenceLocked(Long caseId) {
        CourtCaseFilingDO filing = courtCaseFilingMapper.selectByCaseId(caseId);
        return filing != null && filing.getSubmitTime() != null;
    }

    private List<String> splitUrls(String fileUrls) {
        return Arrays.stream(StrUtil.blankToDefault(fileUrls, "").split(","))
                .map(String::trim)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<Long> extractCaseIds(List<CourtCaseDO> cases) {
        return cases.stream().map(CourtCaseDO::getId).collect(Collectors.toList());
    }

    private Map<Long, Integer> countByCaseId(List<?> rows) {
        Map<Long, Integer> countMap = new HashMap<>();
        for (Object row : rows) {
            Long caseId;
            if (row instanceof CourtCaseEvidenceDO) {
                caseId = ((CourtCaseEvidenceDO) row).getCaseId();
            } else if (row instanceof CourtCasePetitionRecordDO) {
                caseId = ((CourtCasePetitionRecordDO) row).getCaseId();
            } else {
                continue;
            }
            countMap.merge(caseId, 1, Integer::sum);
        }
        return countMap;
    }

    private <T> PageResult<T> page(List<T> list, CourtCaseLegalCasePageReqVO reqVO) {
        int pageNo = reqVO != null && reqVO.getPageNo() != null ? reqVO.getPageNo() : 1;
        int pageSize = reqVO != null && reqVO.getPageSize() != null ? reqVO.getPageSize() : 10;
        int fromIndex = Math.max((pageNo - 1) * pageSize, 0);
        if (fromIndex >= list.size()) {
            return new PageResult<>(Collections.emptyList(), (long) list.size());
        }
        int toIndex = Math.min(fromIndex + pageSize, list.size());
        return new PageResult<>(list.subList(fromIndex, toIndex), (long) list.size());
    }

    private void insertFlowLog(Long caseId, String action, String fromStage, String toStage, Long operatorId, String remark) {
        courtCaseFlowLogMapper.insert(new CourtCaseFlowLogDO()
                .setCaseId(caseId)
                .setAction(action)
                .setFromStage(fromStage)
                .setToStage(toStage)
                .setOperatorId(operatorId)
                .setRemark(remark));
    }

    private int resolveOverdueDays(CourtCaseDO courtCase) {
        if (courtCase.getRepaymentDueDate() == null) {
            return 0;
        }
        return (int) Math.max(0, ChronoUnit.DAYS.between(courtCase.getRepaymentDueDate(), LocalDate.now()));
    }

    private String safeFileName(String fileName) {
        String name = defaultString(fileName);
        return name.replace('/', '_').replace('\\', '_').replace(':', '_');
    }

    private String defaultString(String value) {
        return StrUtil.blankToDefault(value, "-");
    }
}
