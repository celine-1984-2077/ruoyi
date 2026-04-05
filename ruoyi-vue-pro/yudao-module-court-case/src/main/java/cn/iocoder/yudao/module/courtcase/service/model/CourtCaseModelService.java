package cn.iocoder.yudao.module.courtcase.service.model;

import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelConfigRespVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelDraftSaveReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.model.vo.CourtCaseModelRespVO;

public interface CourtCaseModelService {

    CourtCaseModelConfigRespVO getModelConfig();

    CourtCaseModelRespVO saveDraft(CourtCaseModelDraftSaveReqVO reqVO);

    CourtCaseModelRespVO publishDraft(CourtCaseModelDraftSaveReqVO reqVO);

    void deleteDraft(Long id);

    String normalizeAndValidateExtJson(String extJson);
}
