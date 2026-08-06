# 幻语AI (HuanyuAI Chat)

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

## 📲 安装

- 最低系统：Android 5.0 (API 21)
- 目标系统：Android 9 (API 28)
- 直接安装 `app/build/outputs/apk/release/` 下构建出的 APK，或从 [Releases](https://github.com/huanyu-ai/huanyu/releases) 下载

## 🛠️ 构建

需要 JDK 17 与 Android SDK。

```bash
# 1. 在 local.properties 中配置 SDK 路径
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# 2. 构建 Debug APK
./gradlew assembleDebug

# 3. 构建 Release APK
./gradlew assembleRelease
```

## 📁 项目结构

```
app/src/main/
├── AndroidManifest.xml          # 应用清单
├── java/com/huanyuai/chat/
│   └── MainActivity.java        # 唯一 Activity：WebView 宿主 + AppBridge 原生桥
├── res/                         # 资源（图标、字符串）
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

## 📜 许可证

本项目以 [MIT License](LICENSE) 开源。

## ⚠️ 免责声明

- 本项目仅用于学习与技术交流。
- 使用第三方 AI 服务（OpenAI、DeepSeek、Moonshot、通义等）时请遵守相应平台的服务条款。
- 请勿将本项目用于任何违法用途。
