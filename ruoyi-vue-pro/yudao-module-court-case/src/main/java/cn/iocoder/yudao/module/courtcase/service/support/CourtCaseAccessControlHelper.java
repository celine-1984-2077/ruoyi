package cn.iocoder.yudao.module.courtcase.service.support;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.iocoder.yudao.module.courtcase.dal.dataobject.cases.CourtCaseDO;
import cn.iocoder.yudao.module.courtcase.enums.CourtCaseStageEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.permission.PermissionApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CourtCaseAccessControlHelper {

    public static final List<String> SERVICE_STAGES = Arrays.asList(
            CourtCaseStageEnum.ASSIGN.getStage(),
            CourtCaseStageEnum.REMIND.getStage(),
            CourtCaseStageEnum.TODAY_OVERDUE.getStage(),
            CourtCaseStageEnum.FOLLOW_UP.getStage()
    );

    public static final List<String> LEGAL_STAGES = Arrays.asList(
            CourtCaseStageEnum.LEGAL.getStage(),
            CourtCaseStageEnum.LITIGATION.getStage(),
            CourtCaseStageEnum.ARCHIVED.getStage()
    );

    private static final String ROLE_SUPER_ADMIN = "super_admin";
    private static final String ROLE_SERVICE_AGENT = "court_service_agent";
    private static final String ROLE_SERVICE_MANAGER = "court_service_manager";
    private static final String ROLE_LEGAL_AGENT = "court_legal_agent";
    private static final String ROLE_LEGAL_MANAGER = "court_legal_manager";

    @Resource
    private PermissionApi permissionApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private AdminUserApi adminUserApi;

    public boolean isSuperAdmin(Long userId) {
        return permissionApi.hasAnyRoles(userId, ROLE_SUPER_ADMIN);
    }

    public boolean canViewCase(Long userId, CourtCaseDO courtCase) {
        return canViewCase(buildProfile(userId), courtCase);
    }

    public boolean canManageServiceCase(Long userId, CourtCaseDO courtCase) {
        AccessProfile profile = buildProfile(userId);
        if (profile.superAdmin) {
            return true;
        }
        if (!SERVICE_STAGES.contains(courtCase.getCurrentStage())) {
            return false;
        }
        if (profile.serviceManager && profile.deptIds.contains(courtCase.getCurrentDeptId())) {
            return true;
        }
        return profile.serviceAgent && ObjectUtil.equal(courtCase.getCurrentAssigneeId(), userId);
    }

    public boolean canManageLegalCase(Long userId, CourtCaseDO courtCase) {
        AccessProfile profile = buildProfile(userId);
        if (profile.superAdmin) {
            return true;
        }
        if (!LEGAL_STAGES.contains(courtCase.getCurrentStage())) {
            return false;
        }
        if (profile.legalManager && profile.deptIds.contains(courtCase.getCurrentDeptId())) {
            return true;
        }
        return profile.legalAgent && ObjectUtil.equal(courtCase.getCurrentAssigneeId(), userId);
    }

    public List<CourtCaseDO> filterCaseList(Long userId, List<CourtCaseDO> cases) {
        AccessProfile profile = buildProfile(userId);
        return cases.stream()
                .filter(item -> canViewCase(profile, item))
                .collect(Collectors.toList());
    }

    public List<CourtCaseDO> filterServiceCases(Long userId, List<CourtCaseDO> cases) {
        AccessProfile profile = buildProfile(userId);
        return cases.stream()
                .filter(item -> SERVICE_STAGES.contains(item.getCurrentStage()))
                .filter(item -> canViewCase(profile, item))
                .collect(Collectors.toList());
    }

    public List<CourtCaseDO> filterLegalCases(Long userId, List<CourtCaseDO> cases) {
        AccessProfile profile = buildProfile(userId);
        return cases.stream()
                .filter(item -> LEGAL_STAGES.contains(item.getCurrentStage()))
                .filter(item -> canViewCase(profile, item))
                .collect(Collectors.toList());
    }

    private boolean canViewCase(AccessProfile profile, CourtCaseDO courtCase) {
        if (profile.superAdmin) {
            return true;
        }
        String currentStage = courtCase.getCurrentStage();
        if (SERVICE_STAGES.contains(currentStage)) {
            if (profile.serviceManager && profile.deptIds.contains(courtCase.getCurrentDeptId())) {
                return true;
            }
            return profile.serviceAgent && ObjectUtil.equal(courtCase.getCurrentAssigneeId(), profile.userId);
        }
        if (LEGAL_STAGES.contains(currentStage)) {
            if (profile.legalManager && profile.deptIds.contains(courtCase.getCurrentDeptId())) {
                return true;
            }
            return profile.legalAgent && ObjectUtil.equal(courtCase.getCurrentAssigneeId(), profile.userId);
        }
        return false;
    }

    private AccessProfile buildProfile(Long userId) {
        AdminUserRespDTO user = adminUserApi.getUser(userId);
        adminUserApi.validateUser(userId);
        AccessProfile profile = new AccessProfile();
        profile.userId = userId;
        profile.superAdmin = permissionApi.hasAnyRoles(userId, ROLE_SUPER_ADMIN);
        if (profile.superAdmin) {
            return profile;
        }
        profile.serviceAgent = permissionApi.hasAnyRoles(userId, ROLE_SERVICE_AGENT);
        profile.serviceManager = permissionApi.hasAnyRoles(userId, ROLE_SERVICE_MANAGER);
        profile.legalAgent = permissionApi.hasAnyRoles(userId, ROLE_LEGAL_AGENT);
        profile.legalManager = permissionApi.hasAnyRoles(userId, ROLE_LEGAL_MANAGER);
        if ((profile.serviceManager || profile.legalManager) && user.getDeptId() != null) {
            profile.deptIds.add(user.getDeptId());
            List<DeptRespDTO> childDeptList = deptApi.getChildDeptList(user.getDeptId());
            if (CollUtil.isNotEmpty(childDeptList)) {
                profile.deptIds.addAll(childDeptList.stream().map(DeptRespDTO::getId).collect(Collectors.toSet()));
            }
        }
        return profile;
    }

    private static class AccessProfile {
        private Long userId;
        private boolean superAdmin;
        private boolean serviceAgent;
        private boolean serviceManager;
        private boolean legalAgent;
        private boolean legalManager;
        private final Set<Long> deptIds = new HashSet<>();
    }
}
