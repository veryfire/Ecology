package com.erp;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ErpInsert {
    public void Insert(String object_rrn, String material_id, String name, String qty, String date_approved, String vendor_id, String vendor_rrn, String formmodeid) {
        RecordSet rs = new RecordSet();
        String sql = "insert into  uf_erp_wlbh_qty (object_rrn ,material_id ,name,qty,date_approved,vendor_id,vendor_rrn,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime)" +
                "values('" + object_rrn + "','" + material_id + "','" + name + "','" + qty + "','" + date_approved + "','" + vendor_id + "','" + vendor_rrn + "'," +
                "'" + formmodeid + "','1','0','" + GetNowDate() + "','" + GetNowTime() + "')";
        rs.execute(sql);
        rs.execute("select max(id) mid from  uf_erp_wlbh_qty ");
        rs.next();
        String maxid = rs.getString("mid");
        ModeRightInfo info = new ModeRightInfo();
        info.editModeDataShare(Integer.parseInt("1"), Integer.parseInt(formmodeid), Integer.parseInt(maxid));

    }

    public void delete() {
        RecordSet rs = new RecordSet();
        String sql = "delete from uf_erp_wlbh_qty";
        rs.execute(sql);
    }

    public String GetNowDate() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        temp_str = sdf.format(dt);
        return temp_str;
    }

    public String GetNowTime() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        temp_str = sdf.format(dt);
        return temp_str;
    }

}
