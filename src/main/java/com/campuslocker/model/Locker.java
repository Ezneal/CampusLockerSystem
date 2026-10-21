
package com.campuslocker.model;

public class Locker {

    private String lockerNumber;
    private String location;
    private LockerStatus status;

    public Locker(String lockerNumber, String location) {
        this.lockerNumber = lockerNumber;
        this.location = location;
        this.status = new AvailableStatus();
    }

    public String getLockerNumber() {
        return lockerNumber;
    }

    public void setLockerNumber(String lockerNumber) {
        this.lockerNumber = lockerNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LockerStatus getStatus() {
        return status;
    }

    public void setStatus(LockerStatus status) {
        this.status = status;
    }

    public boolean isAvailable() {
        return status instanceof AvailableStatus;
    }

    public String getStatusDetails() {
        return status.getStatusDetails();
    }
}
