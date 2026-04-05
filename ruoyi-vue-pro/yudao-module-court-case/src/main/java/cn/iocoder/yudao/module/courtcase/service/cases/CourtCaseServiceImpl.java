package cn.iocoder.yudao.module.courtcase.service.cases;

import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCaseAdvanceReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCaseCreateReqVO;
import cn.iocoder.yudao.module.courtcase.controller.admin.cases.vo.CourtCasePageReqVO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseFlowLogDO;
import cn.iocoder.yudao.module.courtcase.dal.mysql.cases.CourtCaseFlowLogMapper;
import cn.iocoder.yudao.module.courtcase.dal.mysql.cases.CourtCaseMapper;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseActionEnum;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseCustomerStatusEnum;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseStageEnum;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseStatusEnum;
import cn.iocoder.yudao.module.courtcase.service.model.CourtCaseModelService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.courtcase.enums.ErrorCodeConstants.*;

@Service
@Validated
public class CourtCaseServiceImpl implements CourtCaseService {

    @Resource
    private CourtCaseMapper courtCaseMapper;
    @Resource
    private CourtCaseFlowLogMapper courtCaseFlowLogMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private CourtCaseModelService courtCaseModelService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCase(Long userId, @Valid CourtCaseCreateReqVO createReqVO) {
        CourtCaseDO courtCase = BeanUtils.toBean(createReqVO, CourtCaseDO.class)
                .setCurrentStage(CourtCaseStageEnum.ASSIGN.getStage())
                .setStatus(CourtCaseStatusEnum.PROCESSING.getStatus())
                .setCustomerStatus(CourtCaseCustomerStatusEnum.PENDING_REPAY.getStatus())
                .setRepaid(Boolean.FALSE)
                .setExtJson(courtCaseModelService.normalizeAndValidateExtJson(createReqVO.getExtJson()));
        fillAssigneeAndDept(courtCase, createReqVO.getCurrentAssigneeId(), userId);
        courtCaseMapper.insert(courtCase);
        insertFlowLog(courtCase.getId(), CourtCaseActionEnum.SUBMIT_ASSIGN.getAction(),
                CourtCaseStageEnum.IMPORT.getStage(), CourtCaseStageEnum.ASSIGN.getStage(),
                userId, "案件建档并进入分单阶段");
        return courtCase.getId();
    }

