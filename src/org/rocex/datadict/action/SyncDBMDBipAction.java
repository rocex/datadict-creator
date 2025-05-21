package org.rocex.datadict.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rocex.datadict.vo.ClassVO;
import org.rocex.datadict.vo.Context;
import org.rocex.datadict.vo.MetaVO;
import org.rocex.datadict.vo.MetaVO.ModelType;
import org.rocex.datadict.vo.PropertyVO;
import org.rocex.db.SQLExecutor;
import org.rocex.db.processor.MapProcessor;
import org.rocex.utils.Logger;
import org.rocex.utils.StringHelper;

public class SyncDBMDBipAction extends SyncDBMDAction
{
    /***************************************************************************
     * @param strVersion
     * @author Rocex Wang
     * @since 2025-05-19 11:05:12
     ***************************************************************************/
    public SyncDBMDBipAction(String strVersion)
    {
        super(strVersion);
    }
    
    @Override
    public void afterSyncMetaData()
    {
        Logger.getLogger().begin("after sync metadata in bip");
        
        String[] strSQLs = {
                // "update md_class set table_name='' where (table_name is null or table_name in ('null','NULL'))",
                "update md_module set parent_module_id='md__am' where model_type='md' and id in('adc','aim','ambd','ampub','ams','aom','apm','asp','aum','eiot','iass','lim','lom','mim','omm','pam','pvm','rmm','saa','sem','sim','som','spp');",
                "update md_module set parent_module_id='md__bztrc' where model_type='md' and id in('btis','commom','common','tenant');",
                "update md_module set parent_module_id='md__cgy' where model_type='md' and id in('auction','bcres','bcsourcing','bctask','bid','check','colmsu','conformance','contractchange','cooperation','cpu','cpu-base','cpu-basedoc','cpu-bid-biddoc','cpu-bid-bidsub','cpu-bid-project','cpu-bid-score','cpu-buyoffer','cpu-contract','cpu-lawbid','cpu-order','cpu-price','cpu-pricedecision','cpu-privilege','cpu-pubapp','cpu-supplymgr','cpubase','ewallet','lawbid','mall','osmpu','osmsu','pes','privacy','qmpu','qmsu','saleauction','sourcing','suppliermgr','synergy','tpl','vmi-board','vmi-config','yc','ycyuncaimall','yonbip-cpu-bcsourcing','yonbip-cpu-lawbid','yuncai-bid-biddoc','yuncai-mall-bussiness');",
                "update md_module set parent_module_id='md__ec' where model_type='md' and id in('clm','ec','ecc','ecca','ece','eces','ecn','xtdyh','yonbip-ec-announce','yonbip-ec-base','yonbip-ec-conference','yonbip-ec-contacts','yonbip-ec-feed-ranking','yonbip-ec-file','yonbip-ec-group','yonbip-ec-iform','yonbip-ec-link','yonbip-ec-logger','yonbip-ec-meeting','yonbip-ec-official','yonbip-ec-project','yonbip-ec-resource','yonbip-ec-schedule','yonbip-ec-ucenter','yonbip-ec-upesnpub','yonbip-ec-vote');",
                "update md_module set parent_module_id='md__epm' where model_type='md' and id in('ystzd');",
                "update md_module set parent_module_id='md__fi' where model_type='md' and id in('aai','ctjl','eaai','eac','eap','ear','earap','eeac','efa','egl','eia','eis','epca','epcm','epub','esc','espa','evarc','evnt','fa','fi','fiap','fiapbill','fiar','fiarbill','fieaai','fiecc','fieces','fieia','fieml','fiepcm','fiesc','fireport','gl','pma','prjl','rcl','rvn','yonbip-fi-ctmbam','yonbip-fi-ctmfa','yonbip-fi-ctmrsm','yonbip-fi-ctmtmsp','yonbip-fi-eafcapture','yonbip-fi-eafdocfile','yonbip-fi-eafmid','yonbip-fi-eafrecord','yonbip-fi-eafsys','yonbip-fi-eia','yonbip-fi-epmbcs','yonbip-fi-epmer','yonbip-fi-epmpbf','yonbip-fi-epmplatform','yonbip-fi-sepmbcdc','yonbip-fi-sepmdcp');",
                "update md_module set parent_module_id='md__fkgxcz' where model_type='md' and id in('apct','bam','cam','cm','cmp','cspl','drft','fdtr','grm','ia','knlg','lcm','lgm','prdm','qim','sc','sla','ssc','sscimg','sscpejxpj','sscsfm','stct','stwb','tlm','tmsp','ygxygl','znbzbx','znsd');",
                "update md_module set parent_module_id='md__hr' where model_type='md' and id in('attend','bina','cs','demo','hr','hrbd','hrbudget','hrcb','hrcm','hrcpt','hrdimp','hred','hrem','hresp','hris','hrjx','hrod','hrotd','hrpa','hrpx','hrsm','hrtm','hrtset','hrxc','hrxzhs_mdd','hr_official','os','pq','re','si','ss','yonbip-hr-border','yonbip-hr-demappraisal','yonbip-hr-payext-retirementplan','yonbip-hr-payext-trustee');",
                "update md_module set parent_module_id='md__iuap' where model_type='md' and id in('archive','areaformat','asc','base','baseapp','basedata','basedoc','bd','bf','bill','billcode','billmake','bs','businesslog','coredoc','dzhtfw','eventcenter','fp','gztact','gztbdm','gztsys','gzysys','international','iuap','iuap-aip-console','iuap-aip-ipa','iuap-aip-kg','iuap-aip-ps','iuap-aip-service','iuap-aip-vpa','iuap-apcom-auditlog','iuap-apcom-coderule','iuap-apcom-contract','iuap-apcom-file','iuap-apcom-i18n','iuap-apcom-messagecenter','iuap-apcom-messageplatform','iuap-apcom-migration','iuap-apcom-prewarning','iuap-apcom-print','iuap-apcom-ruleengine','iuap-apcom-scheduler','iuap-apcom-workbench','iuap-apcom-workflow','iuap-apdoc-basedoc','iuap-hc-manager','iuap-im-core','iuap-ipaas-designer','iuap-metadata-extend','iuap-metadata-import','iuap-test','iuap-ymdp-portal','iuap-yonbuilder-businesssflow','iuap-yonbuilder-designer','iuap-yonbuilder-mobile','iuap-yonbuilder-transdata','iuap-yyc-yontest','iuap_aip_rpa','iuap_aip_search','kg-catalog-dept-auth','kg-discovery','kg-excel-table-data','kg-guide','kg-interface-push-data','kg-job','ma','md','mddcommon','mdf','nameformat','oldbd','org','pb','pc','pub','restapi','sn','social','socialmdm','sys','ucfbase','ud','ui','uimg','usermanager','xport','xt-wxxcx-you','xtwjfw','yht','ynf');",
                "update md_module set parent_module_id='md__mkt' where model_type='md' and id in('act','ap','b2broute','bbsmk','crmc','ct','cust','dc','dcas','dsfa','goods','marketing','marketingpublic','mka','om','order','pmc','prm','retail','rm','sa','sfa','udh','udm','udm-sys','udmbase','uhybase','um','uorder','usmp');",
                "update md_module set parent_module_id='md__mm' where model_type='md' and id in('aem','dcrp','dfm','dfm-ems','dfm-tam','ed','ems','hse','les','mfg','mr','mso','osm','pcc','po','qam','qmsdfm','qmsqit','qmsqts','sfc','sop','tam','tbs','vc','wjm');",
                "update md_module set parent_module_id='md__ndi' where model_type='md' and id in('yonbip-ndi-jgmj');",
                "update md_module set parent_module_id='md__pm' where model_type='md' and id in('bgdm','bgtm','ckam','pgrm','prdc','prfl','pric','pris','prjb','prjc','prjr','prtp','rscm','rskm','wkhr');",
                "update md_module set parent_module_id='md__scm' where model_type='md' and id in('aa','b2bpricing','channeloperatecenter','invp','pe','portalmg','pu','quote','sact','sccs','scct','sce','scm','scmbd','scpcommon','scpda','scpdp','scppe','scpsnpio','sh','snp','st','stock','uit','uscmf','usp','ustock','voucher');",
                "update md_module set parent_module_id='md__szyx' where model_type='md' and id in('ausa','dsd','fsm','mkp');",
                "update md_module set parent_module_id='md__tax' where model_type='md' and id in('mdd','taxbd','taxpubdoc','taxspec','yonbip-fi-taxability','yonbip-fi-taxbd','yonbip-fi-taxbuilding','yonbip-fi-taxgateway','yonbip-fi-taxincome','yonbip-fi-taxinfra','yonbip-fi-taxit','yonbip-fi-taxot','yonbip-fi-taxoth','yonbip-fi-taxotypd','yonbip-fi-taxreturn');",
                
                // 政务
                "update md_module set parent_module_id='md__yondif_ai' where model_type='md' and id in('ci','ec','element_cluster');",
                "update md_module set parent_module_id='md__yondif_ams' where model_type='md' and id in('amsauth','ybilldesigner','basparam','element','excelmanage','iem','imptools','indicatoracc','print','pwf','todomessage','workbenchextend','wtfk','ybill','ymm','yondif-ruleengine','yrule','yfile','conmanager','llm_watermanager','testbillcc','testconlist','unitfyenum','watermanager','zw-pwf','fbgbusi','fbgconf','glbindicatoracc');",
                "update md_module set parent_module_id='md__yondif_bi' where model_type='md' and id in('auth','basbank','basbpm','baseorg','basesset','basexpcri','basorgset','baspro','baspub','basstaff','dhr','ele','fbdi','sal','unitfyenum','ydfpx');",
                "update md_module set parent_module_id='md__yondif_fa' where model_type='md' and id in('fa');",
                "update md_module set parent_module_id='md__yondif_fas' where model_type='md' and id in('dfas','fasi','fasitest');",
                "update md_module set parent_module_id='md__yondif_ar' where model_type='md' and id in('arbusi','abm','aepay','ar','dk','rm','ebf');",
                "update md_module set parent_module_id='md__yondif_be' where model_type='md' and id in('centerpay','cm','fi','frbs','income','incpay','pay','payconfig','plan','tlapp','unitfyenum','up','upbas');",
                "update md_module set parent_module_id='md__yondif_bm' where model_type='md' and id in('ba','bgt','perf','pm','unitfyenum');",
                "update md_module set parent_module_id='md__yondif_gla' where model_type='md' and id in('gla_gl','glb_gl','glf_gl');",
                
                "update md_class set display_name=name where (display_name is null or display_name in ('','null','NULL'));",
                "update md_class set primary_class='1' where id in(select main_class_id from md_class where main_class_id is not null);", // 设置同一个业务对象下的主实体
                "update md_class set component_id=biz_object_id where biz_object_id is not null;",
                
                "update md_property set data_type='1976686225086391910' where data_type='attachment' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391911' where data_type='bigText' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391912' where data_type='byte' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391913' where data_type='contact' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391914' where data_type='correlation' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391915' where data_type='date' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391916' where data_type='date_MDD' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391917' where data_type='dateTime' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391918' where data_type='dateTime_Timestamp' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391919' where data_type='decimalRange' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391920' where data_type='DefaultCT' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391921' where data_type='FreeCT' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391922' where data_type='image' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391923' where data_type='int' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391924' where data_type='intDate' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391925' where data_type='intDateTime' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391926' where data_type='link' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391927' where data_type='long' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391928' where data_type='MaterialPropCT' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391929' where data_type='multiLanguage' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391930' where data_type='multiLineText' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391931' where data_type='multipleOption' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391932' where data_type='multipleOption_String' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391933' where data_type='number' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391934' where data_type='OptionCT' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391935' where data_type='quote' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391936' where data_type='quoteList' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391937' where data_type='short' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391938' where data_type='singleOption' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391939' where data_type='singleOption_int' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391940' where data_type='singleOption_Short' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391941' where data_type='singleOption_String' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391942' where data_type='SkuPropCT' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391943' where data_type='switch' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391944' where data_type='text' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391945' where data_type='text_MDD' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391946' where data_type='time' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391947' where data_type='time_MDD' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391948' where data_type='timestamp' and ddc_version='${version}'",
                "update md_property set data_type='1976686225086391949' where data_type='UserDefine' and ddc_version='${version}'",
                
                "update md_property set default_value=null where default_value in('null','NULL');",
                "update md_property set attr_min_value=null where attr_min_value in('','null','NULL');",
                "update md_property set attr_max_value=null where attr_max_value in('','null','NULL');" };
        
        strSQLs = replace("${version}", strVersion, strSQLs);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strSQLs);
        }
        catch (Exception ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
        
        Logger.getLogger().end("after sync metadata in bip");
    }
    
    @Override
    public void beforeSyncMetaData()
    {
        Logger.getLogger().begin("before sync metadata in bip");
        
        super.beforeSyncMetaData();
        
        String[] strDBModuleSQLs = {
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__iuap', '${version}', '技术应用平台', 'md', 'iuap', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__fi', '${version}', '智能会计', 'md', 'fi', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__scm', '${version}', '供应链云', 'md', 'scm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__hr', '${version}', '人力云', 'md', 'hr', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__mkt', '${version}', '营销云', 'md', 'mkt', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__am', '${version}', '资产云', 'md', 'am', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__cgy', '${version}', '采购云', 'md', 'cgy', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__tax', '${version}', '税务云', 'md', 'tax', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__mm', '${version}', '制造云', 'md', 'mm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__epm', '${version}', '企业绩效', 'md', 'epm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__pm', '${version}', '项目云', 'md', 'pm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__trst', '${version}', '云可信', 'md', 'trst', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__bztrc', '${version}', '商旅云', 'md', 'bztrc', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ctrm', '${version}', '贸易云', 'md', 'ctrm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__base', '${version}', '领域基础', 'md', 'base', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ec', '${version}', '协同云', 'md', 'ec', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__szyx', '${version}', '数字营销', 'md', 'szyx', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__fkgxcz', '${version}', '费控共享财资云', 'md', 'fkgxcz', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__ndi', '${version}', '国防工业云', 'md', 'ndi', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__other', '${version}', '其它', 'md', 'other', 'md_clazz');",
                
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__iuap', '${version}', '技术应用平台', 'db', 'iuap', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__fi', '${version}', '智能会计', 'db', 'fi', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__scm', '${version}', '供应链云', 'db', 'scm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__hr', '${version}', '人力云', 'db', 'hr', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__mkt', '${version}', '营销云', 'db', 'mkt', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__am', '${version}', '资产云', 'db', 'am', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__cgy', '${version}', '采购云', 'db', 'cgy', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__tax', '${version}', '税务云', 'db', 'tax', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__mm', '${version}', '制造云', 'db', 'mm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__epm', '${version}', '企业绩效', 'db', 'epm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__pm', '${version}', '项目云', 'db', 'pm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__trst', '${version}', '云可信', 'db', 'trst', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__bztrc', '${version}', '商旅云', 'db', 'bztrc', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ctrm', '${version}', '贸易云', 'db', 'ctrm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__base', '${version}', '领域基础', 'db', 'base', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ec', '${version}', '协同云', 'db', 'ec', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__szyx', '${version}', '数字营销', 'db', 'szyx', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__fkgxcz', '${version}', '费控共享财资云', 'db', 'fkgxcz', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__ndi', '${version}', '国防工业云', 'db', 'ndi', 'db_table');",
                
                // 政务
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_ams', '${version}', 'YonDiF-应用平台扩展', 'md', 'yondif_ams', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_bi', '${version}', 'YonDiF-基础信息', 'md', 'yondif_bi', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_ur', '${version}', 'YonDiF-报表云', 'md', 'yondif_ur', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_ai', '${version}', 'YonDiF-智能平台扩展', 'md', 'yondif_ai', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_bm', '${version}', 'YonDiF-预算管理', 'md', 'yondif_bm', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_be', '${version}', 'YonDiF-预算执行', 'md', 'yondif_be', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_ar', '${version}', 'YonDiF-报销管理', 'md', 'yondif_ar', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_glf', '${version}', 'YonDiF-财政总会计核算', 'md', 'yondif_glf', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_glb', '${version}', 'YonDiF-预算指标核算', 'md', 'yondif_glb', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_gla', '${version}', 'YonDiF-单位会计核算', 'md', 'yondif_gla', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_agcfs', '${version}', 'YonDiF-政府财务报告', 'md', 'yondif_agcfs', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_agfa', '${version}', 'YonDiF-财政总决算', 'md', 'yondif_agfa', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_fas', '${version}', 'YonDiF-财会监督', 'md', 'yondif_fas', 'md_clazz');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('md__yondif_fa', '${version}', 'YonDiF-资产管理', 'md', 'yondif_fa', 'md_clazz');",
                
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_ams', '${version}', 'YonDiF-应用平台扩展', 'db', 'yondif_ams', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_bi', '${version}', 'YonDiF-基础信息', 'db', 'yondif_bi', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_ur', '${version}', 'YonDiF-报表云', 'db', 'yondif_ur', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_ai', '${version}', 'YonDiF-智能平台扩展', 'db', 'yondif_ai', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_bm', '${version}', 'YonDiF-预算管理', 'db', 'yondif_bm', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_be', '${version}', 'YonDiF-预算执行', 'db', 'yondif_be', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_ar', '${version}', 'YonDiF-报销管理', 'db', 'yondif_ar', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_glf', '${version}', 'YonDiF-财政总会计核算', 'db', 'yondif_glf', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_glb', '${version}', 'YonDiF-预算指标核算', 'db', 'yondif_glb', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_gla', '${version}', 'YonDiF-单位会计核算', 'db', 'yondif_gla', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_agcfs', '${version}', 'YonDiF-政府财务报告', 'db', 'yondif_agcfs', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_agfa', '${version}', 'YonDiF-财政总决算', 'db', 'yondif_agfa', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_fas', '${version}', 'YonDiF-财会监督', 'db', 'yondif_fas', 'db_table');",
                "insert into md_module (id, ddc_version, display_name, model_type, name, parent_module_id) values ('db__yondif_fa', '${version}', 'YonDiF-资产管理', 'db', 'yondif_fa', 'db_table');" };
        
        String[] strDBComponentSQLs = {
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aim', '${version}', 'db', 'amc_aim', 'db__am');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_ambd', '${version}', 'db', 'amc_ambd', 'db__am');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aom', '${version}', 'db', 'amc_aom', 'db__am');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_aum', '${version}', 'db', 'amc_aum', 'db__am');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__amc_mro', '${version}', 'db', 'amc_mro', 'db__am');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__btis', '${version}', 'db', 'btis', 'db__bztrc');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_adjust', '${version}', 'db', 'cpu_adjust', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_baseservice', '${version}', 'db', 'cpu_baseservice', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_base_doc', '${version}', 'db', 'cpu_base_doc', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_base_portal', '${version}', 'db', 'cpu_base_portal', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_bi', '${version}', 'db', 'cpu_bi', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_bid', '${version}', 'db', 'cpu_bid', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_cooperation', '${version}', 'db', 'cpu_cooperation', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_lawbid', '${version}', 'db', 'cpu_lawbid', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_mall', '${version}', 'db', 'cpu_mall', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_pubbiz_sourcing', '${version}', 'db', 'cpu_pubbiz_sourcing', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_pubservice', '${version}', 'db', 'cpu_pubservice', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_sourcing', '${version}', 'db', 'cpu_sourcing', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_suppliermgr', '${version}', 'db', 'cpu_suppliermgr', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cpu_synergy', '${version}', 'db', 'cpu_synergy', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pub_dop', '${version}', 'db', 'pub_dop', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yuncai_yop', '${version}', 'db', 'yuncai_yop', 'db__cgy');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctrm_ct', '${version}', 'db', 'ctrm_ct', 'db__ctrm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctrm_share', '${version}', 'db', 'ctrm_share', 'db__ctrm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__db_project', '${version}', 'db', 'db_project', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_0', '${version}', 'db', 'encrypt_0', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_1', '${version}', 'db', 'encrypt_1', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_10', '${version}', 'db', 'encrypt_10', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_11', '${version}', 'db', 'encrypt_11', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_12', '${version}', 'db', 'encrypt_12', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_13', '${version}', 'db', 'encrypt_13', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_14', '${version}', 'db', 'encrypt_14', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_15', '${version}', 'db', 'encrypt_15', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_2', '${version}', 'db', 'encrypt_2', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_3', '${version}', 'db', 'encrypt_3', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_4', '${version}', 'db', 'encrypt_4', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_5', '${version}', 'db', 'encrypt_5', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_6', '${version}', 'db', 'encrypt_6', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_7', '${version}', 'db', 'encrypt_7', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_8', '${version}', 'db', 'encrypt_8', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__encrypt_9', '${version}', 'db', 'encrypt_9', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_announce', '${version}', 'db', 'esn_announce', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_api', '${version}', 'db', 'esn_api', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_base', '${version}', 'db', 'esn_base', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_email', '${version}', 'db', 'esn_email', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_file', '${version}', 'db', 'esn_file', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_file_docs', '${version}', 'db', 'esn_file_docs', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_im', '${version}', 'db', 'esn_im', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_link', '${version}', 'db', 'esn_link', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_message', '${version}', 'db', 'esn_message', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_plugins', '${version}', 'db', 'esn_plugins', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_schedule', '${version}', 'db', 'esn_schedule', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__form_relation', '${version}', 'db', 'form_relation', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iform', '${version}', 'db', 'iform', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__key_db', '${version}', 'db', 'key_db', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__kfpt_openapi', '${version}', 'db', 'kfpt_openapi', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__meeting', '${version}', 'db', 'meeting', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__opsys', '${version}', 'db', 'opsys', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__supervision', '${version}', 'db', 'supervision', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ucenter', '${version}', 'db', 'ucenter', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_clm_contract', '${version}', 'db', 'yonbip_clm_contract', 'db__pm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ec_logger', '${version}', 'db', 'yonbip_ec_logger', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_base', '${version}', 'db', 'epmp_base', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bcdc', '${version}', 'db', 'epmp_bcdc', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bcs', '${version}', 'db', 'epmp_bcs', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bill', '${version}', 'db', 'epmp_bill', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_bp', '${version}', 'db', 'epmp_bp', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_br', '${version}', 'db', 'epmp_br', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__epmp_dcp', '${version}', 'db', 'epmp_dcp', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieaai', '${version}', 'db', 'fieaai', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiearap', '${version}', 'db', 'fiearap', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiearapbill', '${version}', 'db', 'fiearapbill', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiecc', '${version}', 'db', 'fiecc', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieeac', '${version}', 'db', 'fieeac', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiefa', '${version}', 'db', 'fiefa', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieia', '${version}', 'db', 'fieia', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieis', '${version}', 'db', 'fieis', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiepca', '${version}', 'db', 'fiepca', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fieprjl', '${version}', 'db', 'fieprjl', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiepub', '${version}', 'db', 'fiepub', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiercl', '${version}', 'db', 'fiercl', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiervn', '${version}', 'db', 'fiervn', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fievarc', '${version}', 'db', 'fievarc', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__figl', '${version}', 'db', 'figl', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__figlbase', '${version}', 'db', 'figlbase', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__saas', '${version}', 'db', 'saas', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ejls', '${version}', 'db', 'yonbip_fi_ejls', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__apct', '${version}', 'db', 'apct', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmbam', '${version}', 'db', 'ctmbam', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcam', '${version}', 'db', 'ctmcam', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcmp', '${version}', 'db', 'ctmcmp', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmcspl', '${version}', 'db', 'ctmcspl', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmdrft', '${version}', 'db', 'ctmdrft', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfa', '${version}', 'db', 'ctmfa', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfdtr', '${version}', 'db', 'ctmfdtr', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmgrm', '${version}', 'db', 'ctmgrm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmlcm', '${version}', 'db', 'ctmlcm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmlgm', '${version}', 'db', 'ctmlgm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmrsm', '${version}', 'db', 'ctmrsm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmstct', '${version}', 'db', 'ctmstct', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmstwb', '${version}', 'db', 'ctmstwb', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmthre', '${version}', 'db', 'ctmthre', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmtlm', '${version}', 'db', 'ctmtlm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmtmsp', '${version}', 'db', 'ctmtmsp', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__intelligent_audit', '${version}', 'db', 'intelligent_audit', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__intelligent_robot', '${version}', 'db', 'intelligent_robot', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__shared_service', '${version}', 'db', 'shared_service', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_img', '${version}', 'db', 'ssc_img', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_km', '${version}', 'db', 'ssc_km', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_pdm', '${version}', 'db', 'ssc_pdm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_pe', '${version}', 'db', 'ssc_pe', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_sfm', '${version}', 'db', 'ssc_sfm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_sla', '${version}', 'db', 'ssc_sla', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc_staff_credit', '${version}', 'db', 'ssc_staff_credit', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmpub', '${version}', 'db', 'yonbip_fi_ctmpub', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__znbz', '${version}', 'db', 'znbz', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__znbz_fileservice', '${version}', 'db', 'znbz_fileservice', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__cloud_attachment', '${version}', 'db', 'cloud_attachment', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__corehr', '${version}', 'db', 'corehr', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_cpt', '${version}', 'db', 'diwork_cpt', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_esp', '${version}', 'db', 'diwork_esp', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_hr_bonus', '${version}', 'db', 'diwork_hr_bonus', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_hr_budget', '${version}', 'db', 'diwork_hr_budget', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_pub', '${version}', 'db', 'diwork_pub', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_talent', '${version}', 'db', 'diwork_talent', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_train', '${version}', 'db', 'diwork_train', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_wa', '${version}', 'db', 'diwork_wa', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_wa_mdd', '${version}', 'db', 'diwork_wa_mdd', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hrattenddb', '${version}', 'db', 'hrattenddb', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_cpt', '${version}', 'db', 'hr_cpt', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_dataanalysis', '${version}', 'db', 'hr_dataanalysis', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_intelligent', '${version}', 'db', 'hr_intelligent', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_official', '${version}', 'db', 'hr_official', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_orgsystem', '${version}', 'db', 'hr_orgsystem', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_otd', '${version}', 'db', 'hr_otd', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_qualify', '${version}', 'db', 'hr_qualify', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_sinsurance', '${version}', 'db', 'hr_sinsurance', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_socialization', '${version}', 'db', 'hr_socialization', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_staffing', '${version}', 'db', 'hr_staffing', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__performance', '${version}', 'db', 'performance', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ssc', '${version}', 'db', 'ssc', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hr_tm', '${version}', 'db', 'yonbip_hr_tm', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__approvecenter', '${version}', 'db', 'approvecenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__auth_0', '${version}', 'db', 'auth_0', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__bits', '${version}', 'db', 'bits', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__bussiness_expansion_third_pub', '${version}', 'db', 'bussiness_expansion_third_pub', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ctmfdma', '${version}', 'db', 'ctmfdma', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__esn_portal', '${version}', 'db', 'esn_portal', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__fiproduct', '${version}', 'db', 'fiproduct', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__helpcenter', '${version}', 'db', 'helpcenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__high-yxy', '${version}', 'db', 'high-yxy', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ht_staffing', '${version}', 'db', 'ht_staffing', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__idm', '${version}', 'db', 'idm', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_data', '${version}', 'db', 'iuap_aip_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_ipa', '${version}', 'db', 'iuap_aip_ipa', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_kg', '${version}', 'db', 'iuap_aip_kg', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_ps', '${version}', 'db', 'iuap_aip_ps', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_search', '${version}', 'db', 'iuap_aip_search', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_aip_vpa', '${version}', 'db', 'iuap_aip_vpa', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_aprvcntr', '${version}', 'db', 'iuap_apcom_aprvcntr', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_auth', '${version}', 'db', 'iuap_apcom_auth', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_benchservice', '${version}', 'db', 'iuap_apcom_benchservice', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_businesssflow', '${version}', 'db', 'iuap_apcom_businesssflow', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_contract', '${version}', 'db', 'iuap_apcom_contract', 'db__pm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_file', '${version}', 'db', 'iuap_apcom_file', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_messagecenter', '${version}', 'db', 'iuap_apcom_messagecenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_messageservice', '${version}', 'db', 'iuap_apcom_messageservice', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_migration', '${version}', 'db', 'iuap_apcom_migration', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_print', '${version}', 'db', 'iuap_apcom_print', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_ruleengine', '${version}', 'db', 'iuap_apcom_ruleengine', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_supportcenter', '${version}', 'db', 'iuap_apcom_supportcenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apcom_workflow', '${version}', 'db', 'iuap_apcom_workflow', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_basedoc', '${version}', 'db', 'iuap_apdoc_basedoc', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_coredoc', '${version}', 'db', 'iuap_apdoc_coredoc', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_finbd', '${version}', 'db', 'iuap_apdoc_finbd', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_apdoc_social', '${version}', 'db', 'iuap_apdoc_social', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_cloud_basedoc', '${version}', 'db', 'iuap_cloud_basedoc', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_data_datafusion', '${version}', 'db', 'iuap_data_datafusion', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_devops_data', '${version}', 'db', 'iuap_devops_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_dp_data', '${version}', 'db', 'iuap_dp_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_hubble_data', '${version}', 'db', 'iuap_hubble_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_im_core', '${version}', 'db', 'iuap_im_core', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ipaas', '${version}', 'db', 'iuap_ipaas', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ipaas_mdm', '${version}', 'db', 'iuap_ipaas_mdm', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_base', '${version}', 'db', 'iuap_metadata_base', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_extendservice', '${version}', 'db', 'iuap_metadata_extendservice', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_import', '${version}', 'db', 'iuap_metadata_import', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_metadata_service', '${version}', 'db', 'iuap_metadata_service', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_uuas_usercenter', '${version}', 'db', 'iuap_uuas_usercenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ymc_data', '${version}', 'db', 'iuap_ymc_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ymdp_portal', '${version}', 'db', 'iuap_ymdp_portal', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yms_console', '${version}', 'db', 'iuap_yms_console', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yms_data', '${version}', 'db', 'iuap_yms_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yonbuilder_dynamic_ds', '${version}', 'db', 'iuap_yonbuilder_dynamic_ds', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yonbuilder_service', '${version}', 'db', 'iuap_yonbuilder_service', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_ypr_data', '${version}', 'db', 'iuap_ypr_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_yyc_data', '${version}', 'db', 'iuap_yyc_data', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mtd_0331', '${version}', 'db', 'mtd_0331', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mysql', '${version}', 'db', 'mysql', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__todocenter', '${version}', 'db', 'todocenter', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uba', '${version}', 'db', 'uba', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__udhcloud', '${version}', 'db', 'udhcloud', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ugoods', '${version}', 'db', 'ugoods', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ycreport', '${version}', 'db', 'ycreport', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yms_pre', '${version}', 'db', 'yms_pre', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip-indmkt', '${version}', 'db', 'yonbip-indmkt', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_cpu_dw', '${version}', 'db', 'yonbip_cpu_dw', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__engineering_data', '${version}', 'db', 'engineering_data', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ils_les', '${version}', 'db', 'ils_les', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp', '${version}', 'db', 'imp', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_dfm', '${version}', 'db', 'imp_dfm', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_hse', '${version}', 'db', 'imp_hse', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_ib', '${version}', 'db', 'imp_ib', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_plm', '${version}', 'db', 'imp_plm', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__imp_sfc', '${version}', 'db', 'imp_sfc', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mf_ecn', '${version}', 'db', 'mf_ecn', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__production_order', '${version}', 'db', 'production_order', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__qms_dfm', '${version}', 'db', 'qms_dfm', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__qms_qit', '${version}', 'db', 'qms_qit', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__requirements_planning', '${version}', 'db', 'requirements_planning', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_mm_sop', '${version}', 'db', 'yonbip_mm_sop', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_hrfp', '${version}', 'db', 'yonbip_ndi_hrfp', 'db__ndi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_jgmj', '${version}', 'db', 'yonbip_ndi_jgmj', 'db__ndi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_smxt', '${version}', 'db', 'yonbip_ndi_smxt', 'db__ndi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__quotation', '${version}', 'db', 'quotation', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sact', '${version}', 'db', 'sact', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scct', '${version}', 'db', 'scct', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scmbd', '${version}', 'db', 'scmbd', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scmmp', '${version}', 'db', 'scmmp', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__scp', '${version}', 'db', 'scp', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__upurchase', '${version}', 'db', 'upurchase', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__ustock', '${version}', 'db', 'ustock', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_scm_dw', '${version}', 'db', 'yonbip_scm_dw', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indagts', '${version}', 'db', 'indagts', 'db__szyx');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__inddsd', '${version}', 'db', 'inddsd', 'db__szyx');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indfsm', '${version}', 'db', 'indfsm', 'db__szyx');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__indrp', '${version}', 'db', 'indrp', 'db__szyx');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_billcenter', '${version}', 'db', 'diwork_billcenter', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_building', '${version}', 'db', 'diwork_building', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_einvoice_auth', '${version}', 'db', 'diwork_einvoice_auth', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_income', '${version}', 'db', 'diwork_income', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_infra', '${version}', 'db', 'diwork_infra', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_input_tax', '${version}', 'db', 'diwork_input_tax', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_invoice_bd', '${version}', 'db', 'diwork_invoice_bd', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_other_tax', '${version}', 'db', 'diwork_other_tax', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_piaoeda_client', '${version}', 'db', 'diwork_piaoeda_client', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_report', '${version}', 'db', 'diwork_report', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxability', '${version}', 'db', 'diwork_taxability', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxpubdoc', '${version}', 'db', 'diwork_taxpubdoc', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__diwork_taxspec', '${version}', 'db', 'diwork_taxspec', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__idoc', '${version}', 'db', 'idoc', 'db__tax');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__trustedsign_v5', '${version}', 'db', 'trustedsign_v5', 'db__trst');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hy_doccrbd', '${version}', 'db', 'yonbip_hy_doccrbd', 'db__trst');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__crm', '${version}', 'db', 'crm', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__omsdata', '${version}', 'db', 'omsdata', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pricecenter', '${version}', 'db', 'pricecenter', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__quartz', '${version}', 'db', 'quartz', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sddcdata', '${version}', 'db', 'sddcdata', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__udm', '${version}', 'db', 'udm', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uorders', '${version}', 'db', 'uorders', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__upmalls', '${version}', 'db', 'upmalls', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uretaildata', '${version}', 'db', 'uretaildata', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__usmp', '${version}', 'db', 'usmp', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yilian', '${version}', 'db', 'yilian', 'db__mkt');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pmcloud', '${version}', 'db', 'pmcloud', 'db__pm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pmcloud_ods', '${version}', 'db', 'pmcloud_ods', 'db__pm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__pca', '${version}', 'db', 'pca', 'db__mm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__uinttrade', '${version}', 'db', 'uinttrade', 'db__scm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmfm', '${version}', 'db', 'yonbip_fi_ctmfm', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmfm_init', '${version}', 'db', 'yonbip_fi_ctmfm_init', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmlc', '${version}', 'db', 'yonbip_fi_ctmlc', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmlc_init', '${version}', 'db', 'yonbip_fi_ctmlc_init', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_ctmpub_init', '${version}', 'db', 'yonbip_fi_ctmpub_init', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_merge_schema', '${version}', 'db', 'yonbip_fi_merge_schema', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_installer', '${version}', 'db', 'iuap_installer', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__logger', '${version}', 'db', 'logger', 'db__iuap');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__sys', '${version}', 'db', 'sys', 'db__iuap');",
                
                // HR
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__hr_border', '${version}', 'db', 'hr_border', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hr_common', '${version}', 'db', 'yonbip_hr_common', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_hr_dimp', '${version}', 'db', 'yonbip_hr_dimp', 'db__hr');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_mid_sscex', '${version}', 'db', 'yonbip_mid_sscex', 'db__fkgxcz');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__mtd_base', '${version}', 'db', 'mtd_base', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ec_contacts', '${version}', 'db', 'yonbip_ec_contacts', 'db__ec');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_fi_revenue', '${version}', 'db', 'yonbip_fi_revenue', 'db__fi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_ndi_extplugin', '${version}', 'db', 'yonbip_ndi_extplugin', 'db__ndi');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_epm_abca', '${version}', 'db', 'yonbip_epm_abca', 'db__epm');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__yonbip_trst_ekdm', '${version}', 'db', 'yonbip_trst_ekdm', 'db__trst');",
                "insert into md_component (id, ddc_version, model_type, name, own_module) values ('db__iuap_data_dolphinscheduler', '${version}', 'db', 'iuap_data_dolphinscheduler', 'db__iuap');",
                
                // 政务
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ams_file_online', '${version}', 'db', 'yondif_ams_file_online', 'db__yondif_ams', '文件拓展');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ams_online', '${version}', 'db', 'yondif_ams_online', 'db__yondif_ams', '应用基础');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_print_online', '${version}', 'db', 'yondif_print_online', 'db__yondif_ams', '打印');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_agfa_online', '${version}', 'db', 'yondif_agfa_online', 'db__yondif_agfa', '财政总决算');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_gar_agcfs_online', '${version}', 'db', 'yondif_gar_agcfs_online', 'db__yondif_agcfs', '政府财务报告');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ai_adapter_online', '${version}', 'db', 'yondif_ai_adapter_online', 'db__yondif_ai', '大模型适配');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ai_app_online', '${version}', 'db', 'yondif_ai_app_online', 'db__yondif_ai', '智能应用支撑');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ai_law_online', '${version}', 'db', 'yondif_ai_law_online', 'db__yondif_ai', '智能法规库');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_dmp_dq_online', '${version}', 'db', 'yondif_dmp_dq_online', 'db__yondif_ai', '自助查询');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_pm_fa_online', '${version}', 'db', 'yondif_pm_fa_online', 'db__yondif_fa', '资产管理');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_fbdi_online', '${version}', 'db', 'yondif_fbdi_online', 'db__yondif_bi', '基础信息');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ur_online', '${version}', 'db', 'yondif_ur_online', 'db__yondif_ur', '报表云');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_bm_online', '${version}', 'db', 'yondif_bm_online', 'db__yondif_bm', '预算管理');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_be_online', '${version}', 'db', 'yondif_be_online', 'db__yondif_be', '预算执行');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_ar2_online', '${version}', 'db', 'yondif_ar2_online', 'db__yondif_ar', '网上报销');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_vm_ebf_online', '${version}', 'db', 'yondif_vm_ebf_online', 'db__yondif_ar', '电子票夹');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_glf_online', '${version}', 'db', 'yondif_glf_online', 'db__yondif_glf', '财政总会计核算');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_glb_online', '${version}', 'db', 'yondif_glb_online', 'db__yondif_glb', '预算指标核算');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_gla_online', '${version}', 'db', 'yondif_gla_online', 'db__yondif_gla', '单位会计核算');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_fas_dfas_online', '${version}', 'db', 'yondif_fas_dfas_online', 'db__yondif_fas', '监督管理');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_fas_faae_online', '${version}', 'db', 'yondif_fas_faae_online', 'db__yondif_fas', '考核评价');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_fas_fasi_online', '${version}', 'db', 'yondif_fas_fasi_online', 'db__yondif_fas', '监督检查');",
                "insert into md_component (id, ddc_version, model_type, name, own_module, display_name) values ('db__yondif_fas_psrc_online', '${version}', 'db', 'yondif_fas_psrc_online', 'db__yondif_fas', '监督规则中心');" };
        
        String[] strMDComponentSQLs = {
                "insert into md_component (id, biz_model, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391905', '0', '${version}', 'BIP基础数据类型', 'md', 'BipBasicDataType', 'iuap');" };
        
        String[] strMDClassSQLs = {
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391910', 1, '1976686225086391905', '${version}', '附件', 'md', 'Attachment', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391911', 1, '1976686225086391905', '${version}', '大文本', 'md', 'BigText', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391912', 1, '1976686225086391905', '${version}', '字节', 'md', 'Byte', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391913', 1, '1976686225086391905', '${version}', '联系人', 'md', 'Contact', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391914', 1, '1976686225086391905', '${version}', '相关性', 'md', 'Correlation', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391915', 1, '1976686225086391905', '${version}', '日期', 'md', 'Date', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391916', 1, '1976686225086391905', '${version}', '日期', 'md', 'Date_MDD', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391917', 1, '1976686225086391905', '${version}', '日期时间', 'md', 'DateTime', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391918', 1, '1976686225086391905', '${version}', '日期时间戳', 'md', 'DateTime_Timestamp', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391919', 1, '1976686225086391905', '${version}', '小数范围', 'md', 'DecimalRange', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391920', 1, '1976686225086391905', '${version}', 'DefaultCT', 'md', 'DefaultCT', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391921', 1, '1976686225086391905', '${version}', 'FreeCT', 'md', 'FreeCT', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391922', 1, '1976686225086391905', '${version}', '图片', 'md', 'Image', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391923', 1, '1976686225086391905', '${version}', '整数', 'md', 'Int', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391924', 1, '1976686225086391905', '${version}', '整数日期', 'md', 'IntDate', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391925', 1, '1976686225086391905', '${version}', '整数日期时间', 'md', 'IntDateTime', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391926', 1, '1976686225086391905', '${version}', '链接', 'md', 'Link', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391927', 1, '1976686225086391905', '${version}', '长整数', 'md', 'Long', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391928', 1, '1976686225086391905', '${version}', 'MaterialPropCT', 'md', 'MaterialPropCT', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391929', 1, '1976686225086391905', '${version}', '多语言', 'md', 'MultiLanguage', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391930', 1, '1976686225086391905', '${version}', '多行文本', 'md', 'MultiLineText', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391931', 1, '1976686225086391905', '${version}', '多选项', 'md', 'MultipleOption', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391932', 1, '1976686225086391905', '${version}', '多选项-字符串', 'md', 'MultipleOption_String', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391933', 1, '1976686225086391905', '${version}', '数字', 'md', 'Number', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391934', 1, '1976686225086391905', '${version}', 'OptionCT', 'md', 'OptionCT', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391935', 1, '1976686225086391905', '${version}', '引用', 'md', 'Quote', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391936', 1, '1976686225086391905', '${version}', '引用-列表', 'md', 'QuoteList', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391937', 1, '1976686225086391905', '${version}', '短整数', 'md', 'Short', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391938', 1, '1976686225086391905', '${version}', '单选项', 'md', 'SingleOption', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391939', 1, '1976686225086391905', '${version}', '单选项-整数', 'md', 'SingleOption_int', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391940', 1, '1976686225086391905', '${version}', '单选项-短整数', 'md', 'SingleOption_Short', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391941', 1, '1976686225086391905', '${version}', '单选项-字符串', 'md', 'SingleOption_String', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391942', 1, '1976686225086391905', '${version}', 'SkuPropCT', 'md', 'SkuPropCT', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391943', 1, '1976686225086391905', '${version}', '开关', 'md', 'Switch', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391944', 1, '1976686225086391905', '${version}', '文本', 'md', 'Text', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391945', 1, '1976686225086391905', '${version}', '文本', 'md', 'Text_MDD', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391946', 1, '1976686225086391905', '${version}', '时间', 'md', 'Time', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391947', 1, '1976686225086391905', '${version}', '时间', 'md', 'Time_MDD', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391948', 1, '1976686225086391905', '${version}', '时间戳', 'md', 'Timestamp', null);",
                "insert into md_class (id, class_type, component_id, ddc_version, display_name, model_type, name, own_module) values ('1976686225086391949', 1, '1976686225086391905', '${version}', '用户定义', 'md', 'UserDefine', null);" };
        
        // 把以上的ddc_version替换成正确的值
        strDBModuleSQLs = replace("${version}", strVersion, strDBModuleSQLs);
        strDBComponentSQLs = replace("${version}", strVersion, strDBComponentSQLs);
        strMDComponentSQLs = replace("${version}", strVersion, strMDComponentSQLs);
        strMDClassSQLs = replace("${version}", strVersion, strMDClassSQLs);
        
        try
        {
            sqlExecutorTarget.executeUpdate(strDBModuleSQLs);
            sqlExecutorTarget.executeUpdate(strDBComponentSQLs);
            
            sqlExecutorTarget.executeUpdate(strMDComponentSQLs);
            sqlExecutorTarget.executeUpdate(strMDClassSQLs);
        }
        catch (SQLException ex)
        {
            throw new RuntimeException(ex);
        }
        
        Logger.getLogger().end("before sync metadata in bip");
    }
    
    @Override
    protected String getDataTypeSql(PropertyVO propertyVO)
    {
        // 旗舰版的元数据中没有数据库字段类型，需要跟数据库对比找下类型
        if (ModelType.md.name().equalsIgnoreCase(propertyVO.getModelType()))
        {
            PropertyVO tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + propertyVO.getName());
            if (tableNamePropertyVO == null)
            {
                tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + StringHelper.camelToUnderline(propertyVO.getName()));
                if (tableNamePropertyVO == null)
                {
                    tableNamePropertyVO = mapPropertyVOByTableName.get(propertyVO.getTableName() + "." + StringHelper.underlineToCamel(propertyVO.getName()));
                }
            }
            
            if (tableNamePropertyVO != null)
            {
                String strDataTypeSql = tableNamePropertyVO.getDataTypeSql();
                
                propertyVO.setColumnCode(tableNamePropertyVO.getName());
                
                if (StringHelper.isNotBlank(strDataTypeSql))
                {
                    propertyVO.setDataTypeSql(strDataTypeSql);
                    
                    return strDataTypeSql;
                }
            }
            
            String strDataType = propertyVO.getDataType() == null ? "varchar" : propertyVO.getDataType();
            propertyVO.setDataTypeSql(strDataType.contains("text") ? "text" : strDataType);
            
            // 参照-305,枚举-203
            if (strDataType.length() == 19
                    && (propertyVO.getDataTypeStyle() == 305 && propertyVO.getRefModelName() != null || propertyVO.getDataTypeStyle() == 203))
            {
                propertyVO.setDataTypeSql("varchar");
                
                if (propertyVO.getAttrLength() == null)
                {
                    propertyVO.setAttrLength(22);
                }
            }
        }
        
        return super.getDataTypeSql(propertyVO);
    }
    
    @Override
    protected void initTableNameFilters(SQLExecutor sqlExecutorSource2)
    {
        String strSourceUrl = Context.getInstance().getSetting("jdbc.url");
        Properties dbPropSource2 = (Properties) propDBSource.clone();
        dbPropSource2.setProperty("jdbc.url", strSourceUrl.replace("${schema}", "iuap_uuas_usercenter"));
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSource2))
        {
            // 所有表名中包含租户id的都不要
            String strSQL = "select tenant_id,tenant_id from iuap_uuas_usercenter.pub_tenant where tenant_id not in('super','default')";
            
            List<String> listTenantId = new ArrayList<>();
            
            Map<String, String> mapTable = (Map<String, String>) sqlExecutorSource.executeQuery(strSQL, new MapProcessor<>());
            
            listTenantId.addAll(mapTable.keySet());
            listTenantId.addAll(Arrays.asList(strTableIncludeFilters));
            
            strTableIncludeFilters = listTenantId.toArray(new String[0]);
        }
        catch (SQLException ex)
        {
            Logger.getLogger().error(ex.getMessage(), ex);
        }
    }
    
    @Override
    public void syncMDMetaData()
    {
        Logger.getLogger().begin("sync md metadata in bip");
        
        if (!isSyncMD)
        {
            Logger.getLogger().end("sync md metadata in bip", "isSyncMD=%s, skip...", isSyncMD);
            return;
        }
        
        String strOtherSQL1 = ",'" + strVersion + "' as ddc_version,'" + strTs + "' as ts";
        String strOtherSQL2 = ",'" + MetaVO.ModelType.md.name() + "' as model_type" + strOtherSQL1;
        
        String strModuleSQL = "select distinct lower(own_module) as id,own_module as display_name,null as help,own_module as name,'md__other' as parent_module_id,null as version_type"
                + strOtherSQL2
                + " from md_meta_component where ytenant_id='0' and own_module is not null and own_module not in('','null','NULL') order by own_module";
        
        String strComponentSQL = "select id,null as biz_model,display_name,null as help,name,null as namespace,lower(own_module) as own_module,null as version,null as version_type"
                + strOtherSQL2
                + " from md_meta_component where ytenant_id='0' and own_module is not null and own_module not in('','null','NULL') order by own_module";
        
        String strBizObjAsComponentSQL = "select a.id,null as biz_model,a.display_name,null as help,a.name,null as namespace,lower(b.own_module) as own_module,null as version,null as version_type"
                + strOtherSQL2 + " from md_biz_obj a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri"
                + " where a.ytenant_id='0' and b.own_module is not null and a.code in(select biz_object_code from md_meta_class where ytenant_id='0')";
        
        String strClassSQL = "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.clazz.value()
                + " as class_type,b.id as component_id"
                + ",a.table_name as table_name,a.display_name,'' as full_classname,null as help,null as key_attribute,a.name,null as own_module,null as primary_class"
                + ",null as ref_model_name,null as return_type,null as version_type,c.id as biz_object_id,c.main_entity,d.id as main_class_id" + strOtherSQL2
                + " from md_meta_class a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri"
                + " left join md_biz_obj c on c.ytenant_id='0' and a.biz_object_code=c.code left join md_meta_class d on d.ytenant_id='0' and c.main_entity=d.uri"
                + " where a.ytenant_id='0' and a.meta_component_uri is not null"
                + " and b.own_module is not null and b.own_module not in('','null','NULL') order by a.meta_component_uri";
        
        String strEnumAsClass = "select a.id,null as accessor_classname,null as authen,null as biz_itf_imp_classname," + ClassVO.ClassType.enumeration.value()
                + " as class_type,b.id as component_id,null as table_name"
                + ",a.display_name,null as full_classname,null as help,null as key_attribute,null as model_type,a.name,null as own_module,null as primary_class"
                + ",null as ref_model_name,null as return_type,null as version_type" + strOtherSQL2
                + " from md_enumeration a left join md_meta_component b on b.ytenant_id='0' and a.meta_component_uri=b.uri"
                + " where a.ytenant_id='0' and a.uri in (select enumeration_uri from md_enumeration_literal where ytenant_id='0')";
        
        String strPropertySQL = "select a.id,null as accessor_classname,0 as access_power,0 as access_power_group,length as attr_length,max_value as attr_max_value,min_value as attr_min_value"
                + ",0 as attr_sequence,0 as calculation,b.id as class_id,0 as custom_attr"
                + ",(case when ref_meta_class_uri is not null then (select id from md_meta_class where ytenant_id='0' and ref_meta_class_uri=uri)"
                + " when ref_enum_uri is not null then (select id from md_enumeration where ytenant_id='0' and ref_enum_uri=uri) else biz_type end) as data_type"
                + ",'' as data_type_sql,(case when ref_meta_class_uri is not null then 305 when ref_enum_uri is not null then 203 else 300 end) as data_type_style"
                + ",default_value,a.display_name,0 as dynamic_attr,null as dynamic_table,0 as fixed_length,null as help,0 as hidden,0 as key_prop,a.name,0 as not_serialize"
                + ",(case when a.is_nullable is null then '1' else a.is_nullable end) as nullable,precise,0 as read_only,ref_meta_class_uri as ref_model_name,null as version_type"
                + ",b.table_name as table_name" + strOtherSQL1 + " from md_attribute a left join md_meta_class b on a.object_uri=b.uri and b.ytenant_id='0'"
                + " where a.ytenant_id='0' and a.biz_type is not null and object_uri in(select uri from md_meta_class where ytenant_id='0')";
        
        String strEnumValueSQL = "select a.id,b.id as class_id,0 as enum_sequence,code as enum_value,a.name as name,0 as version_type" + strOtherSQL1
                + " from md_enumeration_literal a left join md_enumeration b on a.enumeration_uri=b.uri and b.ytenant_id='0'"
                + " where a.ytenant_id='0' order by enumeration_uri,code";
        
        Properties dbPropSourceMd = new Properties();
        
        String strUrl = Context.getInstance().getSetting("md.jdbc.url", Context.getInstance().getSetting("jdbc.url"));
        dbPropSourceMd.setProperty("jdbc.url", strUrl.replace("${schema}", "iuap_metadata_base"));
        dbPropSourceMd.setProperty("jdbc.user", Context.getInstance().getSetting("md.jdbc.user", Context.getInstance().getSetting("jdbc.user")));
        dbPropSourceMd.setProperty("jdbc.driver", Context.getInstance().getSetting("md.jdbc.driver", Context.getInstance().getSetting("jdbc.driver")));
        dbPropSourceMd.setProperty("jdbc.password", Context.getInstance().getSetting("md.jdbc.password", Context.getInstance().getSetting("jdbc.password")));
        
        try (SQLExecutor sqlExecutorSource = new SQLExecutor(dbPropSourceMd))
        {
            syncMetaData(sqlExecutorSource, strModuleSQL, strComponentSQL, strBizObjAsComponentSQL, strClassSQL, strPropertySQL, strEnumAsClass,
                    strEnumValueSQL);
        }
        
        Logger.getLogger().end("sync md metadata in bip");
    }
}
