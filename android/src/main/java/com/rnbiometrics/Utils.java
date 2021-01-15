package com.rnbiometrics;

import androidx.biometric.BiometricPrompt;

public class Utils {
    protected static String getErrorCode(int errorCodeBiometry) {
        switch (errorCodeBiometry) {
            case BiometricPrompt.ERROR_HW_UNAVAILABLE:
                return BiometryErrors.ERROR_BIOMETRIC_HW_UNAVAILABLE;
            case BiometricPrompt.ERROR_UNABLE_TO_PROCESS:
                return BiometryErrors.ERROR_UNABLE_TO_PROCESS;
            case BiometricPrompt.ERROR_TIMEOUT:
                return BiometryErrors.ERROR_TIMEOUT;
            case BiometricPrompt.ERROR_NO_SPACE:
                return BiometryErrors.ERROR_NO_SPACE;
            case BiometricPrompt.ERROR_CANCELED:
                return BiometryErrors.ERROR_CANCELED;
            case BiometricPrompt.ERROR_LOCKOUT:
                return BiometryErrors.ERROR_LOCKOUT;
            case BiometricPrompt.ERROR_VENDOR:
                return BiometryErrors.ERROR_VENDOR;
            case BiometricPrompt.ERROR_LOCKOUT_PERMANENT:
                return BiometryErrors.ERROR_LOCKOUT_PERMANENT;
            case BiometricPrompt.ERROR_USER_CANCELED:
                return BiometryErrors.ERROR_USER_CANCELED;
            case BiometricPrompt.ERROR_NO_BIOMETRICS:
                return BiometryErrors.ERROR_NO_BIOMETRICS;
            case BiometricPrompt.ERROR_HW_NOT_PRESENT:
                return BiometryErrors.ERROR_BIOMETRIC_NO_HARDWARE;
            case BiometricPrompt.ERROR_NEGATIVE_BUTTON:
                return BiometryErrors.ERROR_NEGATIVE_BUTTON;
            case BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL:
                return BiometryErrors.ERROR_NO_DEVICE_CREDENTIAL;
            default:
                return BiometryErrors.UNKNOWN_ERROR;
        }
    }
}
