package cn.veryfire.ctrip.schedule;

import cn.veryfire.ctrip.hrm.HrmSyn2Ctrip;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;
import weaver.general.BaseBean;
import weaver.interfaces.schedule.BaseCronJob;

public class SynHrmInfoSchedule extends BaseCronJob {
    private BaseBean baseBean = new BaseBean();
    @Override
    public void execute() {
        baseBean.writeLog("Start Syn Hrmresource:",SynHrmInfoSchedule.class);
        HrmSyn2Ctrip hrmSyn2Ctrip = new HrmSyn2Ctrip();
        hrmSyn2Ctrip.multipleEmployeeSync();
        baseBean.writeLog("End Syn Hrmresource:",SynHrmInfoSchedule.class);
    }
}
