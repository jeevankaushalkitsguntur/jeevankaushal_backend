package com.kits.jklub.Util;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class OtpStore {

    private static final ConcurrentHashMap<String, OtpData> store = new ConcurrentHashMap<>();

    public static void saveOtp(String key, String otp) {
        store.put(key, new OtpData(otp, LocalDateTime.now().plusMinutes(5)));
    }

    public static boolean verifyOtp(String key, String otp) {
        OtpData data = store.get(key);

        if (data == null) return false;

        if (data.expiry.isBefore(LocalDateTime.now())) {
            store.remove(key);
            return false;
        }

        if (!data.otp.equals(otp)) return false;

        store.remove(key); // one-time use
        return true;
    }

    static class OtpData {
        String otp;
        LocalDateTime expiry;

        OtpData(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}