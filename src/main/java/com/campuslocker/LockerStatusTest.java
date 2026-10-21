
package com.campuslocker;

import com.campuslocker.dao.LockerDAO;
import com.campuslocker.model.Locker;
import java.sql.SQLException;

public class LockerStatusTest {

    public static void main(String[] args) {
        LockerDAO lockerDAO = new LockerDAO();

        try {
            for (Locker locker : lockerDAO.getAllLockers()) {
                System.out.println(
                    "Locker: " + locker.getLockerNumber()
                    + " | Status: "
                    + locker.getStatus().getStatusName()
                    + " | Details: "
                    + locker.getStatusDetails()
                );
            }
        } catch (SQLException e) {
            System.out.println("Test failed: " + e.getMessage());
        }
    }
}
