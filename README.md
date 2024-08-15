## 组件介绍
  openai-code-review 是一个基于openai的代码自动评审的组件，基于GitHub的actions 在每次向仓库中push代码时触发该组件，然后将评审结果通过企业微信通知指定的用户。
## 使用步骤
需要结合github使用，不支持其他类型的远程代码仓库。
1. 配置github仓库 secrets
2. 在工程中创建github工作流配置文件。

## 开发总结
目录结构
- **OpenAiCodeReview作为入口程序，在pom.xml文件中配置，打成jar包以后程序从该类的main方法开始执行。**
![[Pasted image 20240815225941.png]]

### 功能实现
- 使用java自带的ProcessBuilder，在java程序中创建和管理操作系统的进程，可以获取进程的输入输出流，换句话说可以在java程序中执行linux命令，从而可以拿到git仓库的代码变化。
- 对接第三方接口的流程
	1. 获取token
	2. 按照第三方接口文档构造消息实体，注意使用JSON工具把消息实体转成json字符串，**消息实体的属性需要添加get set方法**，转json会忽略对象的方法只转属性。
	3. 创建http连接，设置请求头，然后获取输出流把json数据写出。
	4. 获取输入流，读取接口返回的json数据。格式固定，使用while循环
	5. 把json数据转化响应消息实体。
- 使用Git对象操作git的仓库
- github 工作流，定义了在什么时机，执行什么操作（可以是一组shell命令）。在github/workflow/.yml文件下进行配置。
- 使用github 仓库的secrets 和环境变量 进行配置
- 环境变量（Environment Variables）是在操作系统中存储的值，用于配置和影响运行中的程序的行为。它们是键值对的形式，可以在操作系统的不同层次上设置和访问。环境变量在操作系统和应用程序之间提供了一种灵活的配置方式，无需修改程序代码即可调整其行为和运行环境。
- 
### 编码技巧
- 使用模板方法设计模式
- 面向接口编程，openai接口目前只给出了chatglm实现，可以添加新的实现类对接其他ai模型。
