# 幻语AI (HuanyuAI Chat)

<p align="center">
  <a href="https://github.com/19923421354/huanyuai-chat/releases"><img src="https://img.shields.io/github/v/release/19923421354/huanyuai-chat?color=brightgreen&label=最新版本" alt="Release"></a>
  <a href="https://github.com/19923421354/huanyuai-chat/stargazers"><img src="https://img.shields.io/github/stars/19923421354/huanyuai-chat?style=social" alt="Stars"></a>
  <a href="https://github.com/19923421354/huanyuai-chat/network"><img src="https://img.shields.io/github/forks/19923421354/huanyuai-chat?style=social" alt="Forks"></a>
  <a href="https://github.com/19923421354/huanyuai-chat/releases/download/v4.0.0/huanyuai-chat-v4.0.0.apk"><img src="https://img.shields.io/badge/下载-APK%20v4.0.0-8b6cff" alt="下载APK"></a>
  <a href="https://img.shields.io/github/downloads/19923421354/huanyuai-chat/total"><img src="https://img.shields.io/github/downloads/19923421354/huanyuai-chat/total?color=orange&label=下载量" alt="Downloads"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/19923421354/huanyuai-chat" alt="License"></a>
</p>

一个基于 Android WebView 的 AI 聊天客户端。应用使用本地 HTML/JS 作为前端界面，通过 `JavascriptInterface` 桥接（`AppBridge`）调用 Android 原生能力（文件读写、剪贴板、分享、模型下载、本地数据加密存储等）。

> 本仓库为 [幻语AI / HuanyuAI] 客户端的开源版本，欢迎 Star & Fork！

## ✨ 功能特性

- 🧩 **纯 WebView 架构**：前端完全由本地 `assets/user_client.html` 驱动，UI 更新无需发版
- 🤖 **多 AI 角色对话**：内置多种 AI 角色与趣味小游戏（数独、五子棋、节奏大师、咖啡屋……）
- 🎨 **原生能力桥接**（`AppBridge`）：
  - 文件选择 / 保存 / 读取 / 分享
  - 剪贴板复制、Toast 提示、打开浏览器
  - 模型文件下载（支持 GGUF / safetensors）
  - localStorage 加密备份与恢复
  - 跨应用共享数据（AES 加密 + GZIP）
- 📦 **本地大模型支持**：可下载 Qwen、TinyLlama、Phi-2 等 GGUF 模型到设备端使用
- 🔐 **数据加密**：共享数据使用 AES/CBC + GZIP 加密存储
- 💖 **赞助开发者**：应用内「我的」页面提供微信/支付宝赞赏入口，支持开源作者持续维护
- 🔄 **检查更新**：应用内可联网检测 GitHub 最新版本并提示升级
- 📋 **更新日志**：应用内可查看各版本更新内容

## 🆕 v4.0.0 更新内容

- **修复所有已知闪退问题**：优化 WebView 初始化流程，添加异常捕获与错误处理
- **升级目标 SDK 到 34**：更安全、更稳定，兼容 Android 14
- **新增加载进度条**：页面加载状态一目了然
- **新增页面加载错误提示**：加载失败时显示友好提示
- **新增文件分享功能**：使用 FileProvider 安全分享文件
- **新增网络状态检测**：JavaScript 可获取网络连接状态
- **新增电量信息获取**：JavaScript 可获取设备电量
- **优化存储权限管理**：Android 11+ 自动适配 Scoped Storage
- **优化 Activity 生命周期**：防止内存泄漏
- **优化 WebView 配置**：支持更多内容类型
- **升级构建系统**：Gradle 8.14.4 + AGP 8.13.2，构建更快更稳定

## 📲 下载安装

> ⬇️ **直接下载 APK：** [幻语AI v4.0.0 APK（约 4 MB）](https://github.com/19923421354/huanyuai-chat/releases/download/v4.0.0/huanyuai-chat-v4.0.0.apk)

- 最低系统：Android 5.0 (API 21)
- 目标系统：Android 14 (API 34)
- 下载后直接安装即可（需允许安装未知来源应用）
- 所有历史版本见 [Releases 页面](https://github.com/19923421354/huanyuai-chat/releases)
- 也可以自行构建：`./gradlew assembleRelease`

## 🛠️ 构建

需要 JDK 17+ 与 Android SDK。

```bash
# 1. 配置 SDK 路径
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 2. 构建 Debug APK
./gradlew assembleDebug

# 3. 构建 Release APK（需要签名密钥）
./gradlew assembleRelease
```

## 📁 项目结构

```
app/src/main/
├── AndroidManifest.xml          # 应用清单
├── java/com/huanyuai/chat/
│   └── MainActivity.java        # 唯一 Activity：WebView 宿主 + AppBridge 原生桥
├── res/                         # 资源（图标、字符串、主题）
└── assets/
    ├── user_client.html         # 主界面（前端全部逻辑）
    ├── index.html               # 备用入口页
    ├── config/                  # 应用配置 / 版本信息
    ├── data/                    # 模型注册表、示例数据
    └── images/                  # 图片资源
```

## 🔌 AppBridge API 一览

| 方法 | 说明 |
| --- | --- |
| `getVersion()` | 获取客户端版本 |
| `getDeviceInfo()` | 获取设备信息 |
| `showToast(msg)` | 显示 Toast |
| `copyToClipboard(text)` | 复制到剪贴板 |
| `pickFile(callback)` | 选择文件（回调路径） |
| `saveFile(name, content)` | 保存文件到下载目录 |
| `readFile(path)` | 读取文件内容 |
| `shareFile(path)` | 分享文件 |
| `openBrowser(url)` | 打开外部浏览器 |
| `downloadModel(url, name)` | 后台下载模型文件 |
| `readSharedData()` / `writeSharedData(json)` | 读取/写入共享数据（AES 加密） |
| `backupLocalStorage(json)` / `restoreLocalStorage()` | localStorage 加密备份/恢复 |
| `getModelDir()` / `getDataDir()` / `getCacheDir()` | 获取目录路径 |
| `isNetworkAvailable()` | 检测网络是否可用 |
| `getBatteryLevel()` | 获取设备电量百分比 |

## 💖 赞助开发者

如果这个项目对你有帮助，欢迎**请开发者喝杯咖啡** ☕️～ 你的每一份支持都是持续更新的动力！

<div align="center">

| 微信赞赏 | 支付宝 |
| :---: | :---: |
| <img src="docs/sponsor/wechat-pay-qr.png" width="240" alt="微信赞赏码"/> | <img src="docs/sponsor/alipay-qr.jpg" width="240" alt="支付宝收款码"/> |

</div>

> 📱 使用微信 / 支付宝 **扫一扫** 或 **长按识别** 二维码即可赞助。
> 感谢每一位支持者！你的名字会出现在贡献者名单中 🙏

## 📜 许可证

本项目以 [MIT License](LICENSE) 开源。

## ⚠️ 免责声明

- 本项目仅用于学习与技术交流。
- 使用第三方 AI 服务（OpenAI、DeepSeek、Moonshot、通义等）时请遵守相应平台的服务条款。
- 请勿将本项目用于任何违法用途。