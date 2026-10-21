
package com.campuslocker.model;

public class AvailableStatus extends LockerStatus {

    @Override
    public String getStatusName() {
        return "Available";
    }

    @Override
    public String getStatusDetails() {
        return "Locker is available for assignment.";
    }
}
