package weaver.interfaces.workflow.action.customaction;

import weaver.conn.RecordSetTrans;
import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存发票到中间表以及校验是否有重复数据
 * 如果有重复数据，则直接返回重复发票到流程页面
 */
public class CheckBillAction extends BaseBean implements Action {
    private String modeId;

    /**
     * 节点后ACTION操作类
     *
     * @param requestInfo
     * @return
     */
    @Override
    public String execute(RequestInfo requestInfo) {
        writeLog("================================进入节点后操作================================");
        String requestid = requestInfo.getRequestid();
        InvoiceUtil invoiceUtil = new InvoiceUtil();
        invoiceUtil.setModeId(this.getModeId());
        invoiceUtil.setRequestId(requestid);
        ArrayList<ArrayList<HashMap<String, String>>> detailMaps = invoiceUtil.getDetailTableDataMap(requestInfo);
        List<InvoiceModel> invoiceModelList = new ArrayList<InvoiceModel>();
        //主表map结构数据
        Map<String, String> mainMap = invoiceUtil.getMainTable(requestInfo);
        for (int i = 0; i < detailMaps.size(); i++) {
            ArrayList<HashMap<String, String>> detailTableMap = detailMaps.get(i);
            for (int j = 0; j < detailTableMap.size(); j++) {
                HashMap<String, String> detailRowMap = detailTableMap.get(j);
                InvoiceModel invoiceModel = new InvoiceModel();
                invoiceModel.setRequestId(requestid);// requestid 流程id
                invoiceModel.setInvoice_number(detailRowMap.get("invoice_number"));// invoice_number 发票号
                invoiceModel.setOwner(mainMap.get("owner"));// owner 报销人
                invoiceModel.setFdate(invoiceUtil.GetNowDate());// fdate 报销日期
                invoiceModel.setMoney(detailRowMap.get("money"));// money 发票金额
                invoiceModel.setStatus("1"); // status 报销状态 1来表达创建，2来表达在途，3来表达归档
                invoiceModelList.add(invoiceModel);
            }

        }
        //插入中间表数据
        List<String> result = invoiceUtil.insertBillData(invoiceModelList);

        if (!result.isEmpty()) {//如果有问题,则返回错误信息提示
            requestInfo.getRequestManager().setMessageid(requestid);
            String content = invoiceUtil.getStrByList(result);
            content.replace("'", "");
            requestInfo.getRequestManager().setMessagecontent("发票号重复,重复的发票为:" + content + ",请修改后重新提交!");
        }
        writeLog("================================节点后操作结束================================");
        return SUCCESS;
    }

    public String getModeId() {
        return modeId;
    }

    public void setModeId(String modeId) {
        this.modeId = modeId;
    }
}
