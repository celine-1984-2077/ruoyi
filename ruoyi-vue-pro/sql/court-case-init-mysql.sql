-- court-case 模块初始化数据（MySQL）
-- 说明：
-- 1. 本文件用于插入案件模块的菜单、权限点与字典数据；
-- 2. 默认将目录直接挂到根菜单，如需调整层级，请修改 parent_id；
-- 3. 默认授权给管理员角色（role_id = 1），如你的环境不同请手动调整。

-- ==================== 字典类型 ====================
DELETE FROM system_dict_data WHERE dict_type IN ('court_case_stage', 'court_case_status');
DELETE FROM system_dict_type WHERE type IN ('court_case_stage', 'court_case_status');

INSERT INTO system_dict_type (name, type, status, remark)
VALUES ('案件阶段', 'court_case_stage', 0, '案件主流程阶段');

INSERT INTO system_dict_type (name, type, status, remark)
VALUES ('案件状态', 'court_case_status', 0, '案件主状态');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark)
VALUES
  (1, '导入建档', 'IMPORT', 'court_case_stage', 0, 'info', '', '案件初始导入'),
  (2, '分单', 'ASSIGN', 'court_case_stage', 0, 'warning', '', '分单阶段'),
  (3, '预提醒', 'REMIND', 'court_case_stage', 0, 'primary', '', '预提醒阶段'),
  (4, '当日未还', 'TODAY_OVERDUE', 'court_case_stage', 0, 'danger', '', '当日未还处理'),
  (5, '追客', 'FOLLOW_UP', 'court_case_stage', 0, 'warning', '', '追客阶段'),
  (6, '法务', 'LEGAL', 'court_case_stage', 0, 'primary', '', '法务审核阶段'),
  (7, '诉讼中', 'LITIGATION', 'court_case_stage', 0, 'danger', '', '法院处理中'),
  (8, '已归档', 'ARCHIVED', 'court_case_stage', 0, 'success', '', '案件归档');

INSERT INTO system_dict_data (sort, label, value, dict_type, status, color_type, css_class, remark)
VALUES
  (1, '处理中', '10', 'court_case_status', 0, 'warning', '', '案件处理中'),
  (2, '已归档', '20', 'court_case_status', 0, 'success', '', '案件已归档');

-- ==================== 菜单与权限 ====================
DELETE rm
FROM system_role_menu rm
INNER JOIN system_menu m ON rm.menu_id = m.id
WHERE m.permission LIKE 'court-case:%';

DELETE FROM system_menu WHERE permission LIKE 'court-case:%';
DELETE FROM system_menu WHERE path IN ('/court-case', 'court-case', 'court-case/case', 'court-case/workbench', 'court-case/model');

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status, visible, keep_alive, always_show, component_name
)
VALUES (
    '案件中心', '', 1, 90, 0,
    '/court-case', 'ep:briefcase', '', 0, 1, 0, 1, ''
);

SELECT @court_case_root_id := LAST_INSERT_ID();

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status, visible, keep_alive, always_show, component_name
)
VALUES (
    '案件管理', '', 2, 1, @court_case_root_id,
    'case', 'ep:files', 'courtCase/case/index', 0, 1, 1, 0, 'CourtCaseList'
);

SELECT @court_case_case_menu_id := LAST_INSERT_ID();

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
VALUES
    ('案件查询', 'court-case:case:query', 3, 1, @court_case_case_menu_id, '', '', '', 0),
    ('案件创建', 'court-case:case:create', 3, 2, @court_case_case_menu_id, '', '', '', 0),
    ('案件推进', 'court-case:case:advance', 3, 3, @court_case_case_menu_id, '', '', '', 0),
    ('流转日志查询', 'court-case:log:query', 3, 4, @court_case_case_menu_id, '', '', '', 0);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status, visible, keep_alive, always_show, component_name
)
VALUES (
    '案件模型', '', 2, 2, @court_case_root_id,
    'model', 'ep:setting', 'courtCase/model/index', 0, 1, 1, 0, 'CourtCaseModelConfig'
);

SELECT @court_case_model_menu_id := LAST_INSERT_ID();

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
VALUES
    ('案件模型查询', 'court-case:model:query', 3, 1, @court_case_model_menu_id, '', '', '', 0),
    ('案件模型编辑', 'court-case:model:update', 3, 2, @court_case_model_menu_id, '', '', '', 0),
    ('案件模型发布', 'court-case:model:publish', 3, 3, @court_case_model_menu_id, '', '', '', 0);

INSERT INTO system_menu (
    name, permission, type, sort, parent_id,
    path, icon, component, status, visible, keep_alive, always_show, component_name
)
VALUES (
    '部门工作台', '', 2, 3, @court_case_root_id,
    'workbench', 'ep:monitor', 'courtCase/workbench/index', 0, 1, 1, 0, 'CourtCaseWorkbench'
);

SELECT @court_case_workbench_menu_id := LAST_INSERT_ID();

INSERT INTO system_menu (name, permission, type, sort, parent_id, path, icon, component, status)
VALUES
    ('工作台查询', 'court-case:case:query', 3, 1, @court_case_workbench_menu_id, '', '', '', 0),
    ('工作台推进', 'court-case:case:advance', 3, 2, @court_case_workbench_menu_id, '', '', '', 0);

INSERT IGNORE INTO system_role_menu (role_id, menu_id)
SELECT 1, id
FROM system_menu
WHERE id = @court_case_root_id
   OR parent_id = @court_case_root_id
   OR permission LIKE 'court-case:%';
