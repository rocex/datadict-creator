# Data Dict Creator

根据数据库中元数据生成nc数据字典

可修改 settings/settings.properties 配置文件，其中 ${version} 替换成相应的版本

```ini
# 数据字典对应的产品版本
${version}.DataDictVersion = NC Cloud ${version}

# 生成的html文件输出目录
${version}.OutputDir = C:/datadict/datadict-${version}/

# 数据库连接信息
${version}.jdbc.driver = oracle.jdbc.OracleDriver
${version}.jdbc.url = jdbc:oracle:thin:@ip:port:orcl
${version}.jdbc.user = 
${version}.jdbc.password = 
```
