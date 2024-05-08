// 查找schema对应的根领域
select distinct b.domain_code root,c.default_schema
from iuap_yms_console.yms_domain_group a
left join iuap_yms_console.yms_domain_group b on b.parent_domain_id='000' and b.path=substring(a.path,1,length(b.path))
left join iuap_yms_console.yms_ds_logic c on a.domain_code=c.product_code
where c.default_schema is not null group by b.domain_code,c.default_schema order by c.default_schema,root;

