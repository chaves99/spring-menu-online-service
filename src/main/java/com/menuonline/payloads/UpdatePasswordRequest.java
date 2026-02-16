package com.menuonline.payloads;

public record UpdatePasswordRequest(String currentPassword, String newPassword) {
}
