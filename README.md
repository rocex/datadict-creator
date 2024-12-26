# Data Dict Creator

根据数据库中元数据生成 NC Cloud 数据字典

可修改 data/settings.properties 和 settings-${version}.properties 配置文件，

```ini
# 数据字典对应的产品版本
$DataDictVersion = NC Cloud 2312

# 生成的数据字典文件输出目录
$WorkDir = C:/datadict/

# 数据库连接信息
$jdbc.driver = oracle.jdbc.OracleDriver
$jdbc.url = jdbc:oracle:thin:@ip:port:orcl
$jdbc.user = 
$jdbc.password = 
```

合并SQLite数据库

```
.open 'mydb.db'

attach 'mydb2.db' as db2;

insert into main.table1(col1,col2) select col1,col2 from db2.table1;

detach database 'db2'

.exit
```

```SQL

insert into main.md_module(id,ddc_version,display_name,help,model_type,name,parent_module_id,ts,version_type) select id,ddc_version,display_name,help,model_type,name,parent_module_id,ts,version_type from db2.md_module;
insert into main.md_component(id,biz_model,ddc_version,display_name,help,model_type,name,namespace,own_module,ts,version,version_type) select id,biz_model,ddc_version,display_name,help,model_type,name,namespace,own_module,ts,version,version_type from db2.md_component;
insert into main.md_class(id,accessor_classname,authen,biz_itf_imp_classname,biz_object_id,class_type,component_id,ddc_version,display_name,full_classname,help,key_attribute,main_class_id,model_type,name,own_module,primary_class,ref_model_name,return_type,table_name,ts,version_type) select id,accessor_classname,authen,biz_itf_imp_classname,biz_object_id,class_type,component_id,ddc_version,display_name,full_classname,help,key_attribute,main_class_id,model_type,name,own_module,primary_class,ref_model_name,return_type,table_name,ts,version_type from db2.md_class;
insert into main.md_property(id,accessor_classname,access_power,access_power_group,attr_length,attr_max_value,attr_min_value,attr_sequence,calculation,class_id,column_code,custom_attr,data_type,data_type_sql,data_type_style,ddc_version,default_value,display_name,dynamic_attr,dynamic_table,fixed_length,help,hidden,key_prop,name,not_serialize,nullable,precise,read_only,ref_model_name,ts,version_type) select id,accessor_classname,access_power,access_power_group,attr_length,attr_max_value,attr_min_value,attr_sequence,calculation,class_id,column_code,custom_attr,data_type,data_type_sql,data_type_style,ddc_version,default_value,display_name,dynamic_attr,dynamic_table,fixed_length,help,hidden,key_prop,name,not_serialize,nullable,precise,read_only,ref_model_name,ts,version_type from db2.md_property;
insert into main.md_enumvalue(id,class_id,ddc_version,enum_sequence,enum_value,name,ts,version_type) select id,class_id,ddc_version,enum_sequence,enum_value,name,ts,version_type from db2.md_enumvalue;

insert into main.ddc_dict_json(id,class_id,ddc_version,dict_json,display_name,name,ts) select id,class_id,ddc_version,dict_json,display_name,name,ts from db2.ddc_dict_json;

```
