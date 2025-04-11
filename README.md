# 选择题刷题器

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
    maven clean install -X
```

- 运行编译好的 .jar 包：
```bash
    java -jar [.jar file built by maven]
```

### 访问
将服务器部署好后（假设部署位置是 `localhost`，端口为 `8081`），
可以访问以下 URL（仅列出所有以视图作为响应的 GET 请求方法）：

- 首页
  - https://localhost:8081/
- 获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容
  - https://localhost:8081/all_questions
- 获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容
  - https://localhost:8081/all_questions_with_correct_option
- 进行所有选择题的练习
  - https://localhost:8081/practise
- 显示所有练习成绩记录
  - https://localhost:8081/all_score_record
- 跳转至成绩结算页面（如果是手动跳转的话就显示最新的一次成绩）
  - https://localhost:8081/current_score_settlement

#### Date：2025.04.10
#### [Apache LICENCE-2.0](https://www.apache.org/licenses/LICENSE-2.0)