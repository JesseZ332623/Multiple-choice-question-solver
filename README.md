# 水课选择题刷题器

作为自己 Spring 项目的练习，目前已经掌握 Spring 中的核心部分。

## 如何使用？

### 部署
- 修改 `/resources/application.properties` 文件下的以下两个属性：

```text
spring.datasource.username=your_username
spring.datasource.password=your_password
```

- 然后使用 Maven 进行构建：
```bash
    mvn clean install -X
```

- 运行编译好的 .jar 包：
```bash
    java -jar [.jar file built by maven]
```

### 应用所有端点树型图

### 访问
将服务器部署好后（假设部署位置是 `localhost`，端口为 `8081`），
可以访问以下 URL（仅列出部分主要的、以视图作为响应的 GET 请求方法）：

- 用户注册页面
  - https://localhost:8081/user_info/register 
  - ![image](https://github.com/user-attachments/assets/a569dbf7-d3d0-40b4-9670-55c239f38497)

- 用户登录页面
  - https://localhost:8081/user_info/login 
  - ![image](https://github.com/user-attachments/assets/4e3e2147-38a3-44ef-8453-ae3c6ca679ec)

- 普通用户修改账号页面
  - https://localhost:8081/user_info/modify 
  - ![image](https://github.com/user-attachments/assets/d0c4ebaa-2827-4a75-a82f-14a6be657505)
 
- 普通用户首页
  - https://localhost:8081/user_info/user_front_page
  - ![image](https://github.com/user-attachments/assets/3b2cde3a-17d4-470f-9bb9-5bacc391ad4a)

- 应用所有端点树型图详见：
[应用所有端点树型图](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/documents/%E5%BA%94%E7%94%A8%E6%89%80%E6%9C%89%E7%AB%AF%E7%82%B9%E6%A0%91%E5%9E%8B%E5%9B%BE.svg)

#### Date：2025.04.21
#### [Apache LICENCE-2.0](https://github.com/JesseZ332623/Multiple-choice-question-solver/blob/master/LICENSE)
