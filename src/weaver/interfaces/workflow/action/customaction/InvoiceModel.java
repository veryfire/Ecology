package weaver.interfaces.workflow.action.customaction;

/**
 * 发票实体类
 */
public class InvoiceModel {
    /**
     * requestId 流程请求ID
     */
    private String requestId;
    /**
     * invoice_number 发票号
     */
    private String invoice_number;
    /**
     * owner 报销人
     */
    private String owner;
    /**
     * fdate 发票日期
     */
    private String fdate;
    /**
     * money 发票金额
     */
    private String money;
    /**
     * status 报销状态 1来表达创建 2来表达在途 3来表达归档
     */
    private String status;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInvoice_number() {
        return invoice_number;
    }

    public void setInvoice_number(String invoice_number) {
        this.invoice_number = invoice_number;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getFdate() {
        return fdate;
    }

    public void setFdate(String fdate) {
        this.fdate = fdate;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
