package com.erp;

import com.erp.ErpInsert;
import weaver.conn.RecordSetDataSource;
import weaver.general.BaseBean;
import weaver.general.Util;
import weaver.interfaces.schedule.BaseCronJob;

public class ErpInsertSchedule extends BaseCronJob {
    private static BaseBean bb = new BaseBean();
    com.erp.ErpInsert ErpInsert = new ErpInsert();

    @Override
    public void execute() {
        //new BaseBean().writeLog("进入2222225555555555555555");
        RecordSetDataSource erp = new RecordSetDataSource("erp");
        ErpInsert.delete();
        String ErpString = " select m.object_rrn,m.material_id,m.name,t1.qty,t1.date_approved,t1.vendor_id,t1.vendor_rrn from( " +
                " select to_char(im.date_approved,'YYYY') date_approved,iml.material_rrn,sum(iml.qty_movement) qty, " +
                " im.vendor_id,im.vendor_rrn from inv_movement im,inv_movement_line iml " +
                " where im.org_rrn =139420 and im.is_active ='Y' " +
                " and im.doc_type = 'PIN' and im.doc_status in('APPROVED','COMPLETED') " +
                " and im.object_rrn = iml.movement_rrn " +
                " group by to_char(im.date_approved,'YYYY'),iml.material_rrn,im.vendor_id,im.vendor_rrn " +
                " ) t1,pdm_material m where t1.material_rrn = m.object_rrn ";
        //new BaseBean().writeLog("ErpString========"+ErpString);
        erp.execute(ErpString);
        while (erp.next()) {
            String object_rrn = Util.null2String(erp.getString("object_rrn"));
            String material_id = Util.null2String(erp.getString("material_id"));
            String name = Util.null2String(erp.getString("name"));
            String qty = Util.null2String(erp.getString("qty"));
            String date_approved = Util.null2String(erp.getString("date_approved"));
            String vendor_id = Util.null2String(erp.getString("vendor_id"));
            String vendor_rrn = Util.null2String(erp.getString("vendor_rrn"));
            ErpInsert.Insert(object_rrn, material_id, name, qty, date_approved, vendor_id, vendor_rrn, "561");

        }
    }


}
