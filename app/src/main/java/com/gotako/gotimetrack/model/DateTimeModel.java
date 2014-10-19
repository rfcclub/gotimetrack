package com.gotako.gotimetrack.model;

public class DateTimeModel {
    private long id;
    private String status;
    private long time;


    public DateTimeModel(long id, String status, long time) {
        super();
        this.id = id;
        this.status = status;
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }


}
