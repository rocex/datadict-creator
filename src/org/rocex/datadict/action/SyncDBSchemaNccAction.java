package org.rocex.datadict.action;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import org.rocex.db.SQLExecutor;
import org.rocex.db.processor.MapProcessor;
import org.rocex.utils.Logger;

public class SyncDBSchemaNccAction extends SyncDBSchemaAction
{
    public SyncDBSchemaNccAction(String strVersion)
    {
        super(strVersion);
    }
    
    /***************************************************************************
     * 修复一些数据库数据问题
     * @author Rocex Wang
     * @since 2020-5-9 14:05:43
     ***************************************************************************/
    @Override
    public void afterSyncData()
    {
        Logger.getLogger().begin("after sync data");
        
        String[] strSQLs = { "update md_class set name='Memo' where id='BS000010000100001030' and name='MEMO'",
                "update md_class set name='MultiLangText' where id='BS000010000100001058' and name='MULTILANGTEXT'",
                "update md_class set name='Custom' where id in('BS000010000100001056','BS000010000100001059') and name='CUSTOM'",
                "update md_module set display_name='财务' where id='gl'",
                // "delete from md_module where id='FBM'",
                "update md_component set own_module='fbm' where own_module='FBM'",
                "update md_component set own_module='mmpac' where own_module='NC_MM_PAC6.31'",
                "update md_component set own_module='hryf' where own_module='HRYF'", "update md_component set own_module='ufob' where own_module='UFOB'",
                "update md_component set own_module='hrp' where own_module='hr_hrp'", "update md_component set own_module='uapbd' where own_module='UAP_BD'",
                "update md_component set own_module='uap' where own_module='ncwebpub'", "update md_component set own_module='nresa' where own_module='resa'",
                "insert into md_module(id,display_name,model_type,name,parent_module_id,ddc_version,version_type) values ('pca', 'pca','md', 'pca', 'mm','"
                        + strVersion + "',0)",
                "update md_module set parent_module_id='md_clazz' where model_type='md' and parent_module_id is null",
                
                "update md_module set parent_module_id='md__'||parent_module_id where model_type='md' and parent_module_id not in('md_clazz','db_table')" };
        
        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("after sync data");
    }
    
