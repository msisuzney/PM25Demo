package com.msisuzney.pm25phone;

/**
 * Created by chenxin.
 * Date: 2017/10/13.
 * Time: 9:59.
 */

public class PMDataMessageEvent {

    private int pm1_0;
    private int pm2_5;

    public PMDataMessageEvent(int pm1_0, int pm2_5) {
        this.pm1_0 = pm1_0;
        this.pm2_5 = pm2_5;
    }

    public int getPm1_0() {
        return pm1_0;
    }

    public void setPm1_0(int pm1_0) {
        this.pm1_0 = pm1_0;
    }

    public int getPm2_5() {
        return pm2_5;
    }

    public void setPm2_5(int pm2_5) {
        this.pm2_5 = pm2_5;
    }
}
