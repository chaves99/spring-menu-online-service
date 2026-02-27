package com.menuonline.payloads;

public record ResetPasswordRequest(String token, String newPassword, String email){}