    @Override
    public void beforeSyncData()
    {
        String[] strModuleSQLs = {
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ae','2505','md',0,'数据处理','ae','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__am','2505','md',0,'资产管理','am','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__basedoc','2505','md',0,'基础档案','basedoc','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__bq','2505','md',0,'商业分析','bq','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__co','2505','md',0,'管理会计','co','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__epm','2505','md',0,'企业绩效','epm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__fi','2505','md',0,'财务管理','fi','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__hr','2505','md',0,'人力资源','hr','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ipm','2505','md',0,'投资管理','ipm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__lightapp','2505','md',0,'轻量化平台','lightapp','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__mm','2505','md',0,'生产制造','mm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__pm','2505','md',0,'项目管理','pm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ria','2505','md',0,'应用平台','ria','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scap','2505','md',0,'国资服务','scap','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scct','2505','md',0,'供应链控制塔','scct','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__scm','2505','md',0,'供应链','scm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ssc','2505','md',0,'共享服务','ssc','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__tm','2505','md',0,'财资管理','tm','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uap','2505','md',0,'UAP Server','uap','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__uapbd','2505','md',0,'基础数据','uapbd','md_clazz')",
                "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('md__ufofr','2505','md',0,'自由报表','ufofr','md_clazz')",
                
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__ae','2505','db',0,'数据处理','ae','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__am','2505','db',0,'资产管理','am','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__basedoc','2505','db',0,'基础档案','basedoc','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__bq','2505','db',0,'商业分析','bq','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__co','2505','db',0,'管理会计','co','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__epm','2505','db',0,'企业绩效','epm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__fi','2505','db',0,'财务管理','fi','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__hr','2505','db',0,'人力资源','hr','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__ipm','2505','db',0,'投资管理','ipm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__lightapp','2505','db',0,'轻量化平台','lightapp','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__mm','2505','db',0,'生产制造','mm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__pm','2505','db',0,'项目管理','pm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__ria','2505','db',0,'应用平台','ria','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__scap','2505','db',0,'国资服务','scap','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__scct','2505','db',0,'供应链控制塔','scct','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__scm','2505','db',0,'供应链','scm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__ssc','2505','db',0,'共享服务','ssc','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__tm','2505','db',0,'财资管理','tm','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values ('db__uap','2505','db',0,'UAP
                // Server','uap','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__uapbd','2505','db',0,'基础数据','uapbd','db_table')",
                // "insert into md_module (id,ddc_version,model_type,version_type,display_name,name,parent_module_id) values
                // ('db__ufofr','2505','db',0,'自由报表','ufofr','db_table')",
                
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__mm', '2505', 0, '生产制造', 'db', 'mm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__codefactory', '2505', 0, 'codefactory', 'db', 'codefactory', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__tm', '2505', 0, '财资管理', 'db', 'tm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__ncaam', '2505', 0, '应用资产管理', 'db', 'ncaam', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__scm', '2505', 0, '供应链', 'db', 'scm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__uapadp', '2505', 0, 'uapadp', 'db', 'uapadp', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__epm', '2505', 0, '企业绩效管理', 'db', 'epm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__fi', '2505', 0, '财务会计', 'db', 'fi', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__pm', '2505', 0, '项目管理', 'db', 'pm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__qc', '2505', 0, '质量管理', 'db', 'qc', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__danresa', '2505', 0, '利润中心主题', 'db', 'co', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__ria', '2505', 0, '应用平台', 'db', 'ria', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__basedoc', '2505', 0, '基础档案', 'db', 'basedoc', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__uapwb', '2505', 0, 'uapwb', 'db', 'uapwb', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__dafi', '2505', 0, '财务主题', 'db', 'dafi', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__sscia', '2505', 0, '智能审核', 'db', 'sscia', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__lightapp', '2505', 0, '公共产品', 'db', 'lightapp', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__opm', '2505', 0, 'opm', 'db', 'opm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__icl', '2505', 0, '智能结账', 'db', 'icl', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__scct', '2505', 0, '供应链控制塔', 'db', 'scct', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__ssc', '2505', 0, '共享服务', 'db', 'ssc', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__lcdp', '2505', 0, '应用工厂', 'db', 'lcdp', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__hr', '2505', 0, '人力资本', 'db', 'hr', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__atp', '2505', 0, '自动化任务平台', 'db', 'atp', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__am', '2505', 0, '资产管理', 'db', 'am', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__da', '2505', 0, '数据应用', 'db', 'da', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__dapub', '2505', 0, '数据应用基础', 'db', 'dapub', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__ipm', '2505', 0, '投资管理', 'db', 'ipm', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__co', '2505', 0, '管理会计', 'db', 'co', 'db_table')",
                "insert into md_module (id, ddc_version, version_type, display_name, model_type, name, parent_module_id) values ('db__scap', '2505', 0, '国资服务', 'db', 'scap', 'db_table')",
                
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmbd', '2505', 0, '生产制造基础档案', 'db', 'mmbd', 'db__uapbd')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__fip', '2505', 0, '会计平台', 'db', 'fip', 'db__uap')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__uapeai', '2505', 0, '数据交换平台', 'db', 'uapeai', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__dcpp', '2505', 0, '外系统数据采集', 'db', 'dcpp', 'db__epm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__tmmob', '2505', 0, '移动应用', 'db', 'tmmob', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ia', '2505', 0, '存货核算', 'db', 'ia', 'db__fi')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__pu', '2505', 0, '采购管理', 'db', 'pu', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmpps', '2505', 0, '生产计划公共', 'db', 'mmpps', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__imp', '2505', 0, '混合云（其他）', 'db', 'imp', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__eom', '2505', 0, '运行管理', 'db', 'eom', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__elm', '2505', 0, '润滑管理', 'db', 'elm', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ipmdm', '2505', 0, '决策模型', 'db', 'ipmdm', 'db__ipm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrhi', '2505', 0, '人员信息', 'db', 'hrhi', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrjf', '2505', 0, '组织机构管理', 'db', 'hrjf', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrsz', '2505', 0, '人力设置', 'db', 'hrsz', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__workbench', '2505', 0, '工作桌面', 'db', 'workbench', 'db__lightapp')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__platform', '2505', 0, 'web中间件', 'db', 'platform', 'db__lightapp')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__iaudit', '2505', 0, '标准接口', 'db', 'iaudit', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__fbm', '2505', 0, '商业汇票', 'db', 'fbm', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__gpmc', '2505', 0, '担保合同管理', 'db', 'gpmc', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ccc', '2505', 0, '授信管理', 'db', 'ccc', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__sp', '2505', 0, '结算平台', 'db', 'sp', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__cmp', '2505', 0, '现金管理', 'db', 'cmp', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__lcm', '2505', 0, '信用证管理', 'db', 'lcm', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__bond', '2505', 0, '发债管理', 'db', 'bond', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ifm', '2505', 0, '金融投资', 'db', 'ifm', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__fd', '2505', 0, '融资筹划', 'db', 'fd', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__tmpf', '2505', 0, 'tmpf', 'db', 'tmpf', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__sca', '2505', 0, '成本估算', 'db', 'sca', 'db__co')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmpub', '2505', 0, '生产制造公共', 'db', 'mmpub', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__bcsi', '2505', 0, '无线设备接口', 'db', 'bcsi', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__wmsi', '2505', 0, 'wms集成接口', 'db', 'wmsi', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__fa', '2505', 0, '固定资产', 'db', 'fa', 'db__fi')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ssctp', '2505', 0, '作业平台', 'db', 'ssctp', 'db__ssc')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ewm', '2505', 0, '维修管理', 'db', 'ewm', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__alo', '2505', 0, '资产租出管理', 'db', 'alo', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__semi', '2505', 0, '特种设备与计量', 'db', 'semi', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__rlm', '2505', 0, '周转材租入管理', 'db', 'rlm', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__pma', '2505', 0, '工程转固', 'db', 'pma', 'db__pm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmsfc', '2505', 0, '离散车间管理', 'db', 'mmsfc', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmbcm', '2505', 0, '移动条码管理-仓储', 'db', 'mmbcm', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrcm', '2505', 0, '人员合同管理', 'db', 'hrcm', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrys', '2505', 0, '预算管理', 'db', 'hrys', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmpac', '2505', 0, '生产任务管理公共', 'db', 'mmpac', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__credit', '2505', 0, '销售信用', 'db', 'credit', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__yyc', '2505', 0, '采购云', 'db', 'yyc', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ipmrisk', '2505', 0, '风险管理', 'db', 'ipmrisk', 'db__ipm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ssccm', '2505', 0, '信用管理', 'db', 'ssccm', 'db__ssc')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__baseapp', '2505', 0, '基础应用（baseapp）', 'db', 'baseapp', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__uap', '2505', 0, '统一应用平台（uap）', 'db', 'uap', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__uapbd', '2505', 0, '公共基础数据', 'db', 'uapbd', 'db__basedoc')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riaorg', '2505', 0, '组织管理', 'db', 'riaorg', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riart', '2505', 0, '运行框架', 'db', 'riart', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__tbb', '2505', 0, '企业绩效管理平台', 'db', 'tbb', 'db__epm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__fipub', '2505', 0, '财务基础设置', 'db', 'fipub', 'db__fi')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__dcm', '2505', 0, '日成本', 'db', 'dcm', 'db__co')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__scctpa', '2505', 0, '预测性洞察', 'db', 'scctpa', 'db__scct')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__scctac', '2505', 0, '自动决策', 'db', 'scctac', 'db__scct')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__yxy', '2505', 0, '营销云', 'db', 'yxy', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__pmbd', '2505', 0, '基础设置', 'db', 'pmbd', 'db__pm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ipmrm', '2505', 0, '产权管理', 'db', 'ipmrm', 'db__ipm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmdp', '2505', 0, '需求管理', 'db', 'mmdp', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmmrp', '2505', 0, '物料需求计划', 'db', 'mmmrp', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmppac', '2505', 0, '流程生产任务管理', 'db', 'mmppac', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hryf', '2505', 0, '员工服务', 'db', 'hryf', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__sscpfa', '2505', 0, '共享服务绩效分析', 'db', 'sscpfa', 'db__ssc')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__scaplm', '2505', 0, '大额资金监测', 'db', 'scaplm', 'db__scap')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ampub', '2505', 0, '基础设置', 'db', 'ampub', 'db__am')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__scmpub', '2505', 0, '供应链基础设置', 'db', 'scmpub', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ic', '2505', 0, '库存管理', 'db', 'ic', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__so', '2505', 0, '销售管理', 'db', 'so', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__price', '2505', 0, '销售价格', 'db', 'price', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ct', '2505', 0, '合同管理', 'db', 'ct', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__cm', '2505', 0, '成本管理', 'db', 'cm', 'db__co')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ica', '2505', 0, '项目成本', 'db', 'ica', 'db__co')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riaam', '2505', 0, '权限管理', 'db', 'riaam', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riacc', '2505', 0, '客户化配置', 'db', 'riacc', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riadc', '2505', 0, '开发配置工具', 'db', 'riadc', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riamm', '2505', 0, '模型定制与管理', 'db', 'riamm', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riasm', '2505', 0, '系统管理', 'db', 'riasm', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__riawf', '2505', 0, '流程平台', 'db', 'riawf', 'db__ria')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__arap', '2505', 0, '应收应付', 'db', 'arap', 'db__fi')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__sf', '2505', 0, '资金调度', 'db', 'sf', 'db__tm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__sc', '2505', 0, '委外加工', 'db', 'sc', 'db__scm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__pim', '2505', 0, '立项管理', 'db', 'pim', 'db__pm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__phm', '2505', 0, '前期管理', 'db', 'phm', 'db__pm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__pmf', '2505', 0, '竣工管理', 'db', 'pmf', 'db__pm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ipmip', '2505', 0, '投资计划', 'db', 'ipmip', 'db__ipm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmdpac', '2505', 0, '离散生产任务管理', 'db', 'mmdpac', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__mmecn', '2505', 0, '工程变更', 'db', 'mmecn', 'db__mm')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__hrkq', '2505', 0, '假勤管理', 'db', 'hrkq', 'db__hr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ffw', '2505', 0, '基础框架', 'db', 'ffw', 'db__uap')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__aeam', '2505', 0, '分析建模', 'db', 'aeam', 'db__ae')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__aemm', '2505', 0, '元数据管理', 'db', 'aemm', 'db__ae')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ae', '2505', 0, '数据处理平台', 'db', 'ae', 'db__ufofr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__aert', '2505', 0, '数据处理平台运行时', 'db', 'aert', 'db__ae')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__bq', '2505', 0, '商业分析工具', 'db', 'bq', 'db__ufofr')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__bqrt', '2505', 0, 'bq运行框架', 'db', 'bqrt', 'db__bq')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__ufofr', '2505', 0, '报表平台', 'db', 'ufofr', 'db__lightapp')",
                "insert into md_component (id, ddc_version, version_type, display_name, model_type, name, own_module) values ('db__lightbq', '2505', 0, '轻量自由报表', 'db', 'lightbq', 'db__ufofr')" };
        
        // 把以上的ddc_version替换成正确的值
        strModuleSQLs = Arrays.stream(strModuleSQLs).map(strSQL -> strSQL.replace("'bip__version'", "'" + strVersion + "'")).toArray(String[]::new);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strModuleSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public boolean isNeedSyncData()
    {
        String strSQLs[] = { "drop table md_module", "drop table md_component", "drop table md_class", "drop table md_property", "drop table md_enumvalue",
                "drop table md_index", "drop table ddc_dict_json" };
        
        if (true)
        {
            try
            {
                sqlExecutorTarget.executeUpdate(strSQLs);
            }
            catch (SQLException ex)
            {
                Logger.getLogger().error(ex.getMessage(), ex);
            }
        }
        
        return super.isNeedSyncData();
    }
    
    @Override
    protected void syncDBMeta()
    {
        Logger.getLogger().begin("sync database meta");
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(propDBSource); Connection connSource = sqlExecutorSource.getConnection())
        {
            initTableFiltersWithTempTableName(sqlExecutorSource);
            
            String strTableInModuleSQL = "select lower(a.defaulttablename),c.id from md_class a left join md_component b on a.componentid=b.id"
                    + " left join md_module c on b.ownmodule=c.id where a.classtype=201 order by defaulttablename";
            
            mapTableInModule = (Map<String, String>) sqlExecutorSource.executeQuery(strTableInModuleSQL, new MapProcessor<>());
            
            syncDBTable(sqlExecutorSource, connSource.getCatalog(), connSource.getSchema(), null);
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("sync database meta");
    }
    
    /***************************************************************************
     * @author Rocex Wang
     * @since 2021-11-11 14:00:20
     ***************************************************************************/
    @Override
    public void syncMetaData()
    {
        String strOtherSQL = "'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";
        
        String strModuleSQL = "select distinct id,name,displayname,parentmoduleid,help,versiontype," + strOtherSQL + " from md_module order by name";
        
        String strComponentSQL = "select distinct id,name,displayname,ownmodule,namespace,help,isbizmodel as biz_model,version,versiontype," + strOtherSQL
                + " from md_component";
        
        String strClassSQL = "select id,name,displayname,defaulttablename as table_name,fullclassname,keyattribute,componentid,classtype"
                + ",isprimary as primary_class,help,accessorclassname,bizitfimpclassname,refmodelname,returntype,isauthen,versiontype," + strOtherSQL
                + " from md_class"
                // + " where id like '%aaa%'"
                + " order by defaulttablename";
                
        String strPropertySQL = "select a.id id,a.name as name,a.displayname as displayname,attrlength,attrminvalue"
                + ",attrmaxvalue,attrsequence,customattr,datatype,datatypestyle,a.defaultvalue as defaultvalue"
                + ",a.nullable as nullable,a.precise as precise,a.refmodelname,classid,accesspowergroup,a.accessorclassname,dynamictable"
                + ",a.help,accesspower,calculation,dynamicattr,a.fixedlength,a.hided as hidden,a.notserialize,a.readonly,c.sqldatetype data_type_sql"
                + ",c.pkey key_prop,a.versiontype," + strOtherSQL
                + " from md_property a left join md_class b on a.classid=b.id left join md_column c on a.name=c.name and c.tableid=b.defaulttablename"
                // + " where a.classid like '%aaa%'"
                + " order by a.classid,c.pkey desc,a.attrsequence";
                
        String strEnumValueSQL = "select id as class_id,enumsequence as enum_sequence,name,value enum_value,versiontype," + strOtherSQL
                + " from md_enumvalue order by id,enumsequence";
        
        Properties dbPropSource2 = (Properties) propDBSource.clone();
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, null, strClassSQL, strPropertySQL, null, strEnumValueSQL);
        }
    }
}
