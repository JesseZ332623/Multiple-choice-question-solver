# 选择题刷题器

作为自己 Spring 项目的练习，目前已经掌握 Spring 中的核心部分。

## 如何使用？

### 部署
- 修改 `/resources/application.properties` 文件下的以下两个属性：
    - `spring.datasource.username=your_username`
    - `spring.datasource.password=your_password`

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
可以访问以下 URL（仅列出所有 GET 请求方法）：

- GET 请求方法
  - 获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容
    - http://localhost:8081/all_questions
  - 获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容
    - http://localhost:8081/all_questions_with_correct_option
  - 进行所有选择题的练习
    - http://localhost:8081/practise
  - 显示所有练习成绩记录
    - http://localhost:8081/all_score_record
  - 获取所有问题的内容，正确选项，答对次数以及它的所有选项的内容， 服务器以 JSON 格式作为响应
    - http://localhost:8081/api/question/all_questions
  - GET 方法请求，获取所有问题的内容，正确答案，答对的次数以及对应正确选项的内容。，服务器以 JSON 的格式作为响应
    - http://localhost:8081/api/question/all_questions_with_correct_option

#### Date：2025.4.8
#### [Apache LICENCE-2.0](https://www.apache.org/licenses/LICENSE-2.0)