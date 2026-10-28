# 禾木商城管理系统

前后端分离的学习项目：Vue 3 商城前台与管理后台，Spring Boot 接口，MySQL 保存业务数据。

## 技术版本

这些版本按当前电脑上已经安装的软件选定：

| 组件 | 版本 |
| --- | --- |
| JDK | 21.0.2（`JAVA_HOME`） |
| Maven | 3.9.11 |
| Spring Boot | 4.1.1 |
| MyBatis-Plus | 3.5.17（`mybatis-plus-spring-boot4-starter`） |
| SpringDoc | 3.1.1 |
| Node.js | 24.18.0 |
| npm | 11.16.0 |
| Vue | 3.5.44 |
| Vite | 8.3.4 |
| Element Plus | 2.14.7 |
| MySQL | 8.4.3 |

本机 `java` 命令指向 JDK 25，Maven 使用的是 `JAVA_HOME` 里的 JDK 21。后端按 JDK 21 编译。

## 目录

```text
frontend/    Vue 商城前台和管理后台
backend/     Spring Boot 接口
database/    建表脚本和初始化数据
docs/        数据库设计、接口说明、测试记录
scripts/     初始化数据库和启动后端的脚本
```

## 从零启动

### 1. 启动 MySQL

本机服务名是 `MySQL84`。如果服务没有运行，用管理员 PowerShell 执行：

```powershell
Start-Service MySQL84
```

当前开发环境用普通权限启动这个服务会被拒绝，需要你在管理员终端里启动。

### 2. 创建数据库

在项目根目录执行。`-p` 后面不要写密码，执行后会提示输入：

```powershell
& "E:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p --default-character-set=utf8mb4 -e "source C:/Users/dell/mall-management-system/database/schema.sql"
& "E:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p --default-character-set=utf8mb4 mall_db -e "source C:/Users/dell/mall-management-system/database/seed.sql"
```

也可以运行 `scripts/init-db.ps1`，它会提示输入两次 MySQL 密码。

### 3. 配置后端环境变量

```powershell
Copy-Item backend\.env.example backend\.env
```

编辑 `backend/.env`：

- `DB_PASSWORD` 改成你的 MySQL 密码。
- `JWT_SECRET` 改成至少 32 个字符的随机字符串。

`backend/.env` 已被 Git 忽略，不要把真实密码提交到仓库。

### 4. 启动 Java 后端

如果 `mvn` 不在 PATH 中，可以使用已经下载的 Maven：

```powershell
$env:Path = "C:\Users\dell\tools\apache-maven-3.9.11\bin;" + $env:Path
powershell -File scripts\run-backend.ps1
```

启动成功后访问：

- 接口文档：http://localhost:8080/swagger-ui.html
- OpenAPI JSON：http://localhost:8080/v3/api-docs

### 5. 安装并启动 Vue 前端

```powershell
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173 。开发服务器会把 `/api` 代理到 `http://localhost:8080`。

生产构建：

```powershell
cd frontend
npm run build
```

### 6. 访问商城和管理后台

- 商城首页：http://localhost:5173/
- 用户登录：http://localhost:5173/login
- 管理后台：http://localhost:5173/admin/login

## 开发环境测试账号

这些账号由 `database/seed.sql` 写入，密码以 BCrypt 哈希保存。只用于本地学习，生产环境不得沿用。

| 用户名 | 密码 | 角色 | 说明 |
| --- | --- | --- | --- |
| admin | Admin@123456 | 管理员 | 进入管理后台 |
| linxiaoyu | User@123456 | 普通用户 | 已有地址、购物车和订单 |
| zhouhang | User@123456 | 普通用户 | 用于验证不能查看他人订单 |
| wangmin | User@123456 | 普通用户 | 账号已禁用，不能登录 |

也可以用邮箱登录，例如 `admin@mall.local`。

## 认证说明

采用无状态 JWT，不使用服务端 Session。

- 登录成功后，前端把令牌放在 `sessionStorage`，请求头为 `Authorization: Bearer <token>`。
- 令牌默认 120 分钟过期，时间由 `JWT_EXPIRATION_MINUTES` 控制。
- 令牌里包含用户编号、角色和 `tokenVersion`。
- 退出登录或修改密码时，数据库里的 `token_version` 加 1，旧令牌立刻失效。
- 关闭浏览器标签页后，`sessionStorage` 中的令牌会被清除。
- 角色权限由 Spring Security 在后端校验。前端隐藏菜单不能代替权限控制。
- 注册接口不接收角色字段，用户不能把自己改成管理员。

## 销售额口径

销售额和有效订单只统计状态为待发货、已发货、已完成的订单。已取消订单不计入。本项目没有接入支付网关，下单成功即视为已支付并扣减库存。

## 常见问题

**后端提示请设置 JWT_SECRET**

`backend/.env` 不存在，或 `JWT_SECRET` 少于 32 个字符。按上面的步骤复制并修改。

**数据库连接失败**

确认 MySQL 服务已启动，数据库名是 `mall_db`，用户名和密码与 `.env` 一致。

**端口被占用**

修改 `.env` 里的 `SERVER_PORT`，同时修改 `frontend/vite.config.js` 里的代理端口。

**普通用户打开后台被拒绝**

这是预期行为。后台路由和 `/api/admin/**` 都要求管理员角色。

**mvn 命令找不到**

本机 Maven 位于 `C:\Users\dell\tools\apache-maven-3.9.11`。把 `bin` 目录加入当前终端的 PATH，或直接运行 `scripts/run-backend.ps1`。
