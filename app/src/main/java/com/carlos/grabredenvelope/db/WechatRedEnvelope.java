package com.carlos.grabredenvelope.db;




public class WechatRedEnvelope {
    //    @Id(autoincrement = true)
    private Long id;
    private long time = System.currentTimeMillis();
    private String count = "";

    //    @Generated(hash = 1534010414)
    public WechatRedEnvelope() {

    }
    //    @Generated(hash = 1534010414)
    public WechatRedEnvelope(Long id, long time, String count) {
        this.id = id;
        this.time = time;
        this.count = count;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTime() {
        return this.time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getCount() {
        return this.count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
