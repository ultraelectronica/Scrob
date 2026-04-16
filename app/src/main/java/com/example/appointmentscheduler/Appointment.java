package com.example.appointmentscheduler;

public class Appointment {
    private int id;
    private String title;
    private String date;
    private String time;
    private String location;
    private String description;

    public Appointment(int id, String title, String date, String time, String location, String description) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.time = time;
        this.location = location;
        this.description = description;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
}
