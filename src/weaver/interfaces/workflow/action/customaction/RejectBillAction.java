package weaver.interfaces.workflow.action.customaction;

import weaver.general.BaseBean;
import weaver.interfaces.workflow.action.Action;
import weaver.soa.workflow.request.RequestInfo;

/**
 * 流程退回时执行删除中间表的操作
 */
public class RejectBillAction extends BaseBean implements Action {
    @Override
    public String execute(RequestInfo requestInfo) {
        String requestid = requestInfo.getRequestid();
        InvoiceUtil invoiceUtil = new InvoiceUtil();
        boolean result = invoiceUtil.deleteBillByReuquestId(requestid);//删除中间表数据
        if (result) {
            return SUCCESS;
        } else {
            return FAILURE_AND_CONTINUE;
        }
    }
}
