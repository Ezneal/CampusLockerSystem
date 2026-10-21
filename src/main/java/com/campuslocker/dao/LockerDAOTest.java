
package com.campuslocker;

import com.campuslocker.dao.LockerDAO;
import com.campuslocker.model.Locker;

public class LockerDAOTest {

    public static void main(String[] args) {
        LockerDAO lockerDAO = new LockerDAO();

        try {
            Locker locker = new Locker("L001", "Ground Floor");

            lockerDAO.addLocker(locker);
            System.out.println("Locker saved successfully!");

            System.out.println("\nLocker inventory:");

            for (Locker item : lockerDAO.getAllLockers()) {
                System.out.println(
                        item.getLockerNumber() + " - " +
                        item.getLocation() + " - " +
                        item.getStatus().getStatusName()
                );
            }

        } catch (Exception e) {
            System.out.println("Something went wrong:");
            System.out.println(e.getMessage());
        }
    }
}
