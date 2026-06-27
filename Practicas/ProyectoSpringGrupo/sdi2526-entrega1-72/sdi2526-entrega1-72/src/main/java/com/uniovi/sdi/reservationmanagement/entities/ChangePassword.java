package com.uniovi.sdi.reservationmanagement.entities;

public class ChangePassword {

    private String currentPassword;
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    @SuppressWarnings("unused")
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    @SuppressWarnings("unused")
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
