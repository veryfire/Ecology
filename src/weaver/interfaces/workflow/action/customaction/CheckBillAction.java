package weaver.interfaces.workflow.action.customaction;

import weaver.conn.RecordSetTrans;
import weaver.general.BaseBean;
import weaver.general.Util;
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
        InvoiceUtil invoiceUtil = new InvoiceUtil();
        String requestid = requestInfo.getRequestid();
        Map<String, String> mainMap = invoiceUtil.getMainTable(requestInfo);
        String owner = Util.null2String(mainMap.get("owner"));
        invoiceUtil.setModeId(this.getModeId());
        invoiceUtil.setRequestId(requestid);

        int tableIndex = 0;
        String distinguish = Util.null2String(mainMap.get("distinguish"));
        writeLog("distinguish:" + distinguish);
        if (!"".equals(distinguish)) {
            tableIndex = Integer.parseInt(distinguish) - 1;
        }

        ArrayList<ArrayList<HashMap<String, String>>> detailMaps = invoiceUtil.getDetailTableDataMap(requestInfo,tableIndex);

        List<InvoiceModel> invoiceModelList = new ArrayList<InvoiceModel>();
        List<InvoiceModel> invoiceModelListAndInvoice_numberIsNull = new ArrayList<InvoiceModel>();
        //主表map结构数据
        for (int i = 0; i < detailMaps.size(); i++) {
            ArrayList<HashMap<String, String>> detailRowMap = detailMaps.get(i);
            for (int j = 0; j < detailRowMap.size(); j++) {
                HashMap<String, String> detailCellMap = detailRowMap.get(j);
                String invoice_number = Util.null2String(detailCellMap.get("invoice_number"));
                String purposr = Util.null2String(detailCellMap.get("purpose"));
                String money = Util.null2String(detailCellMap.get("money"));
                if (!"".equals(invoice_number)) {
                    InvoiceModel invoiceModel = new InvoiceModel();
                    invoiceModel.setRequestId(requestid);// requestid 流程id
                    invoiceModel.setInvoice_number(invoice_number);// invoice_number 发票号
                    invoiceModel.setOwner(owner);// owner 报销人
                    invoiceModel.setFdate(invoiceUtil.GetNowDate());// fdate 报销日期
                    invoiceModel.setMoney(money);// money 发票金额
                    invoiceModel.setStatus("0"); // status 报销状态 0来表达在途,0来表达归档
                    invoiceModel.setPurpose(purposr);
                    invoiceModelList.add(invoiceModel);
                } else {
                    InvoiceModel invoiceModelAndNull = new InvoiceModel();
                    invoiceModelAndNull.setRequestId(requestid);
                    invoiceModelAndNull.setStatus("0");
                    invoiceModelAndNull.setPurpose(purposr);
                    invoiceModelAndNull.setFdate(invoiceUtil.GetNowDate());
                    invoiceModelAndNull.setInvoice_number("");
                    invoiceModelAndNull.setOwner(owner);
                    invoiceModelAndNull.setMoney(money);
                    invoiceModelListAndInvoice_numberIsNull.add(invoiceModelAndNull);
                }
            }

        }
        //插入发票编号为空的数据
        if (invoiceModelListAndInvoice_numberIsNull.size() > 0) {
            invoiceUtil.insertBillDataAndInvoiceNumberIsNull(invoiceModelListAndInvoice_numberIsNull);
        }

        //插入中间表数据
        if (invoiceModelList.size() > 0) {
            List<String> result = invoiceUtil.insertBillData(invoiceModelList);
            if (!result.isEmpty()) {//如果有问题,则返回错误信息提示
                requestInfo.getRequestManager().setMessageid(requestid);
                String content = invoiceUtil.getStrByList(result);
                content.replace("'", "");
                requestInfo.getRequestManager().setMessagecontent("发票号重复,重复的发票为:" + content + ",请修改后重新提交!");
            }
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
