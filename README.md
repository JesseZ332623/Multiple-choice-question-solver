# 选择题刷题器

## 文档

### 应用所有端点树型图

- 详见：
  [应用所有端点树型图](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/documents/%E5%BA%94%E7%94%A8%E6%89%80%E6%9C%89%E7%AB%AF%E7%82%B9%E6%A0%91%E5%9E%8B%E5%9B%BE.svg)

### 应用模块描述

- 详见：[应用模块描述](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/documents/%E5%BA%94%E7%94%A8%E6%A8%A1%E5%9D%97%E6%8F%8F%E8%BF%B0.md)

### 应用数据 E-R 图

- 详见：[E-R 图](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/documents/%E9%A1%B9%E7%9B%AE%20E-R%20%E5%9B%BE.png)

## 如何使用？

### 部署

- 从 release 处下载编译好的 .jar 包并运行：

  ```bash
  java -jar [your-jar-path/examination-version-SNAPSHOT.jar]
  ```

### 访问

将服务器部署好后（假设部署位置是 `localhost`，端口为 `8081`），
可以访问以下 URL（仅列出部分主要的、以视图作为响应的 GET 请求方法）：

- 用户注册页面
  - <https://localhost:8081/user_info/register>
  ![image](https://github.com/user-attachments/assets/a569dbf7-d3d0-40b4-9670-55c239f38497)

- 用户登录页面
  - <https://localhost:8081/user_info/login>
  ![image](https://github.com/user-attachments/assets/96a61394-043e-4685-acfb-82d4f0365c1e)

- 普通用户修改账号页面
  - <https://localhost:8081/user_info/modify>
  ![image](https://github.com/user-attachments/assets/d0c4ebaa-2827-4a75-a82f-14a6be657505)

- 普通用户首页
  - <https://localhost:8081/user_info/user_front_page>
  ![image](https://github.com/user-attachments/assets/38449497-4205-4034-811d-ed65d2ceba95)

- 用户练习成绩展示页面
  - <https://localhost:8081/score_record/all_score_record>
  ![练习结果分页查询-1](https://github.com/user-attachments/assets/3b37b6a6-9e18-4df7-a073-9e611095c325)

- 管理员界面（当前只写了用户数据管理页面）
  - <https://localhost:8081/admin/all_users>
 ![image](https://github.com/user-attachments/assets/7452a86b-f69c-4aef-84bd-e3d81bb00dd6)

- Hibernate 查询统计页面
  - <https://localhost:8081/hibernate/query_statistics>
  ![image](https://github.com/user-attachments/assets/e46892bf-93c5-4d0f-b942-d9b8c3aad59a)

#### Date：2025.06.03

#### [Apache LICENCE-2.0](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/LICENSE)
