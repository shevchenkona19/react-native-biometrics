package com.rnbiometrics;

import androidx.annotation.RestrictTo;

public interface BiometryErrors {
     String CANCELLED_BY_USER = "CANCELLED_BY_USER";
     String ERROR_OBTAINING_SIGNATURE = "ERROR_OBTAINING_SIGNATURE";

     String ERROR_BIOMETRIC_NO_HARDWARE = "ERROR_BIOMETRIC_NO_HARDWARE";
     String ERROR_BIOMETRIC_HW_UNAVAILABLE = "ERROR_BIOMETRIC_HW_UNAVAILABLE";
     String ERROR_BIOMETRIC_NONE_ENROLLED = "ERROR_BIOMETRIC_NONE_ENROLLED";

     String ANDROID_VERSION_UNSUPPORTED = "ANDROID_VERSION_UNSUPPORTED";
     String CREATE_KEYS_EXCEPTION = "CREATE_KEYS_EXCEPTION";

     String ERROR_DELETING_KEYS = "ERROR_DELETING_KEYS";
     String ERROR_INIT_KEY_STORE = "ERROR_INIT_KEY_STORE";
     String ERROR_NO_ACTIVITY = "ERROR_NO_ACTIVITY";
     String ERROR_UNRECOVERABLE_KEY = "ERROR_UNRECOVERABLE_KEY";
     String CREATE_SIGNATURE_EXCEPTION = "CREATE_SIGNATURE_EXCEPTION";
     String ERROR_CHECK_KEYS_EXIST = "ERROR_CHECK_KEYS_EXIST";

     /**
      * Error state returned when the sensor was unable to process the current image.
      */
     String ERROR_UNABLE_TO_PROCESS = "ERROR_UNABLE_TO_PROCESS";

     /**
      * Error state returned when the current request has been running too long. This is intended to
      * prevent programs from waiting for the biometric sensor indefinitely. The timeout is platform
      * and sensor-specific, but is generally on the order of 30 seconds.
      */
     String ERROR_TIMEOUT = "ERROR_TIMEOUT";

     /**
      * Error state returned for operations like enrollment; the operation cannot be completed
      * because there's not enough storage remaining to complete the operation.
      */
     String ERROR_NO_SPACE = "ERROR_NO_SPACE";

     /**
      * The operation was canceled because the biometric sensor is unavailable. For example, this may
      * happen when the user is switched, the device is locked or another pending operation prevents
      * or disables it.
      */
     String ERROR_CANCELED = "ERROR_CANCELED";

     /**
      * The operation was canceled because the API is locked out due to too many attempts.
      * This occurs after 5 failed attempts, and lasts for 30 seconds.
      */
     String ERROR_LOCKOUT = "ERROR_LOCKOUT";

     /**
      * Hardware vendors may extend this list if there are conditions that do not fall under one of
      * the above categories. Vendors are responsible for providing error strings for these errors.
      * These messages are typically reserved for internal operations such as enrollment, but may be
      * used to express vendor errors not otherwise covered. Applications are expected to show the
      * error message string if they happen, but are advised not to rely on the message id since they
      * will be device and vendor-specific
      */
     String ERROR_VENDOR = "ERROR_VENDOR";

     /**
      * The operation was canceled because ERROR_LOCKOUT occurred too many times.
      * Biometric authentication is disabled until the user unlocks with strong authentication
      * (PIN/Pattern/Password)
      */
     String ERROR_LOCKOUT_PERMANENT = "ERROR_LOCKOUT_PERMANENT";

     /**
      * The user canceled the operation. Upon receiving this, applications should use alternate
      * authentication (e.g. a password). The application should also provide the means to return to
      * biometric authentication, such as a "use <biometric>" button.
      */
     String ERROR_USER_CANCELED = "ERROR_USER_CANCELED";

     /**
      * The user does not have any biometrics enrolled.
      */
     String ERROR_NO_BIOMETRICS = "ERROR_NO_BIOMETRICS";

     /**
      * The user pressed the negative button.
      */
     String ERROR_NEGATIVE_BUTTON = "ERROR_NEGATIVE_BUTTON";

     /**
      * The device does not have pin, pattern, or password set up.
      */
     String ERROR_NO_DEVICE_CREDENTIAL = "ERROR_NO_DEVICE_CREDENTIAL";

     /**
      * This error indicates that error code from Biometrics API is not declared. Such case is almost impossible
      */
     String UNKNOWN_ERROR = "UNKNOWN_ERROR";
}
