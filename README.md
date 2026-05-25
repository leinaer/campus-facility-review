# 校园生活圈 - Campus Evaluation System

一个前后端分离的微信小程序项目，包含管理后台和后端服务。

## 项目结构

```
campus/
├── campus-evaluation/        # Spring Boot 后端服务
├── campus-admin/             # Vue 3 管理后台
├── campus-evaluation-mp/     # 微信小程序前端
└── sql/                      # 数据库初始化脚本
```

## 技术栈

### 后端 (campus-evaluation)

- Java 17
- Spring Boot 3.5.3
- MyBatis Plus 3.5.5
- MySQL 8.0
- Redis
- JWT (jjwt 0.12.5)
- 微信小程序 SDK (weixin-java-miniapp 4.4.0)
- EasyExcel 3.3.2

### 前端管理后台 (campus-admin)

- Vue 3.5
- Vite 8.0
- Element Plus 2.13
- Pinia 3.0
- Vue Router 5.0
- Axios 1.15
- ECharts 6.0

### 微信小程序 (campus-evaluation-mp)

- 微信小程序原生框架

---

## 环境要求

在开始之前，请确保你的系统已安装以下软件：

| 软件           | 版本要求 | 下载地址                                                     |
| -------------- | -------- | ------------------------------------------------------------ |
| JDK            | 17+      | [Oracle](https://www.oracle.com/java/technologies/downloads/) 或 [OpenJDK](https://openjdk.org/) |
| Node.js        | 18+      | [Node.js 官网](https://nodejs.org/)                          |
| MySQL          | 8.0+     | [MySQL 官网](https://dev.mysql.com/downloads/)               |
| Redis          | 6.0+     | [Redis 官网](https://redis.io/download)                      |
| Maven          | 3.6+     | [Maven 官网](https://maven.apache.org/download.cgi)          |
| 微信开发者工具 | 最新版   | [微信官方](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html) |

---

## 快速开始

### 第一步：数据库准备

1. 创建数据库

```sql
CREATE DATABASE campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
```

2. 导入初始化脚本

```bash
mysql -u root -p campus < sql/init.sql
```

或使用 Navicat、DataGrip 等工具导入 `sql/init.sql` 文件。

---

### 第二步：配置并启动后端

#### 1. 配置环境变量

进入后端目录：

```bash
cd campus-evaluation
```

复制环境变量模板：

```bash
cp .env.example .env
```

编辑 `.env` 文件，填入你的配置：

```env
# 数据库配置
DB_URL=jdbc:mysql://localhost:3306/campus?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT%2B8
DB_USERNAME=root
DB_PASSWORD=你的数据库密码

# Redis 配置
REDIS_HOST=localhost
REDIS_PORT=6379

# 微信小程序配置
WECHAT_APPID=你的小程序AppID
WECHAT_SECRET=你的小程序Secret

# 文件上传路径
FILE_UPLOAD_PATH=D:/campus-uploads
```

> **注意**：`.env` 文件不会被 Git 跟踪，请妥善保管你的密钥。

#### 2. 启动 Redis

确保 Redis 服务正在运行：

```bash
# Windows (如果安装了 Redis)
redis-server

# Linux/Mac
redis-server
```

#### 3. 编译并运行后端

```bash
# 使用 Maven Wrapper (推荐)
mvnw spring-boot:run

# 或使用系统 Maven
mvn spring-boot:run
```

首次运行会自动下载依赖，可能需要几分钟。

看到以下输出表示启动成功：

```
Started CampusEvaluationApplication in X.XXX seconds
```

后端服务默认运行在 `http://localhost:8080`

---

### 第三步：配置并启动管理后台

#### 1. 安装依赖

打开新终端，进入前端目录：

```bash
cd campus-admin
npm install
```

#### 2. 配置环境变量（可选）

如果需要自定义 API 地址，复制环境变量模板：

```bash
cp .env.example .env.local
```

编辑 `.env.local`：

```env
VITE_API_BASE_URL=http://localhost:8080
```

#### 3. 启动开发服务器

```bash
npm run dev
```

启动成功后，浏览器访问 `http://localhost:5173`

---

### 第四步：配置微信小程序

#### 1. 获取微信小程序 AppID

1. 登录 [微信公众平台](https://mp.weixin.qq.com/)
2. 进入「开发」->「开发管理」->「开发设置」
3. 复制你的 AppID

#### 2. 配置小程序

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 打开微信开发者工具
3. 选择「导入项目」
4. 项目目录选择 `campus-evaluation-mp`
5. AppID 填入你从公众平台获取的 AppID
6. 点击「导入」

#### 3. 修改后端接口地址

在小程序代码中，找到 API 请求的基础 URL（通常在 `utils/request.js` 或类似文件中），将其修改为你的后端地址：

```javascript
// 开发环境
const BASE_URL = 'http://localhost:8080'

// 生产环境需要改为你的服务器地址
```

> **注意**：微信小程序要求使用 HTTPS，本地调试时需在开发者工具中开启「不校验合法域名」选项。

---

## 配置说明

### 后端配置项

| 配置项             | 说明              | 默认值                               |
| ------------------ | ----------------- | ------------------------------------ |
| `DB_URL`           | 数据库连接 URL    | `jdbc:mysql://localhost:3306/campus` |
| `DB_USERNAME`      | 数据库用户名      | `root`                               |
| `DB_PASSWORD`      | 数据库密码        | 必填                                 |
| `REDIS_HOST`       | Redis 主机地址    | `localhost`                          |
| `REDIS_PORT`       | Redis 端口        | `6379`                               |
| `WECHAT_APPID`     | 微信小程序 AppID  | 必填                                 |
| `WECHAT_SECRET`    | 微信小程序 Secret | 必填                                 |
| `FILE_UPLOAD_PATH` | 文件上传存储路径  | `D:/campus-uploads`                  |

### 前端配置项

| 配置项              | 说明          | 默认值                  |
| ------------------- | ------------- | ----------------------- |
| `VITE_API_BASE_URL` | 后端 API 地址 | `http://localhost:8080` |

---

## 常见问题

### 1. 后端启动失败：端口被占用

**错误信息**：`Port 8080 was already in use`

**解决方案**：

- 修改 `application.yml` 中的 `server.port`
- 或关闭占用 8080 端口的程序

```bash
# Windows 查看占用端口的进程
netstat -ano | findstr :8080

# 杀死进程
taskkill /F /PID <PID>
```

### 2. 数据库连接失败

**检查项**：

- MySQL 服务是否启动
- 数据库名、用户名、密码是否正确
- 数据库 `campus` 是否已创建
- SQL 脚本是否已成功导入

### 3. Redis 连接失败

**检查项**：

- Redis 服务是否启动
- Redis 端口是否正确（默认 6379）

```bash
# 测试 Redis 连接
redis-cli ping
# 应返回 PONG
```

### 4. 微信小程序无法调用后端 API

**解决方案**：

- 确保后端已启动且可访问
- 在微信开发者工具中开启「不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书」
- 检查小程序代码中的 API 地址是否正确

### 5. 文件上传失败

**检查项**：

- `FILE_UPLOAD_PATH` 配置的目录是否存在
- 目录是否有写入权限
- 上传文件大小是否超过限制（默认 5MB）

---

## 生产部署

### 后端打包

```bash
cd campus-evaluation
mvn clean package -DskipTests
```

生成的 JAR 文件位于 `target/campus-evaluation-0.0.1-SNAPSHOT.jar`

### 后端运行

```bash
# 设置环境变量
export DB_PASSWORD=your_password
export WECHAT_SECRET=your_secret

# 运行
java -jar target/campus-evaluation-0.0.1-SNAPSHOT.jar
```

### 前端构建

```bash
cd campus-admin
npm run build
```

生成的静态文件位于 `dist/` 目录，可部署到 Nginx、Apache 或其他静态文件服务器。

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 管理后台
    location / {
        root /path/to/campus-admin/dist;
        try_files $uri $uri/ /index.html;
    }

    # 后端 API 代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 上传文件访问
    location /uploads {
        alias /path/to/campus-uploads;
    }
}
```

---


## 项目功能

- 用户认证（微信登录 + JWT）
- 设施评价管理
- 活动管理
- 帖子发布与管理
- Banner 轮播图管理
- 数据统计与分析
- 文件上传（图片等）

---

## 许可证

本项目仅供学习交流使用。

---

## 联系方式

如有问题，请提 Issue 或联系项目维护者。