    @Override
    public CourtCaseDO getCase(Long id) {
        return validateCaseExists(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCase(Long id) {
        CourtCaseDO courtCase = validateCaseExists(id);
        if (ObjectUtil.equal(courtCase.getStatus(), CourtCaseStatusEnum.ARCHIVED.getStatus())) {
            throw exception(CASE_DELETE_ARCHIVED_FORBIDDEN);
        }
        courtCaseFlowLogMapper.deleteByCaseId(id);
        courtCaseMapper.deleteById(id);
    }

    @Override
    public PageResult<CourtCaseDO> getCasePage(CourtCasePageReqVO pageReqVO) {
        return courtCaseMapper.selectPage(pageReqVO);
    }

    @Override
    public PageResult<CourtCaseDO> getWorkbenchPage(Long userId, CourtCasePageReqVO pageReqVO) {
        AdminUserRespDTO operator = validateOperator(userId);
        pageReqVO.setCurrentDeptId(operator.getDeptId());
        return courtCaseMapper.selectPage(pageReqVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void advanceCase(Long userId, @Valid CourtCaseAdvanceReqVO advanceReqVO) {
        CourtCaseDO courtCase = validateCaseExists(advanceReqVO.getId());
        AdminUserRespDTO operator = validateOperator(userId);
        if (!ObjectUtil.equal(courtCase.getCurrentDeptId(), operator.getDeptId())) {
            throw exception(CASE_OPERATOR_NOT_IN_DEPT);
        }
        if (courtCase.getCurrentAssigneeId() != null && !ObjectUtil.equal(courtCase.getCurrentAssigneeId(), userId)) {
            throw exception(CASE_OPERATOR_NOT_ASSIGNEE);
        }

        CourtCaseActionEnum actionEnum = CourtCaseActionEnum.valueOfAction(advanceReqVO.getAction());
        if (actionEnum == null) {
            throw exception(CASE_ACTION_NOT_SUPPORTED);
        }
        CourtCaseStageEnum fromStage = CourtCaseStageEnum.valueOfStage(courtCase.getCurrentStage());
        CourtCaseStageEnum toStage = calculateNextStage(fromStage, actionEnum);
        if (toStage == null) {
            throw exception(CASE_STAGE_NOT_MATCH);
        }

        courtCase.setCurrentStage(toStage.getStage());
        if (toStage == CourtCaseStageEnum.ARCHIVED) {
            courtCase.setStatus(CourtCaseStatusEnum.ARCHIVED.getStatus());
            courtCase.setCurrentDeptId(operator.getDeptId());
            courtCase.setCurrentAssigneeId(userId);
        } else {
            fillAssigneeAndDept(courtCase, advanceReqVO.getNextAssigneeId(), userId);
        }
        courtCaseMapper.updateById(courtCase);
        insertFlowLog(courtCase.getId(), actionEnum.getAction(), fromStage.getStage(),
                toStage.getStage(), userId, advanceReqVO.getRemark());
    }

    @Override
    public List<CourtCaseFlowLogDO> getCaseFlowLogs(Long caseId) {
        validateCaseExists(caseId);
        return courtCaseFlowLogMapper.selectListByCaseId(caseId);
    }

    private CourtCaseDO validateCaseExists(Long id) {
        CourtCaseDO courtCase = courtCaseMapper.selectById(id);
        if (courtCase == null) {
            throw exception(CASE_NOT_EXISTS);
        }
        return courtCase;
    }

    private AdminUserRespDTO validateOperator(Long userId) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        adminUserApi.validateUser(userId);
        return user;
    }

    private void fillAssigneeAndDept(CourtCaseDO courtCase, Long nextAssigneeId, Long fallbackUserId) {
        Long assigneeId = nextAssigneeId != null ? nextAssigneeId : fallbackUserId;
        AdminUserRespDTO assignee = adminUserApi.getUser(assigneeId);
        adminUserApi.validateUser(assigneeId);
        courtCase.setCurrentAssigneeId(assigneeId);
        courtCase.setCurrentDeptId(assignee.getDeptId());
    }

    private void insertFlowLog(Long caseId, String action, String fromStage, String toStage,
                               Long operatorId, String remark) {
        courtCaseFlowLogMapper.insert(new CourtCaseFlowLogDO()
                .setCaseId(caseId)
                .setAction(action)
                .setFromStage(fromStage)
                .setToStage(toStage)
                .setOperatorId(operatorId)
                .setRemark(remark));
    }

    private CourtCaseStageEnum calculateNextStage(CourtCaseStageEnum fromStage, CourtCaseActionEnum actionEnum) {
        if (fromStage == null) {
            return null;
        }
        switch (fromStage) {
            case ASSIGN:
                return actionEnum == CourtCaseActionEnum.SUBMIT_ASSIGN ? CourtCaseStageEnum.REMIND : null;
            case REMIND:
                return actionEnum == CourtCaseActionEnum.SUBMIT_REMIND ? CourtCaseStageEnum.TODAY_OVERDUE : null;
            case TODAY_OVERDUE:
                return actionEnum == CourtCaseActionEnum.SUBMIT_TODAY_OVERDUE ? CourtCaseStageEnum.FOLLOW_UP : null;
            case FOLLOW_UP:
                if (actionEnum == CourtCaseActionEnum.SUBMIT_FOLLOW_UP || actionEnum == CourtCaseActionEnum.TRANSFER_LEGAL) {
                    return CourtCaseStageEnum.LEGAL;
                }
                return null;
            case LEGAL:
                return actionEnum == CourtCaseActionEnum.FILE_LAWSUIT ? CourtCaseStageEnum.LITIGATION : null;
            case LITIGATION:
                return actionEnum == CourtCaseActionEnum.ARCHIVE ? CourtCaseStageEnum.ARCHIVED : null;
            default:
                return null;
        }
    }
}
