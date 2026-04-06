-- ------------------------------------------------------------
-- 案件中心：组织、岗位、角色、菜单、测试用户初始化
-- 可重复执行，面向本地演示环境
-- 默认测试密码沿用 admin 的加密串，对应密码：admin123
-- ------------------------------------------------------------

SET NAMES utf8mb4;

-- 1. 部门
INSERT INTO system_dept (id, name, parent_id, sort, leader_user_id, phone, email, status, creator, updater, deleted, tenant_id)
VALUES
    (124, '客服中心', 100, 210, NULL, '18810001240', 'service@court.local', 0, '1', '1', b'0', 1),
    (125, '法诉中心', 100, 220, NULL, '18810001250', 'legal@court.local', 0, '1', '1', b'0', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    parent_id = VALUES(parent_id),
    sort = VALUES(sort),
    phone = VALUES(phone),
    email = VALUES(email),
    status = VALUES(status),
    deleted = b'0',
    tenant_id = VALUES(tenant_id);

-- 2. 岗位
INSERT INTO system_post (id, code, name, sort, status, remark, creator, updater, deleted, tenant_id)
VALUES
    (8, 'court_service_agent', '客服专员', 20, 0, '案件中心-客服专员', '1', '1', b'0', 1),
    (9, 'court_service_manager', '客服主管', 21, 0, '案件中心-客服主管', '1', '1', b'0', 1),
    (10, 'court_legal_agent', '法诉专员', 22, 0, '案件中心-法诉专员', '1', '1', b'0', 1),
    (11, 'court_legal_manager', '法诉主管', 23, 0, '案件中心-法诉主管', '1', '1', b'0', 1)
ON DUPLICATE KEY UPDATE
    code = VALUES(code),
    name = VALUES(name),
    sort = VALUES(sort),
    status = VALUES(status),
    remark = VALUES(remark),
    deleted = b'0',
    tenant_id = VALUES(tenant_id);

-- 3. 角色
INSERT INTO system_role (id, name, code, sort, data_scope, data_scope_dept_ids, status, type, remark, creator, updater, deleted, tenant_id)
VALUES
    (156, '客服专员', 'court_service_agent', 20, 5, '', 0, 1, '仅查看本人负责的客服案件', '1', '1', b'0', 1),
    (157, '客服主管', 'court_service_manager', 21, 4, '', 0, 1, '查看客服部门全部案件', '1', '1', b'0', 1),
    (158, '法诉专员', 'court_legal_agent', 22, 5, '', 0, 1, '仅查看本人负责的法诉案件', '1', '1', b'0', 1),
    (159, '法诉主管', 'court_legal_manager', 23, 4, '', 0, 1, '查看法诉部门全部案件', '1', '1', b'0', 1)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    code = VALUES(code),
    sort = VALUES(sort),
    data_scope = VALUES(data_scope),
    status = VALUES(status),
    type = VALUES(type),
    remark = VALUES(remark),
    deleted = b'0',
    tenant_id = VALUES(tenant_id);

-- 4. 案件中心菜单
UPDATE system_menu
SET name = '客服工作台',
    path = 'service-workbench',
    component = 'courtCase/serviceWorkbench/index',
    component_name = 'CourtCaseServiceWorkbench',
    sort = 2,
    visible = b'1',
    keep_alive = b'1',
    always_show = b'0',
    status = 0,
    deleted = b'0'
WHERE id = 5069;

UPDATE system_menu
SET name = '客服工作台查询',
    permission = 'court-case:service:query',
    status = 0,
    deleted = b'0'
WHERE id = 5070;

UPDATE system_menu
SET name = '跟进备注新增',
    permission = 'court-case:service:follow-up:create',
    status = 0,
    deleted = b'0'
WHERE id = 5071;

INSERT INTO system_menu (id, name, permission, type, sort, parent_id, path, icon, component, component_name, status, visible, keep_alive, always_show, creator, updater, deleted)
VALUES
    (5072, '我的跟进', '', 2, 3, 5069, 'service-follow-up', 'ep:edit-pen', 'courtCase/serviceWorkbench/index', 'CourtCaseServiceFollowUp', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5073, '我的逾期客户', '', 2, 4, 5069, 'service-overdue', 'ep:warning', 'courtCase/serviceWorkbench/index', 'CourtCaseServiceOverdue', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5074, '法务工作台', '', 2, 5, 5059, 'legal-workbench', 'ep:briefcase', 'courtCase/legalWorkbench/index', 'CourtCaseLegalWorkbenchMenu', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5075, '我的案件', '', 2, 1, 5074, 'legal-my-cases', 'ep:files', 'courtCase/legalWorkbench/index', 'CourtCaseLegalMyCases', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5076, '证据管理', '', 2, 2, 5074, 'legal-evidence', 'ep:folder-opened', 'courtCase/legalWorkbench/index', 'CourtCaseLegalEvidence', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5077, '诉状管理', '', 2, 3, 5074, 'legal-petition', 'ep:document', 'courtCase/legalWorkbench/index', 'CourtCaseLegalPetition', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5078, '立案跟踪', '', 2, 4, 5074, 'legal-filing', 'ep:collection-tag', 'courtCase/legalWorkbench/index', 'CourtCaseLegalFiling', 0, b'1', b'1', b'0', '1', '1', b'0'),
    (5079, '设置还款提醒', 'court-case:service:reminder:create', 3, 3, 5069, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5080, '标记已还款', 'court-case:service:mark-repaid', 3, 4, 5069, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5081, '移交法诉', 'court-case:service:transfer:create', 3, 5, 5069, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5082, '法务工作台查询', 'court-case:legal:query', 3, 1, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5083, '证据上传', 'court-case:legal:evidence:create', 3, 2, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5084, '证据删除', 'court-case:legal:evidence:delete', 3, 3, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5085, '证据打包下载', 'court-case:legal:evidence:download', 3, 4, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5086, '诉状生成', 'court-case:legal:petition:generate', 3, 5, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5087, '诉状覆盖上传', 'court-case:legal:petition:override', 3, 6, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5088, '诉状模板管理', 'court-case:legal:template:manage', 3, 7, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5089, '立案信息保存', 'court-case:legal:filing:save', 3, 8, 5074, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0'),
    (5090, '案件删除', 'court-case:case:delete', 3, 5, 5060, '', '', '', '', 0, b'1', b'1', b'1', '1', '1', b'0')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    permission = VALUES(permission),
    type = VALUES(type),
    sort = VALUES(sort),
    parent_id = VALUES(parent_id),
    path = VALUES(path),
    icon = VALUES(icon),
    component = VALUES(component),
    component_name = VALUES(component_name),
    status = VALUES(status),
    visible = VALUES(visible),
    keep_alive = VALUES(keep_alive),
    always_show = VALUES(always_show),
    deleted = b'0';

-- 5. 角色菜单授权
DELETE FROM system_role_menu WHERE role_id IN (156, 157, 158, 159);
DELETE FROM system_role_menu WHERE role_id = 1 AND menu_id IN (5072,5073,5074,5075,5076,5077,5078,5079,5080,5081,5082,5083,5084,5085,5086,5087,5088,5089,5090);

INSERT INTO system_role_menu (id, role_id, menu_id, creator, updater, deleted, tenant_id)
VALUES
    (6374, 1, 5072, '1', '1', b'0', 1),
    (6375, 1, 5073, '1', '1', b'0', 1),
    (6376, 1, 5074, '1', '1', b'0', 1),
    (6377, 1, 5075, '1', '1', b'0', 1),
    (6378, 1, 5076, '1', '1', b'0', 1),
    (6379, 1, 5077, '1', '1', b'0', 1),
    (6380, 1, 5078, '1', '1', b'0', 1),
    (6381, 1, 5079, '1', '1', b'0', 1),
    (6382, 1, 5080, '1', '1', b'0', 1),
    (6383, 1, 5081, '1', '1', b'0', 1),
    (6384, 1, 5082, '1', '1', b'0', 1),
    (6385, 1, 5083, '1', '1', b'0', 1),
    (6386, 1, 5084, '1', '1', b'0', 1),
    (6387, 1, 5085, '1', '1', b'0', 1),
    (6388, 1, 5086, '1', '1', b'0', 1),
    (6389, 1, 5087, '1', '1', b'0', 1),
    (6390, 1, 5088, '1', '1', b'0', 1),
    (6391, 1, 5089, '1', '1', b'0', 1),
    (6392, 1, 5090, '1', '1', b'0', 1),
    (6393, 156, 5059, '1', '1', b'0', 1),
    (6394, 156, 5069, '1', '1', b'0', 1),
    (6395, 156, 5070, '1', '1', b'0', 1),
    (6396, 156, 5071, '1', '1', b'0', 1),
    (6397, 156, 5072, '1', '1', b'0', 1),
    (6398, 156, 5073, '1', '1', b'0', 1),
    (6399, 156, 5079, '1', '1', b'0', 1),
    (6400, 156, 5080, '1', '1', b'0', 1),
    (6401, 156, 5081, '1', '1', b'0', 1),
    (6402, 157, 5059, '1', '1', b'0', 1),
    (6403, 157, 5069, '1', '1', b'0', 1),
    (6404, 157, 5070, '1', '1', b'0', 1),
    (6405, 157, 5072, '1', '1', b'0', 1),
    (6406, 157, 5073, '1', '1', b'0', 1),
    (6407, 158, 5059, '1', '1', b'0', 1),
    (6408, 158, 5074, '1', '1', b'0', 1),
    (6409, 158, 5075, '1', '1', b'0', 1),
    (6410, 158, 5076, '1', '1', b'0', 1),
    (6411, 158, 5077, '1', '1', b'0', 1),
    (6412, 158, 5078, '1', '1', b'0', 1),
    (6413, 158, 5082, '1', '1', b'0', 1),
    (6414, 158, 5083, '1', '1', b'0', 1),
    (6415, 158, 5084, '1', '1', b'0', 1),
    (6416, 158, 5085, '1', '1', b'0', 1),
    (6417, 158, 5086, '1', '1', b'0', 1),
    (6418, 158, 5087, '1', '1', b'0', 1),
    (6419, 158, 5089, '1', '1', b'0', 1),
    (6420, 159, 5059, '1', '1', b'0', 1),
    (6421, 159, 5074, '1', '1', b'0', 1),
    (6422, 159, 5075, '1', '1', b'0', 1),
    (6423, 159, 5076, '1', '1', b'0', 1),
    (6424, 159, 5077, '1', '1', b'0', 1),
    (6425, 159, 5078, '1', '1', b'0', 1),
    (6426, 159, 5082, '1', '1', b'0', 1),
    (6427, 159, 5083, '1', '1', b'0', 1),
    (6428, 159, 5084, '1', '1', b'0', 1),
    (6429, 159, 5085, '1', '1', b'0', 1),
    (6430, 159, 5086, '1', '1', b'0', 1),
    (6431, 159, 5087, '1', '1', b'0', 1),
    (6432, 159, 5089, '1', '1', b'0', 1);

-- 6. 用户
INSERT INTO system_users (id, username, password, nickname, remark, dept_id, post_ids, email, mobile, sex, avatar, status, creator, updater, deleted, tenant_id)
VALUES
    (145, 'csagent01', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '客服专员一号', '案件中心测试账号', 124, '[8]', 'csagent01@court.local', '18800000045', 0, '', 0, '1', '1', b'0', 1),
    (146, 'csagent02', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '客服专员二号', '案件中心测试账号', 124, '[8]', 'csagent02@court.local', '18800000046', 0, '', 0, '1', '1', b'0', 1),
    (147, 'csmanager01', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '客服主管一号', '案件中心测试账号', 124, '[9]', 'csmanager01@court.local', '18800000047', 0, '', 0, '1', '1', b'0', 1),
    (148, 'legalagent01', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '法诉专员一号', '案件中心测试账号', 125, '[10]', 'legalagent01@court.local', '18800000048', 0, '', 0, '1', '1', b'0', 1),
    (149, 'legalagent02', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '法诉专员二号', '案件中心测试账号', 125, '[10]', 'legalagent02@court.local', '18800000049', 0, '', 0, '1', '1', b'0', 1),
    (150, 'legalmanager01', '$2a$04$.vd8nPeLwxt6hnSzmAoAyul8BOLX7Cib6QhcxRe30rfvrIPQHH1OG', '法诉主管一号', '案件中心测试账号', 125, '[11]', 'legalmanager01@court.local', '18800000050', 0, '', 0, '1', '1', b'0', 1)
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password = VALUES(password),
    nickname = VALUES(nickname),
    remark = VALUES(remark),
    dept_id = VALUES(dept_id),
    post_ids = VALUES(post_ids),
    email = VALUES(email),
    mobile = VALUES(mobile),
    status = VALUES(status),
    deleted = b'0',
    tenant_id = VALUES(tenant_id);

UPDATE system_dept SET leader_user_id = 147 WHERE id = 124;
UPDATE system_dept SET leader_user_id = 150 WHERE id = 125;

-- 7. 用户角色 / 用户岗位
DELETE FROM system_user_role WHERE user_id IN (145,146,147,148,149,150);
DELETE FROM system_user_post WHERE user_id IN (145,146,147,148,149,150);

INSERT INTO system_user_role (id, user_id, role_id, creator, updater, deleted, tenant_id)
VALUES
    (55, 145, 156, '1', '1', b'0', 1),
    (56, 146, 156, '1', '1', b'0', 1),
    (57, 147, 157, '1', '1', b'0', 1),
    (58, 148, 158, '1', '1', b'0', 1),
    (59, 149, 158, '1', '1', b'0', 1),
    (60, 150, 159, '1', '1', b'0', 1);

INSERT INTO system_user_post (id, user_id, post_id, creator, updater, deleted, tenant_id)
VALUES
    (130, 145, 8, '1', '1', b'0', 1),
    (131, 146, 8, '1', '1', b'0', 1),
    (132, 147, 9, '1', '1', b'0', 1),
    (133, 148, 10, '1', '1', b'0', 1),
    (134, 149, 10, '1', '1', b'0', 1),
    (135, 150, 11, '1', '1', b'0', 1);

-- 8. 演示案件重新分配到客服 / 法诉部门，便于权限验证
UPDATE court_case
SET current_dept_id = 124,
    current_assignee_id = CASE WHEN MOD(id, 2) = 0 THEN 146 ELSE 145 END,
    legal_receiver_id = NULL
WHERE current_stage IN ('ASSIGN', 'REMIND', 'TODAY_OVERDUE', 'FOLLOW_UP');

UPDATE court_case
SET current_dept_id = 125,
    current_assignee_id = CASE WHEN MOD(id, 2) = 0 THEN 149 ELSE 148 END,
    legal_receiver_id = CASE WHEN MOD(id, 2) = 0 THEN 149 ELSE 148 END
WHERE current_stage IN ('LEGAL', 'LITIGATION', 'ARCHIVED');
