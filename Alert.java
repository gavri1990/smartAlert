package com.example.smartalert;

public class Alert
{
    public String hazard;
    public String location;
    public String timeStamp;
    public String falseAlarm;

    public Alert(String hazard, String location, String timeStamp, String falseAlarm)
    {
        this.hazard = hazard;
        this.location = location;
        this.timeStamp = timeStamp;
        this.falseAlarm = falseAlarm;
    }

    public Alert(String hazard, String location, String timeStamp)
    {
        this.hazard = hazard;
        this.location = location;
        this.timeStamp = timeStamp;
    }
}
