package com.rnbiometrics;

import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.security.Signature;
import java.security.SignatureException;

public class CreateSignatureCallback extends BiometricPrompt.AuthenticationCallback {
    private final Promise promise;
    private final String payload;

    public CreateSignatureCallback(Promise promise, String payload) {
        super();
        this.promise = promise;
        this.payload = payload;
    }

    @Override
    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED ) {
            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("success", false);
            resultMap.putString("error", BiometryErrors.CANCELLED_BY_USER);
            this.promise.resolve(resultMap);
        } else {
            this.promise.reject(String.valueOf(errorCode), errString.toString());
        }
    }

    @Override
    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        try {
            BiometricPrompt.CryptoObject cryptoObject = result.getCryptoObject();
            Signature cryptoSignature = null;
            if (cryptoObject != null) {
                cryptoSignature = cryptoObject.getSignature();
                if (cryptoSignature != null) {
                    cryptoSignature.update(this.payload.getBytes());

                    byte[] signed = cryptoSignature.sign();
                    String signedString = Base64.encodeToString(signed, Base64.DEFAULT);
                    signedString = signedString.replaceAll("\r", "").replaceAll("\n", "");

                    WritableMap resultMap = new WritableNativeMap();
                    resultMap.putBoolean("success", true);
                    resultMap.putString("signature", signedString);
                    promise.resolve(resultMap);
                }
            }
        } catch (NullPointerException e) {
            ExceptionLogger.log(e);
            promise.reject(BiometryErrors.ERROR_OBTAINING_SIGNATURE, "Error creating signature: " + e.getMessage());
        } catch (SignatureException e) {
            ExceptionLogger.log(e);
            promise.reject(BiometryErrors.ERROR_OBTAINING_SIGNATURE, "Error updating signature: " + e.getMessage());
        }
    }
}
