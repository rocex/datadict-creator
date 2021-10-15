# Data Dict Creator

根据数据库中元数据生成 NC Cloud 数据字典

可修改 settings/settings.properties 配置文件，其中 ${version} 替换成相应的版本

```ini
# 数据字典对应的产品版本
${version}.DataDictVersion = NC Cloud ${version}

# 生成数据字典格式，取值：html/json，都是静态页面，所不同的是json的会通过js动态替换字典内容，html完全静态内容
# html 通过frameset布局，每个实体都是一个html静态页面，会有很多文件，调整格式很麻烦
# json 通过div布局，每个实体都是一个json数据文件，前端只有一个模板文件
generateType = json

# 生成的html文件输出目录
${version}.OutputDir = C:/datadict/datadict-${version}/

# 数据库连接信息
${version}.jdbc.driver = oracle.jdbc.OracleDriver
${version}.jdbc.url = jdbc:oracle:thin:@ip:port:orcl
${version}.jdbc.user = 
${version}.jdbc.password = 
```
