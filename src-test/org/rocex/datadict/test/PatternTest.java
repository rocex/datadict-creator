package org.rocex.datadict.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PatternTest
{
    private final String strTableFilterPattern = "(^[0-9\\-\\(_][0-9a-z ()=\"-%_]+)|\\w+([0-9_.]+$)|^iform_\\w+|^bpm_[0-9_]+$|^del_\\w+|^intelliv[0-9a-z]+\\w+|^iuap_extend_\\w+|\\w+(_del|_copy){1}$|^t_\\w+|^tb_sheet\\w+";

    private final Pattern patternTableFilter = Pattern.compile(strTableFilterPattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);

    public void testPattern()
    {
        List<String> listTableName2 = new ArrayList<>();
        List<String> listTableName3 = new ArrayList<>();

        String strContents[] = TestData.strContent.split("\n");

        for (String strContent : strContents)
        {
            strContent = strContent.trim();

            boolean blMatches = patternTableFilter.matcher(strContent).matches();

            String string = (blMatches ? "×" : "√") + " " + strContent;

            if (blMatches)
            {
                listTableName2.add(string);
            }
            else
            {
                listTableName3.add(string);
            }
        }

        listTableName2.addAll(listTableName3);

        try
        {
            Files.write(Paths.get("C:/table-name.txt"), listTableName2);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}

class TestData
{
    // @formatter:off
    static final String strContent = """
        (case when (5467=5467) then sleep(5) else 5467 end)
        (case when 7617=1865 then 7617 else null end)
        (select concat(0x7178707671,(elt(2151=2151,1)),0x717a6a7871))
        -1013% or 1795=1795-
        -3507
        0000l59fu91df3pj520000_entity081new3freect_2
        222
        2301_billforeignkey
        a1111_managementclass
        aa
        aa_attributestructure
        aa_auth_usertorole
        aa_barcoderule
        auth_manager_refer
        auth_manager_refer_history_20220322131851
        b1d58449a7f85da585072410ea9e_1
        buy_order_info
        COLLATIONS
        COLLATION_CHARACTER_SET_APPLICABILITY
        db
        del_20230324_aa_billcode_mapping
        del_incre_ustock_taskparam
        del_incre_yts_task_lock
        del_tmp
        del_tmp_20180919
        df1485848a4d62930c36fda9e42e_2
        download
        dv_user_search_record_statistics
        ec_label
        nonces
        notice
        noticeclass
        null_1
        null_2
        num
        num2
        wx_user
        wx_user_log
        w_px013044
        x$host_summary
        x$host_summary_by_file_io
        x$host_summary_by_file_io_type
        zzztest
        z_tance_log
        这是描述信息bd_staff_job_kq98okpvdlmv6w_330897
        """;
    // @formatter:on
}
