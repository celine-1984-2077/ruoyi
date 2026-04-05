package cn.iocoder.yudao.module.courtcase.service.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.courtcase.controller.admin.service.vo.*;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.*;
import cn.iocoder.yudao.module.courtcase.dal.mysql.cases.*;
import cn.iocoder.yudao.module.courtcase.enums.*;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.courtcase.enums.ErrorCodeConstants.*;

@Service
@Validated
public class CourtCaseServiceWorkbenchServiceImpl implements CourtCaseServiceWorkbenchService {

    private static final List<String> SERVICE_STAGES = Arrays.asList(
            CourtCaseStageEnum.ASSIGN.getStage(),
            CourtCaseStageEnum.REMIND.getStage(),
            CourtCaseStageEnum.TODAY_OVERDUE.getStage(),
            CourtCaseStageEnum.FOLLOW_UP.getStage()
    );

    @Resource
    private CourtCaseMapper courtCaseMapper;
    @Resource
    private CourtCaseFlowLogMapper courtCaseFlowLogMapper;
    @Resource
    private CourtCaseFollowUpMapper courtCaseFollowUpMapper;
    @Resource
    private CourtCaseReminderMapper courtCaseReminderMapper;
    @Resource
    private CourtCaseTransferMapper courtCaseTransferMapper;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    public CourtCaseServiceWorkbenchSummaryRespVO getSummary(Long userId) {
        List<CourtCaseDO> serviceCases = getServiceCases(userId, null);
        Map<Long, CourtCaseReminderDO> reminderMap = getLatestPendingReminderMap(serviceCases);
        CourtCaseServiceWorkbenchSummaryRespVO respVO = new CourtCaseServiceWorkbenchSummaryRespVO();
        long remindCount = 0L;
        long highPriorityCount = 0L;
        long overdueCount = 0L;
        long transferredCount = 0L;
        long manualReminderDueCount = 0L;
        for (CourtCaseDO courtCase : serviceCases) {
            ServiceCaseMetrics metrics = buildMetrics(courtCase, reminderMap.get(courtCase.getId()));
            if (metrics.inReminderPool) {
                remindCount++;
            }
            if (ObjectUtil.equal(metrics.priority, CourtCaseReminderPriorityEnum.HIGH.name())) {
                highPriorityCount++;
            }
            if (metrics.overdueDays > 0) {
                overdueCount++;
            }
            if (ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.TRANSFERRED_TO_LEGAL.getStatus())) {
                transferredCount++;
            }
            if (metrics.manualReminderDue) {
                manualReminderDueCount++;
            }
        }
        respVO.setRemindCount(remindCount);
        respVO.setHighPriorityCount(highPriorityCount);
        respVO.setOverdueCount(overdueCount);
        respVO.setTransferredCount(transferredCount);
        respVO.setManualReminderDueCount(manualReminderDueCount);
        return respVO;
    }

    @Override
    public PageResult<CourtCaseServiceWorkbenchRespVO> getReminderPage(Long userId, CourtCaseServiceWorkbenchPageReqVO reqVO) {
        List<CourtCaseDO> serviceCases = getServiceCases(userId, reqVO);
        Map<Long, CourtCaseReminderDO> reminderMap = getLatestPendingReminderMap(serviceCases);
        List<CourtCaseServiceWorkbenchRespVO> rows = serviceCases.stream()
                .map(courtCase -> toServiceResp(courtCase, reminderMap.get(courtCase.getId())))
                .filter(item -> !Boolean.TRUE.equals(item.getRepaid()))
                .filter(item -> item.getRepaymentDueDate() != null)
                .filter(item -> {
                    long days = ChronoUnit.DAYS.between(LocalDate.now(), item.getRepaymentDueDate());
                    return days <= 7 || Boolean.TRUE.equals(item.getManualReminderDue());
                })
                .sorted(Comparator
                        .comparing((CourtCaseServiceWorkbenchRespVO item) -> "HIGH".equals(item.getPriority()) ? 0 : 1)
                        .thenComparing(CourtCaseServiceWorkbenchRespVO::getRepaymentDueDate, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
        return page(rows, reqVO);
    }

    @Override
    public PageResult<CourtCaseServiceWorkbenchRespVO> getOverduePage(Long userId, CourtCaseServiceWorkbenchPageReqVO reqVO) {
        List<CourtCaseDO> serviceCases = getServiceCases(userId, reqVO);
        Map<Long, CourtCaseReminderDO> reminderMap = getLatestPendingReminderMap(serviceCases);
        List<CourtCaseServiceWorkbenchRespVO> rows = serviceCases.stream()
                .map(courtCase -> toServiceResp(courtCase, reminderMap.get(courtCase.getId())))
                .filter(item -> item.getOverdueDays() != null && item.getOverdueDays() > 0)
                .sorted(Comparator.comparing(CourtCaseServiceWorkbenchRespVO::getOverdueDays, Comparator.reverseOrder()))
                .collect(Collectors.toList());
        return page(rows, reqVO);
    }

    @Override
    public List<CourtCaseFollowUpRespVO> getFollowUpList(Long caseId) {
        validateCaseExists(caseId);
        List<CourtCaseFollowUpDO> list = courtCaseFollowUpMapper.selectListByCaseId(caseId);
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(
                list.stream().map(CourtCaseFollowUpDO::getOperatorId).collect(Collectors.toSet()));
        return list.stream().map(item -> {
            CourtCaseFollowUpRespVO respVO = new CourtCaseFollowUpRespVO();
            respVO.setId(item.getId());
            respVO.setCaseId(item.getCaseId());
            respVO.setOperatorId(item.getOperatorId());
            respVO.setOperatorName(Optional.ofNullable(userMap.get(item.getOperatorId()))
                    .map(AdminUserRespDTO::getNickname).orElse(String.valueOf(item.getOperatorId())));
            respVO.setContent(item.getContent());
            respVO.setAttachmentUrls(item.getAttachmentUrls());
            respVO.setCreateTime(item.getCreateTime());
            return respVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFollowUp(Long userId, @Valid CourtCaseFollowUpCreateReqVO reqVO) {
        CourtCaseDO courtCase = validateCaseExists(reqVO.getCaseId());
        validateServiceOperator(userId, courtCase);
        courtCaseFollowUpMapper.insert(new CourtCaseFollowUpDO()
                .setCaseId(reqVO.getCaseId())
                .setOperatorId(userId)
                .setContent(reqVO.getContent())
                .setAttachmentUrls(reqVO.getAttachmentUrls()));
        courtCase.setLastFollowUpTime(LocalDateTime.now());
        if (ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.PENDING_REPAY.getStatus())
                || ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.REMINDING.getStatus())
                || ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.OVERDUE.getStatus())) {
            courtCase.setCustomerStatus(CourtCaseCustomerStatusEnum.FOLLOWING.getStatus());
        }
        courtCaseMapper.updateById(courtCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCustomReminder(Long userId, @Valid CourtCaseReminderCreateReqVO reqVO) {
        CourtCaseDO courtCase = validateCaseExists(reqVO.getCaseId());
        validateServiceOperator(userId, courtCase);
        CourtCaseReminderPriorityEnum priority = resolvePriority(reqVO.getRemindTime().toLocalDate());
        courtCaseReminderMapper.insert(new CourtCaseReminderDO()
                .setCaseId(reqVO.getCaseId())
                .setReminderType("MANUAL")
                .setRemindTime(reqVO.getRemindTime())
                .setContent(reqVO.getContent())
                .setPriority(priority.name())
                .setStatus(CourtCaseReminderStatusEnum.PENDING.name())
                .setCreatorId(userId));
        courtCase.setNextRemindTime(reqVO.getRemindTime());
        courtCase.setNextRemindContent(reqVO.getContent());
        if (!Boolean.TRUE.equals(courtCase.getRepaid())) {
            courtCase.setCustomerStatus(CourtCaseCustomerStatusEnum.REMINDING.getStatus());
        }
        courtCaseMapper.updateById(courtCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRepaid(Long userId, Long caseId) {
        CourtCaseDO courtCase = validateCaseExists(caseId);
        validateServiceOperator(userId, courtCase);
        LocalDateTime now = LocalDateTime.now();
        String fromStage = courtCase.getCurrentStage();
        courtCase.setRepaid(Boolean.TRUE);
        courtCase.setRepaidTime(now);
        courtCase.setCustomerStatus(CourtCaseCustomerStatusEnum.REPAID.getStatus());
        courtCase.setStatus(CourtCaseStatusEnum.ARCHIVED.getStatus());
        courtCase.setCurrentStage(CourtCaseStageEnum.ARCHIVED.getStage());
        courtCase.setNextRemindTime(null);
        courtCase.setNextRemindContent(null);
        courtCaseMapper.updateById(courtCase);
        markReminderDone(caseId);
        courtCaseFlowLogMapper.insert(new CourtCaseFlowLogDO()
                .setCaseId(caseId)
                .setAction("MARK_REPAID")
                .setFromStage(fromStage)
                .setToStage(CourtCaseStageEnum.ARCHIVED.getStage())
                .setOperatorId(userId)
                .setRemark("客服标记已还款"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferToLegal(Long userId, @Valid CourtCaseTransferReqVO reqVO) {
        CourtCaseDO courtCase = validateCaseExists(reqVO.getCaseId());
        validateServiceOperator(userId, courtCase);
        if (courtCase.getRepaymentDueDate() == null) {
            throw exception(CASE_REPAY_DATE_REQUIRED);
        }
        int overdueDays = (int) Math.max(0, ChronoUnit.DAYS.between(courtCase.getRepaymentDueDate(), LocalDate.now()));
        if (overdueDays <= 15) {
            throw exception(CASE_TRANSFER_NOT_OVERDUE);
        }
        if (reqVO.getReceiverUserId() == null) {
            throw exception(CASE_TRANSFER_RECEIVER_REQUIRED);
        }
        AdminUserRespDTO legalUser = validateOperator(reqVO.getReceiverUserId());
        LocalDateTime now = LocalDateTime.now();
        String fromStage = courtCase.getCurrentStage();
        courtCaseTransferMapper.insert(new CourtCaseTransferDO()
                .setCaseId(reqVO.getCaseId())
                .setFromUserId(userId)
                .setToUserId(reqVO.getReceiverUserId())
                .setReason(reqVO.getReason())
                .setExtraNote(reqVO.getExtraNote())
                .setStatus(CourtCaseTransferStatusEnum.TRANSFERRED.name())
                .setTransferTime(now)
                .setRecallDeadline(now.plusHours(24)));
        courtCase.setCurrentStage(CourtCaseStageEnum.LEGAL.getStage());
        courtCase.setCurrentAssigneeId(reqVO.getReceiverUserId());
        courtCase.setCurrentDeptId(legalUser.getDeptId());
        courtCase.setCustomerStatus(CourtCaseCustomerStatusEnum.TRANSFERRED_TO_LEGAL.getStatus());
        courtCase.setLegalReceiverId(reqVO.getReceiverUserId());
        courtCase.setTransferTime(now);
        courtCase.setTransferRecallDeadline(now.plusHours(24));
        courtCaseMapper.updateById(courtCase);
        markReminderDone(reqVO.getCaseId());
        courtCaseFlowLogMapper.insert(new CourtCaseFlowLogDO()
                .setCaseId(reqVO.getCaseId())
                .setAction(CourtCaseActionEnum.TRANSFER_LEGAL.getAction())
                .setFromStage(fromStage)
                .setToStage(CourtCaseStageEnum.LEGAL.getStage())
                .setOperatorId(userId)
                .setRemark(reqVO.getReason() + (reqVO.getExtraNote() != null ? "：" + reqVO.getExtraNote() : "")));
    }

    private List<CourtCaseDO> getServiceCases(Long userId, CourtCaseServiceWorkbenchPageReqVO reqVO) {
        AdminUserRespDTO operator = validateOperator(userId);
        return courtCaseMapper.selectList(new LambdaQueryWrapperX<CourtCaseDO>()
                .eq(CourtCaseDO::getCurrentDeptId, operator.getDeptId())
                .in(CourtCaseDO::getCurrentStage, SERVICE_STAGES)
                .likeIfPresent(CourtCaseDO::getCaseNo, reqVO != null ? reqVO.getCaseNo() : null)
                .likeIfPresent(CourtCaseDO::getCustomerName, reqVO != null ? reqVO.getCustomerName() : null)
                .orderByAsc(CourtCaseDO::getRepaymentDueDate)
                .orderByDesc(CourtCaseDO::getId));
    }

    private Map<Long, CourtCaseReminderDO> getLatestPendingReminderMap(List<CourtCaseDO> cases) {
        if (CollUtil.isEmpty(cases)) {
            return Collections.emptyMap();
        }
        List<CourtCaseReminderDO> reminders = courtCaseReminderMapper.selectPendingListByCaseIds(
                cases.stream().map(CourtCaseDO::getId).collect(Collectors.toSet()));
        Map<Long, CourtCaseReminderDO> result = new HashMap<>();
        for (CourtCaseReminderDO reminder : reminders) {
            result.putIfAbsent(reminder.getCaseId(), reminder);
        }
        return result;
    }

    private CourtCaseServiceWorkbenchRespVO toServiceResp(CourtCaseDO courtCase, CourtCaseReminderDO reminder) {
        ServiceCaseMetrics metrics = buildMetrics(courtCase, reminder);
        CourtCaseServiceWorkbenchRespVO respVO = new CourtCaseServiceWorkbenchRespVO();
        respVO.setId(courtCase.getId());
        respVO.setCaseNo(courtCase.getCaseNo());
        respVO.setOrderNo(courtCase.getOrderNo());
        respVO.setCustomerName(courtCase.getCustomerName());
        respVO.setMobile(courtCase.getMobile());
        respVO.setAmount(courtCase.getAmount());
        respVO.setRepaymentDueDate(courtCase.getRepaymentDueDate());
        respVO.setOverdueDays(metrics.overdueDays);
        respVO.setOverdueStatus(metrics.overdueStatus);
        respVO.setReminderStatus(metrics.reminderStatus);
        respVO.setPriority(metrics.priority);
        respVO.setCustomerStatus(resolveDisplayCustomerStatus(courtCase, metrics));
        respVO.setCurrentStage(courtCase.getCurrentStage());
        respVO.setCurrentAssigneeId(courtCase.getCurrentAssigneeId());
        respVO.setNextRemindTime(courtCase.getNextRemindTime());
        respVO.setNextRemindContent(courtCase.getNextRemindContent());
        respVO.setLastFollowUpTime(courtCase.getLastFollowUpTime());
        respVO.setRepaid(Boolean.TRUE.equals(courtCase.getRepaid()));
        respVO.setManualReminderDue(metrics.manualReminderDue);
        respVO.setCanTransferLegal(metrics.overdueDays > 15);
        respVO.setCreateTime(courtCase.getCreateTime());
        return respVO;
    }

    private ServiceCaseMetrics buildMetrics(CourtCaseDO courtCase, CourtCaseReminderDO reminder) {
        ServiceCaseMetrics metrics = new ServiceCaseMetrics();
        LocalDate today = LocalDate.now();
        int overdueDays = 0;
        if (courtCase.getRepaymentDueDate() != null) {
            overdueDays = (int) ChronoUnit.DAYS.between(courtCase.getRepaymentDueDate(), today);
        }
        metrics.overdueDays = Math.max(overdueDays, 0);
        if (Boolean.TRUE.equals(courtCase.getRepaid())) {
            metrics.overdueStatus = "已还款";
            metrics.reminderStatus = "已还款";
            metrics.priority = CourtCaseReminderPriorityEnum.NORMAL.name();
            return metrics;
        }
        if (metrics.overdueDays > 0) {
            metrics.overdueStatus = "已逾期 " + metrics.overdueDays + " 天";
        } else {
            metrics.overdueStatus = "未逾期";
        }
        long daysUntilDue = courtCase.getRepaymentDueDate() == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(today, courtCase.getRepaymentDueDate());
        metrics.inReminderPool = daysUntilDue <= 7;
        metrics.priority = daysUntilDue <= 3 || metrics.overdueDays > 0
                ? CourtCaseReminderPriorityEnum.HIGH.name()
                : CourtCaseReminderPriorityEnum.NORMAL.name();
        if (reminder != null) {
            metrics.manualReminderDue = reminder.getRemindTime() != null && !reminder.getRemindTime().isAfter(LocalDateTime.now());
            metrics.reminderStatus = metrics.manualReminderDue ? "待弹窗提醒" : "已设置下次提醒";
        } else if (courtCase.getNextRemindTime() != null) {
            metrics.manualReminderDue = !courtCase.getNextRemindTime().isAfter(LocalDateTime.now());
            metrics.reminderStatus = metrics.manualReminderDue ? "待弹窗提醒" : "已设置下次提醒";
        } else if (metrics.inReminderPool) {
            metrics.reminderStatus = "待提醒";
        } else {
            metrics.reminderStatus = "未到提醒期";
        }
        return metrics;
    }

    private void validateServiceOperator(Long userId, CourtCaseDO courtCase) {
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

    private CourtCaseDO validateCaseExists(Long caseId) {
        CourtCaseDO courtCase = courtCaseMapper.selectById(caseId);
        if (courtCase == null) {
            throw exception(CASE_NOT_EXISTS);
        }
        return courtCase;
    }

    private void markReminderDone(Long caseId) {
        List<CourtCaseReminderDO> reminders = courtCaseReminderMapper.selectPendingListByCaseIds(Collections.singleton(caseId));
        reminders.forEach(item -> {
            item.setStatus(CourtCaseReminderStatusEnum.DONE.name());
            item.setProcessedTime(LocalDateTime.now());
            courtCaseReminderMapper.updateById(item);
        });
    }

    private CourtCaseReminderPriorityEnum resolvePriority(LocalDate remindDate) {
        return ChronoUnit.DAYS.between(LocalDate.now(), remindDate) <= 3
                ? CourtCaseReminderPriorityEnum.HIGH
                : CourtCaseReminderPriorityEnum.NORMAL;
    }

    private PageResult<CourtCaseServiceWorkbenchRespVO> page(List<CourtCaseServiceWorkbenchRespVO> rows,
                                                             CourtCaseServiceWorkbenchPageReqVO reqVO) {
        int start = (reqVO.getPageNo() - 1) * reqVO.getPageSize();
        if (start >= rows.size()) {
            return new PageResult<>(Collections.emptyList(), (long) rows.size());
        }
        int end = Math.min(start + reqVO.getPageSize(), rows.size());
        return new PageResult<>(rows.subList(start, end), (long) rows.size());
    }

    private static class ServiceCaseMetrics {
        private int overdueDays;
        private String overdueStatus;
        private String reminderStatus;
        private String priority;
        private boolean manualReminderDue;
        private boolean inReminderPool;
    }

    private String resolveDisplayCustomerStatus(CourtCaseDO courtCase, ServiceCaseMetrics metrics) {
        if (Boolean.TRUE.equals(courtCase.getRepaid())) {
            return CourtCaseCustomerStatusEnum.REPAID.getStatus();
        }
        if (ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.TRANSFERRED_TO_LEGAL.getStatus())) {
            return courtCase.getCustomerStatus();
        }
        if (metrics.overdueDays > 0) {
            return CourtCaseCustomerStatusEnum.OVERDUE.getStatus();
        }
        if (ObjectUtil.equal(courtCase.getCustomerStatus(), CourtCaseCustomerStatusEnum.FOLLOWING.getStatus())) {
            return courtCase.getCustomerStatus();
        }
        if (metrics.inReminderPool || courtCase.getNextRemindTime() != null) {
            return CourtCaseCustomerStatusEnum.REMINDING.getStatus();
        }
        return CourtCaseCustomerStatusEnum.PENDING_REPAY.getStatus();
    }
}
