
package com.campuslocker.model;

import java.time.LocalDateTime;

public abstract class LockerStatus {

    private LocalDateTime dateUpdated;

    public LockerStatus() {
        this.dateUpdated = LocalDateTime.now();
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    protected void updateDate() {
        this.dateUpdated = LocalDateTime.now();
    }

    public abstract String getStatusName();

    public abstract String getStatusDetails();
}
