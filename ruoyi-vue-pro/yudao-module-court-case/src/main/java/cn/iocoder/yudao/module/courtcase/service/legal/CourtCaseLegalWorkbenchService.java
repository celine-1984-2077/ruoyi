package cn.iocoder.yudao.module.courtcase.service.legal;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.courtcase.controller.admin.legal.vo.*;

import javax.validation.Valid;
import java.util.List;

public interface CourtCaseLegalWorkbenchService {

    CourtCaseLegalSummaryRespVO getSummary(Long userId);

    PageResult<CourtCaseLegalCaseRespVO> getCasePage(Long userId, CourtCaseLegalCasePageReqVO reqVO);

    List<CourtCaseEvidenceRespVO> getEvidenceList(Long userId, Long caseId);

    void createEvidence(Long userId, @Valid CourtCaseEvidenceCreateReqVO reqVO);

    void deleteEvidence(Long userId, Long id) throws Exception;

    CourtCaseDownloadRespVO downloadEvidenceZip(Long userId, Long caseId) throws Exception;

    List<CourtCasePetitionTemplateRespVO> getPetitionTemplateList();

    Long createPetitionTemplate(Long userId, @Valid CourtCasePetitionTemplateSaveReqVO reqVO);

    void updatePetitionTemplate(Long userId, @Valid CourtCasePetitionTemplateSaveReqVO reqVO);

    void deletePetitionTemplate(Long userId, Long id);

    List<CourtCasePetitionRecordRespVO> getPetitionRecordList(Long userId, Long caseId);

    Long generatePetition(Long userId, @Valid CourtCasePetitionGenerateReqVO reqVO) throws Exception;

    void overridePetition(Long userId, @Valid CourtCasePetitionOverrideReqVO reqVO);

    CourtCaseFilingRespVO getFiling(Long userId, Long caseId);

    void saveFiling(Long userId, @Valid CourtCaseFilingSaveReqVO reqVO);
}
