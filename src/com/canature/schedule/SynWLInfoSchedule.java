package com.canature.schedule;

import weaver.conn.RecordSet;
import weaver.formmode.data.ModeDataIdUpdate;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.general.StaticObj;
import weaver.interfaces.datasource.DataSource;
import weaver.interfaces.schedule.BaseCronJob;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

public class SynWLInfoSchedule extends BaseCronJob {
    private static BaseBean bb = new BaseBean();

    @Override
    public void execute() {
        bb.writeLog("erp出库数据同步开始:", SynWLInfoSchedule.class);
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String today = sdf.format(date);
        Date lastDay = new Date(date.getTime() - 86400000L);
        String yesterday = sdf.format(lastDay);
        boolean executeSyn = executeSynWl(yesterday, today);
        bb.writeLog("erp出库数据同步成功:" + executeSyn, SynWLInfoSchedule.class);
    }

    public boolean executeSynWl(String startDate, String endDate) {
        java.sql.Connection conn = null;
        Statement statement = null;
        DataSource ds = (DataSource) StaticObj.getServiceByFullname(("datasource.erp"),
                        weaver.interfaces.datasource.DataSource.class);
        conn = ds.getConnection();
        try {
            statement = conn.createStatement();
            String sql = "SELECT LINE.MOVEMENT_ID AS ckdbh,line.material_id AS wlbh,sum(line.qty_movement) AS cksl,to_char(LINE.created,'yyyy-mm-dd') AS rq "
                    + "FROM INV_MOVEMENT_LINE LINE WHERE LINE.IS_ACTIVE='Y' "
                    + "AND LINE.LINE_STATUS IN ('APPROVED','COMPLETED') "
                    + "AND LINE.created>=to_date('"
                    + startDate
                    + "','YYYY-MM-DD') "
                    + "AND LINE.created<=to_date('"
                    + endDate
                    + "','YYYY-MM-DD') "
                    + "GROUP BY LINE.MOVEMENT_ID,line.material_id,to_char(LINE.created,'yyyy-mm-dd')";
            bb.writeLog("获取erp数据的sql:" + sql, SynWLInfoSchedule.class);
            ResultSet rs = statement.executeQuery(sql);
            List<Wlbh> list = new ArrayList<Wlbh>();
            while (rs.next()) {
                Wlbh wlbhs = new Wlbh();
                String ckbh = rs.getString(1);
                String wlbh = rs.getString(2);
                String ckl = rs.getString(3);
                String createDate = rs.getString(4);
                wlbhs.setCkbh(ckbh);
                wlbhs.setWlbh(wlbh);
                wlbhs.setCkl(ckl);
                wlbhs.setCreateDate(createDate);
                list.add(wlbhs);
            }
            boolean exits = getFormExits(startDate);
            if (!exits) {
                bb.writeLog("OA表中不存在数据", SynWLInfoSchedule.class);
                updateOAWlb(list);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public void updateOAWlb(List<Wlbh> list) {
        // /formmode/view/AddFormMode.jsp?modeId=1761&formId=-400&type=1
        String tableName = "uf_xzwlnxltj";
        for (Wlbh wlbh : list) {
            Integer billId = getBillId(tableName, 1761);
            Map<String, String> map = new HashMap<String, String>();
            map.put("ckbh", wlbh.getCkbh());
            map.put("wlbh", wlbh.getWlbh());
            map.put("ckl", wlbh.getCkl());
            map.put("rq", wlbh.getCreateDate());
            updateModeDataByBillId(tableName, map, billId);
            editModeInfoRight(1, 1761, billId);
        }

    }

    /**
     * 判断表中是否已经存在数据
     *
     * @param start
     * @return
     */
    public boolean getFormExits(String start) {
        RecordSet rs = new RecordSet();
        String sqlString = "select id from uf_xzwlnxltj where rq like '%"
                + start + "%'";
        rs.execute(sqlString);
        if (rs.next()) {
            return true;
        }
        return false;
    }

    public Integer getBillId(String billtablename, int formmodeid) {
        int billid = 0;
        String uuid = UUID.randomUUID().toString().replace("-", "");
        RecordSet rs = new RecordSet();
        RecordSet rs1 = new RecordSet();
        String sql = "insert into "
                + billtablename
                + "(mkuuid,formmodeid,modedatacreater,modedatacreatertype,modedatacreatedate,modedatacreatetime)"
                + "values('" + uuid + "','" + formmodeid + "','1','0','"
                + GetNowDate() + "','" + GetNowTime() + "')";
        rs.execute(sql);
        rs1.execute("select id from " + billtablename + " where mkuuid='"
                + uuid + "'");
        if (rs1.next()) {
            billid = rs1.getInt(1);
        }
        return billid;
    }

    /**
     * 获取插入建模表的id
     *
     * @param billtablename 建模表名称
     * @param formmodeid    模块ID
     * @param userid        用户ID 1
     * @param usertype      用户类型1
     * @param createdate    创建日期
     * @param createtime    创建时间
     * @return 返回新建建模的ID
     */

    public int getModeDataIdUpdate(String billtablename, int formmodeid,
                                   int userid, int usertype, String createdate, String createtime) {
        ModeDataIdUpdate instance = ModeDataIdUpdate.getInstance();
        int billId = instance.getModeDataNewId(billtablename, formmodeid,
                userid, usertype, createdate, createtime);
        return billId;
    }

    /**
     * 根据表名更新表单数据信息
     *
     * @param tableName 表名
     * @param updateMap map结构的数据
     * @return
     */
    public boolean updateModeDataByBillId(String tableName,
                                          Map<String, String> updateMap, int billId) {
        boolean flag = false;
        if (updateMap != null && updateMap.size() > 0) {
            Set<String> keySet = updateMap.keySet();
            StringBuffer sb = new StringBuffer();
            sb.append("update ").append(tableName).append(" set ");
            int i = 0;
            for (String key : keySet) {
                String fieldName = key;
                String fieldValue = updateMap.get(key);
                i++;
                if (i == updateMap.size()) {
                    sb.append(fieldName).append("='").append(fieldValue)
                            .append("'");
                } else {
                    sb.append(fieldName).append("='").append(fieldValue)
                            .append("',");
                }
            }
            sb.append(" where id = ").append(billId);

            RecordSet rs = new RecordSet();
            flag = rs.execute(sb.toString());
        }
        return flag;
    }

    /**
     * 添加建模表数据默认权限
     *
     * @param creteor  创建人ID
     * @param modeId   模块ID
     * @param sourceId 表单数据ID
     */
    public void editModeInfoRight(int createrid, int modeId, int sourceId) {
        ModeRightInfo modeRightInfo = new ModeRightInfo();
        modeRightInfo.editModeDataShare(createrid, modeId, sourceId);
    }

    /**
     * 获得当前日期
     *
     * @return
     */
    public String GetNowDate() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        temp_str = sdf.format(dt);
        return temp_str;
    }

    /**
     * 获得当前时间
     *
     * @return
     */
    public String GetNowTime() {
        String temp_str = "";
        Date dt = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        temp_str = sdf.format(dt);
        return temp_str;
    }
}
