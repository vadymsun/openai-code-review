# 组件介绍
  openai-code-review 是一个基于openai的代码自动评审的组件，基于GitHub的actions 在每次向仓库中push代码时触发该组件，然后将评审结果通过企业微信通知指定的用户。
# 使用步骤
需要结合github使用，不支持其他类型的远程代码仓库。
1. 配置github仓库 secrets
2. 在工程中创建github工作流配置文件。

