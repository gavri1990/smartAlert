package com.example.smartalert;

public class Contact
{
    private int contact_id;
    private String contact_name;
    private String contact_telephone;

    public Contact(int contact_id, String contact_name, String contact_telephone) //constructor 1
    {
        this.contact_id = contact_id;
        this.contact_name = contact_name;
        this.contact_telephone = contact_telephone;
    }

    public Contact(String contact_name, String contact_telephone) //constructor 2
    {
        this.contact_name = contact_name;
        this.contact_telephone = contact_telephone;
    }

    public int getContact_id() {
        return contact_id;
    }

    public void setContact_id(int contact_id) {
        this.contact_id = contact_id;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getContact_telephone() {
        return contact_telephone;
    }

    public void setContact_telephone(String contact_telephone) {
        this.contact_telephone = contact_telephone;
    }
}


