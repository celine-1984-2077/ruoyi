# Roy 项目说明

这是一个基于 ruoyi-vue-pro 的全栈项目，采用 Monorepo 方式管理前后端代码。

## 项目结构

```
roy/
├── docker-compose.yml          # Docker 编排配置（MySQL + Redis）
├── ruoyi-vue-pro/             # 后端项目（Spring Boot）
│   └── docs-site/             # 项目文档（本地定制）
└── yudao-ui-admin-vue3/       # 前端项目（Vue 3）
```

## 技术栈

**后端：**
- Spring Boot
- MyBatis Plus
- MySQL 8.0
- Redis 7

**前端：**
- Vue 3
- TypeScript
- Element Plus

## 仓库管理

本项目采用 **Monorepo** 方式管理，前后端代码都在一个仓库中：
- GitHub 仓库：https://github.com/Tony1120/ruoyi
- 包含完整的前后端源代码
- 可以直接在 GitHub 上查看和修改代码

### 上游仓库

- 后端上游：https://github.com/YunaiV/ruoyi-vue-pro
- 前端上游：https://github.com/yudaocode/yudao-ui-admin-vue3

## 同步上游更新

### 一次性配置（首次执行）

```bash
# 添加上游远程仓库
git remote add upstream-backend https://github.com/YunaiV/ruoyi-vue-pro.git
git remote add upstream-frontend https://github.com/yudaocode/yudao-ui-admin-vue3.git

# 验证配置
git remote -v
```

### 更新后端代码

```bash
# 1. 获取上游最新代码
git fetch upstream-backend master

# 2. 使用 subtree 合并更新
git subtree pull --prefix=ruoyi-vue-pro upstream-backend master --squash

# 3. 解决冲突（如果有）
# 编辑冲突文件，然后：
git add .
git commit -m "chore: 合并后端上游更新"

# 4. 推送到自己的仓库
git push origin master
```

### 更新前端代码

```bash
# 1. 获取上游最新代码
git fetch upstream-frontend master

# 2. 使用 subtree 合并更新
git subtree pull --prefix=yudao-ui-admin-vue3 upstream-frontend master --squash

# 3. 解决冲突（如果有）
git add .
git commit -m "chore: 合并前端上游更新"

# 4. 推送到自己的仓库
git push origin master
```

### 注意事项

⚠️ **合并前建议：**
- 提交或暂存当前所有改动
- 创建备份分支：`git checkout -b backup-before-merge`
- 查看上游更新内容：`git log upstream-backend/master --oneline -10`

⚠️ **如果合并出现冲突：**
- 手动编辑冲突文件
- 保留你的定制修改
- 测试后再提交

⚠️ **SQL 文件：**
- 项目的 SQL 文件已被 .gitignore 排除（避免 GitHub 密钥检测）
- 更新时注意手动同步数据库变更

## 本地开发

### 启动数据库服务

```bash
# 启动 MySQL 和 Redis
docker-compose up -d

# 查看状态
docker-compose ps

# 停止服务
docker-compose down
```

### 启动后端

```bash
cd ruoyi-vue-pro
# 根据项目文档配置数据库连接等
mvn spring-boot:run
```

### 启动前端

```bash
cd yudao-ui-admin-vue3
npm install
npm run dev
```

## 部署

### 云服务器部署

```bash
# 1. 克隆仓库
git clone https://github.com/Tony1120/ruoyi.git
cd ruoyi

# 2. 启动数据库服务
docker-compose up -d

# 3. 配置并启动后端
cd ruoyi-vue-pro
# 修改配置文件（数据库连接等）
# 启动应用

# 4. 配置并启动前端
cd ../yudao-ui-admin-vue3
npm install
npm run build
# 配置 Nginx 或其他 Web 服务器
```

## 项目定制

### 本地改动记录

- `ruoyi-vue-pro/docs-site/` - 项目文档
- 其他定制内容...

### 推送改动

```bash
git add .
git commit -m "描述你的修改"
git push origin master
```

## 常见问题

### Q: 如何查看上游有哪些更新？

```bash
git fetch upstream-backend master
git log HEAD..upstream-backend/master --oneline
```

### Q: 合并时出现大量冲突怎么办？

考虑使用以下策略：
- 优先保留你的改动：`git merge -X ours ...`
- 优先使用上游代码：`git merge -X theirs ...`
- 或者手动挑选需要的更新

### Q: 想回退到合并前的状态？

```bash
# 查看提交历史
git log --oneline

# 回退到指定提交
git reset --hard <commit-id>

# 强制推送（谨慎使用）
git push origin master --force
```

## 更多资源

- ruoyi-vue-pro 官方文档：https://doc.iocoder.cn/
- Vue 3 文档：https://vuejs.org/
- Spring Boot 文档：https://spring.io/projects/spring-boot
