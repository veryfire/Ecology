package weaver.interfaces.workflow.action.customaction;

import weaver.conn.RecordSet;
import weaver.formmode.setup.ModeRightInfo;
import weaver.general.BaseBean;
import weaver.soa.workflow.request.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

/**
 * 工具类
 */
public class InvoiceUtil extends BaseBean {
    /**
     * 建模模块ID
     */
    private String modeId;
    /**
     * 流程请求ID
     */
    private String requestId;

    /**
     * 插入数据到中间表
     *
     * @param invoiceModelList
     * @return
     */
    public synchronized List<String> insertBillData(List<InvoiceModel> invoiceModelList) {
        RecordSet recordSet = new RecordSet();//通过事务来操作
        List<String> result = isExitByInvoiceNumber(invoiceModelList);
        if (result.isEmpty()) {
            for (InvoiceModel invoiceModel : invoiceModelList) {
                String insertSql = "insert into uf_invoice(requestid,invoice_number,owner,fdate,money,status)" + "values('" +
                        invoiceModel.getRequestId() + "','" +
                        invoiceModel.getInvoice_number() + "','" +
                        invoiceModel.getOwner() + "','" +
                        invoiceModel.getFdate() + "','" +
                        invoiceModel.getMoney() + "','" +
                        invoiceModel.getStatus() + "')";
                try {
                    recordSet.executeSql(insertSql);

                    writeLog("data insert success,sqlstatment:" + insertSql, InvoiceUtil.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    writeLog("data insert error,data will rollback", InvoiceUtil.class);
                }
            }
            List<Integer> invoiceIds = getInvoiceIdByInvoiceNumber(invoiceModelList);
            //添加建模默认查看权限
            for (Integer integer : invoiceIds) {
                editModeInfoRight(integer);
            }
        }
        return result;
    }

    /**
     * 查询是否存在记录,返回数据库存在的记录
     *
     * @param invoiceModelList
     * @return
     */
    public List<String> isExitByInvoiceNumber(List<InvoiceModel> invoiceModelList) {
        List<String> list = new ArrayList<String>();
        for (InvoiceModel invoiceModel : invoiceModelList) {
            list.add(invoiceModel.getInvoice_number());
        }
        RecordSet recordSet = new RecordSet();
        String sql = "select invoice_number from uf_invoice where invoice_number in (" + getStrByList(list) + ")";
        recordSet.execute(sql);
        List<String> invoiceList = new ArrayList<String>();
        while (recordSet.next()) {
            invoiceList.add(recordSet.getString(1));
        }
        return invoiceList;
    }

    /**
     * 返回本次插入数据的数据id
     *
     * @param invoiceModelList
     * @return
     */
    public List<Integer> getInvoiceIdByInvoiceNumber(List<InvoiceModel> invoiceModelList) {
        List<Integer> idList = new ArrayList<Integer>();
        List<String> list = new ArrayList<String>();
        for (InvoiceModel invoiceModel : invoiceModelList) {
            list.add(invoiceModel.getInvoice_number());
        }
        RecordSet recordSet = new RecordSet();
        String sql = "select id from uf_invoice where invoice_number in (" + getStrByList(list) + ")";
        recordSet.execute(sql);
        while (recordSet.next()) {
            idList.add(recordSet.getInt(1));
        }
        return idList;
    }

    /**
     * @param list
     * @return
     */
    public String getStrByList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        StringBuffer sbf = new StringBuffer();
        for (int i = 0; i < list.size(); i++) {
            int len = list.size();
            if (i < len - 1) {
                sbf.append("'" + list.get(i) + "'").append(",");
            } else {
                sbf.append("'" + list.get(i) + "'");
            }
        }
        return sbf.toString();
    }

    /**
     * 根据流程请求id删除中间表发票数据
     *
     * @param requestid
     * @return true success,false fail
     */
    public boolean deleteBillByReuquestId(String requestid) {
        if ("".equals(requestid) || requestid == null) {
            return false;
        }
        RecordSet recordSet = new RecordSet();
        boolean result = recordSet.execute("delete from uf_invoice where requestid = '" + requestid + "'");
        writeLog("delete uf_invoice table data result:" + result);
        return result;
    }

    /**
     * 根据requestinfo返回主表map结构数据
     *
     * @param requestInfo
     * @return
     */
    public Map<String, String> getMainTable(RequestInfo requestInfo) {
        Map<String, String> mainMap = new HashMap<String, String>();
        Property[] propertys = requestInfo.getMainTableInfo().getProperty();
        for (Property property : propertys) {
            String fieldName = property.getName();
            String fieldValue = property.getValue();
            mainMap.put(fieldName, fieldValue);
        }
        return mainMap;
    }

    /**
     * 根据requestinfo返回所有明细表集合
     *
     * @param requestInfo
     * @return
     */
    public ArrayList<ArrayList<HashMap<String, String>>> getDetailTableDataMap(RequestInfo requestInfo) {
        ArrayList<ArrayList<HashMap<String, String>>> detailMaps = new ArrayList<ArrayList<HashMap<String, String>>>();
        DetailTable[] detailTables = requestInfo.getDetailTableInfo().getDetailTable();
        for (int i = 0; i < detailTables.length; i++) {
            ArrayList<HashMap<String, String>> detailMap = new ArrayList<HashMap<String, String>>();
            Row[] rows = detailTables[i].getRow();//第一个明细表，获得所有行
            for (int j = 0; j < rows.length; j++) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                Cell[] cells = rows[j].getCell();//明细表 明细行 明细列
                for (Cell cell : cells) {
                    String filedName = cell.getName();
                    String fieldValue = cell.getValue();
                    hashMap.put(filedName, fieldValue);
                    writeLog("明细表:" + (i + 1) + " ,\t\t 明细行：" + (j + 1) + " ,\t\t 字段名：" + filedName + ",\t\t 字段值：" + fieldValue);
                }
                detailMap.add(hashMap);
            }
            detailMaps.add(detailMap);
        }
        return detailMaps;
    }

    /**
     * @param sourceId 表单数据ID
     */
    public void editModeInfoRight(int sourceId) {
        ModeRightInfo modeRightInfo = new ModeRightInfo();
        modeRightInfo.editModeDataShare(1, this.getModeId(), sourceId);
        updateModeFormIdInfo(sourceId);
    }

    public void updateModeFormIdInfo(int sourceId) {
        String sql = "update uf_invoce set formmodeid='" + this.getModeId() + "',userid='1'," +
                " usertype='0', createdate='" + GetNowDate() + "', createtime='" + GetNowTime() + "' where id =" + sourceId;
        RecordSet recordSet = new RecordSet();
        recordSet.execute(sql);
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

    public int getModeId() {
        return modeId != "" ? Integer.parseInt(modeId) : this.getRequestId();
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
    }

    public int getRequestId() {
        return Integer.parseInt(requestId);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

}
