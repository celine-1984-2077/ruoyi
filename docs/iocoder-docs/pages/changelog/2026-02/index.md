# 【v2026-02】开发中

Source: https://doc.iocoder.cn/changelog/2026-02/

## 

### 📈 Statistic

- 总代码行数：
- 源码代码行数：
- 注释行数：
- 单元测试用例数：

### ⭐ New Features

- 【新增】《IoT 开发指南》，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献
- 【新增】动态表单新增 iframe 和省市区选择器组件，由 [@puhui999](https://gitee.com/puhui999)  贡献 [#861](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/861/) 、[#333](https://gitee.com/yudaocode/yudao-ui-admin-vben/pulls/333/) 、[#862](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/862) 、[#333](https://gitee.com/yudaocode/yudao-ui-admin-vben/pulls/333)
- 【优化】使用 static 块优化 IPUtils 和 AreaUtils 初始化逻辑，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献

Vben5.0 + antd 管理后台专区：

Vben5.0 + element-plus 管理后台专区：

Vue3 + element-plus 管理后台专区：

BPM 工作流专区：

AI 大模型专区：

MALL 商城专区：

Admin Uniapp 管理后台专区：

IoT 物联网专区：

- 【新增】接入 modbus 协议，重构所有协议的配置，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献 [#863](https://gitee.com/yudaocode/yudao-ui-admin-vue3/pulls/863/) 、[#1518](https://gitee.com/zhijiantianya/ruoyi-vue-pro/pulls/1518)

### 🐞 Bug Fixes

- 【修复】S3 文件名包含 + 号时预签名 URL 解码错误，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献

Vue3 + element-plus 管理后台专区：

Vben5.0 管理后台专区：

- 【修复】修复上传头像时，如果图片加载失败，弹框一直 loading 的问题，由 [@li\_shifeng](https://gitee.com/li_shifeng)  贡献 [#335]https://gitee.com/yudaocode/yudao-ui-admin-vben/pulls/335)

MALL 商城专区：

- 【修复】分销提现页银行名称存储问题，兼容管理端回显，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献 [#33](https://github.com/yudaocode/yudao-mall-uniapp/pull/33)
- 【修复】修复订单项价格计算逻辑，由 [@irongroup](https://gitee.com/irongroup)  贡献 [#243](https://gitee.com/zhijiantianya/yudao-cloud/pulls/243/)

Admin Uniapp 管理后台专区：

- 【修复】登录成功跳转逻辑，解决小程序端在有查询参数时因强制使用 reLaunch 模式导致无法返回的问题，由 [@quitelinxd](https://gitee.com/quitelinxd)  贡献 [#44](https://gitee.com/yudaocode/yudao-ui-admin-uniapp/pulls/44/)

IoT 物联网专区：

BPM 工作流专区：

- 【修复】串行多实例（按顺序依次审批）审批人顺序不确定，HashSet 未转为有序集合，由 [@芋道源码](https://gitee.com/zhijiantianya)  贡献
