package com.canature.schedule;

import weaver.conn.RecordSetDataSource;
import weaver.interfaces.schedule.BaseCronJob;

public class SynWlbhOrderCount extends BaseCronJob {
    @Override
    public void execute() {


    }

    public void synOrderCount() {
        RecordSetDataSource rs = new RecordSetDataSource("erp");
        String sql = "";
        rs.execute(sql);

    }

    public void getOAwLBH() {
        String sql = "select t.bumen, t.zdrq zdrq, t1.wlbh,replace(t1.wlmc,'&nbsp;','') wlmc" +
                "  from formtable_main_70 t, formtable_main_70_dt1 t1" +
                " where t.id = t1.mainid" +
                "   and t1.wllb1 = 1" +
                "   and t1.wlbh is not null" +
                " group by t.bumen, t.zdrq, t1.wlbh,t1.wlmc" +
                " order by t.zdrq";
    }


}
