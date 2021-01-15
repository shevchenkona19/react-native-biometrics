package com.rnbiometrics;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricPrompt.AuthenticationCallback;
import androidx.biometric.BiometricPrompt.PromptInfo;
import androidx.fragment.app.FragmentActivity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by brandon on 4/5/18.
 */

public class ReactNativeBiometrics extends ReactContextBaseJavaModule {

    private interface BiometryType {
        String BIOMETRICS = "biometrics";
    }

    protected String biometricKeyAlias = "biometric_key";

    private KeyStore store;

    public ReactNativeBiometrics(ReactApplicationContext reactContext) {
        super(reactContext);
        try {
            store = KeyStore.getInstance("AndroidKeyStore");
            store.load(null);
        } catch (Exception e) {
            store = null;
            ExceptionLogger.log(e);
        }
    }

    @Override
    public String getName() {
        return "ReactNativeBiometrics";
    }

    @ReactMethod
    public void isSensorAvailable(Promise promise) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ReactApplicationContext reactApplicationContext = getReactApplicationContext();
                BiometricManager biometricManager = BiometricManager.from(reactApplicationContext);
                int canAuthenticate = biometricManager.canAuthenticate();
                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    WritableMap resultMap = new WritableNativeMap();
                    resultMap.putBoolean("available", true);
                    resultMap.putString("biometryType", BiometryType.BIOMETRICS);
                    promise.resolve(resultMap);
                } else {
                    WritableMap resultMap = new WritableNativeMap();
                    resultMap.putBoolean("available", false);

                    switch (canAuthenticate) {
                        case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                            resultMap.putString("error", BiometryErrors.ERROR_BIOMETRIC_NO_HARDWARE);
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                            resultMap.putString("error", BiometryErrors.ERROR_BIOMETRIC_HW_UNAVAILABLE);
                            break;
                        case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                            resultMap.putString("error", BiometryErrors.ERROR_BIOMETRIC_NONE_ENROLLED);
                            break;
                    }
                    promise.resolve(resultMap);
                }
            } else {
                WritableMap resultMap = new WritableNativeMap();
                resultMap.putBoolean("available", false);
                resultMap.putString("error", BiometryErrors.ANDROID_VERSION_UNSUPPORTED);
                promise.resolve(resultMap);
            }
    }

    @ReactMethod
    public void createKeys(Promise promise) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                deleteBiometricKey();
                KeyPairGenerator keyPairGenerator;
                keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore");
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(biometricKeyAlias, KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .setAlgorithmParameterSpec(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4))
                        .setUserAuthenticationRequired(true)
                        .build();
                keyPairGenerator.initialize(keyGenParameterSpec);

                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                PublicKey publicKey = keyPair.getPublic();
                byte[] encodedPublicKey = publicKey.getEncoded();
                String publicKeyString = Base64.encodeToString(encodedPublicKey, Base64.DEFAULT);
                publicKeyString = publicKeyString.replaceAll("\r", "").replaceAll("\n", "");

                WritableMap resultMap = new WritableNativeMap();
                resultMap.putString("publicKey", publicKeyString);
                promise.resolve(resultMap);
            } else {
                promise.reject(BiometryErrors.ANDROID_VERSION_UNSUPPORTED, "Cannot generate keys on android versions below 6.0");
            }
        } catch (NoSuchAlgorithmException e) {
            ExceptionLogger.log(e);
            promise.reject(BiometryErrors.CREATE_KEYS_EXCEPTION, ExceptionLogger.getExceptionMessage("RSA Algorithm is not found :("));
        } catch (NoSuchProviderException e) {
            ExceptionLogger.log(e);
            promise.reject(BiometryErrors.CREATE_KEYS_EXCEPTION, ExceptionLogger.getExceptionMessage("RSA Algorithm is not found :("));
        } catch (InvalidAlgorithmParameterException e) {
            ExceptionLogger.log(e);
            promise.reject(BiometryErrors.CREATE_KEYS_EXCEPTION, ExceptionLogger.getExceptionMessage("Check params for RSA algorithm"));
        }

    }

    @ReactMethod
    public void deleteKeys(Promise promise) {
        FunctionResult exists = doesBiometricKeyExist();
        if (exists.success && exists.result) {
            FunctionResult deleted = deleteBiometricKey();

            if (deleted.success && deleted.result) {
                WritableMap resultMap = new WritableNativeMap();
                resultMap.putBoolean("keysDeleted", true);
                promise.resolve(resultMap);
            } else {
                promise.reject(BiometryErrors.ERROR_DELETING_KEYS, ExceptionLogger.getExceptionMessage(deleted.reason));
            }
        } else {
            WritableMap resultMap = new WritableNativeMap();
            resultMap.putBoolean("keysDeleted", false);
            promise.resolve(resultMap);
        }
    }

    @ReactMethod
    public void createSignature(final ReadableMap params, final Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiThreadUtil.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (store == null) {
                                promise.reject(BiometryErrors.ERROR_INIT_KEY_STORE, ExceptionLogger.getExceptionMessage("Keystore did not initialized"));
                                return;
                            }
                            try {
                                String cancelButtomText = params.getString("cancelButtonText");
                                String promptMessage = params.getString("promptMessage");
                                String payload = params.getString("payload");

                                Signature signature = Signature.getInstance("SHA256withRSA");

                                PrivateKey privateKey = (PrivateKey) store.getKey(biometricKeyAlias, null);
                                signature.initSign(privateKey);

                                BiometricPrompt.CryptoObject cryptoObject = new BiometricPrompt.CryptoObject(signature);

                                AuthenticationCallback authCallback = new CreateSignatureCallback(promise, payload);
                                FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
                                if (fragmentActivity != null) {
                                    Executor executor = Executors.newSingleThreadExecutor();
                                    BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, authCallback);

                                    PromptInfo promptInfo = new PromptInfo.Builder()
                                            .setDeviceCredentialAllowed(false)
                                            .setNegativeButtonText(cancelButtomText)
                                            .setTitle(promptMessage)
                                            .build();
                                    biometricPrompt.authenticate(promptInfo, cryptoObject);
                                } else {
                                    promise.reject(BiometryErrors.ERROR_NO_ACTIVITY, "Error creating signature: activity is not in foreground");
                                }
                            } catch (UnrecoverableKeyException e) {
                                ExceptionLogger.log(e);
                                promise.reject(BiometryErrors.ERROR_UNRECOVERABLE_KEY, ExceptionLogger.getExceptionMessage("Key in the keystore cannot be recovered"));
                            } catch (NoSuchAlgorithmException e) {
                                ExceptionLogger.log(e);
                                promise.reject(BiometryErrors.CREATE_SIGNATURE_EXCEPTION, ExceptionLogger.getExceptionMessage("SHA256withRSA is not available"));
                            } catch (KeyStoreException e) {
                                ExceptionLogger.log(e);
                                promise.reject(BiometryErrors.CREATE_KEYS_EXCEPTION, ExceptionLogger.getExceptionMessage("Cannot read key from key store"));
                            } catch (InvalidKeyException e) {
                                ExceptionLogger.log(e);
                                promise.reject(BiometryErrors.CREATE_KEYS_EXCEPTION, ExceptionLogger.getExceptionMessage("Invalid key in keystore, try to regenerate it"));
                            }
                        }
                    });
        } else {
            promise.reject(BiometryErrors.ANDROID_VERSION_UNSUPPORTED, "Cannot generate keys on android versions below 6.0");
        }
    }

    @ReactMethod
    public void simplePrompt(final ReadableMap params, final Promise promise) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            UiThreadUtil.runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {
                                String cancelButtomText = params.getString("cancelButtonText");
                                String promptMessage = params.getString("promptMessage");

                                AuthenticationCallback authCallback = new SimplePromptCallback(promise);
                                FragmentActivity fragmentActivity = (FragmentActivity) getCurrentActivity();
                                if (fragmentActivity != null) {
                                    Executor executor = Executors.newSingleThreadExecutor();
                                    BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, executor, authCallback);

                                    PromptInfo promptInfo = new PromptInfo.Builder()
                                            .setDeviceCredentialAllowed(false)
                                            .setNegativeButtonText(cancelButtomText)
                                            .setTitle(promptMessage)
                                            .build();
                                    biometricPrompt.authenticate(promptInfo);
                                } else {
                                    promise.reject(BiometryErrors.ERROR_NO_ACTIVITY, "Error creating signature: activity is not in foreground");
                                }
                        }
                    });
        } else {
            promise.reject(BiometryErrors.ANDROID_VERSION_UNSUPPORTED, "Cannot display biometric prompt on android versions below 6.0");
        }
    }

    @ReactMethod
    public void biometricKeysExist(Promise promise) {
            FunctionResult doesBiometricKeyExist = doesBiometricKeyExist();
            if (doesBiometricKeyExist.success) {
                WritableMap resultMap = new WritableNativeMap();
                resultMap.putBoolean("keysExist", doesBiometricKeyExist.result);
                promise.resolve(resultMap);
            } else {
                promise.reject(BiometryErrors.ERROR_CHECK_KEYS_EXIST, ExceptionLogger.getExceptionMessage(doesBiometricKeyExist.reason));
            }
    }

    protected FunctionResult doesBiometricKeyExist() {
        if (store == null) {
            return new FunctionResult(false, "Key store did not initialized");
        }
        try {
            return new FunctionResult(true, store.containsAlias(biometricKeyAlias));
        } catch (KeyStoreException e) {
            ExceptionLogger.log(e);
            return new FunctionResult(false, "KeyStore exception: " + e.getMessage());
        }
    }
    protected FunctionResult deleteBiometricKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            keyStore.deleteEntry(biometricKeyAlias);
            return new FunctionResult(true, true);
        } catch (IOException e) {
            ExceptionLogger.log(e);
            return new FunctionResult(false, "IOException for keys deletion: " + e.getMessage());
        } catch (CertificateException e) {
            ExceptionLogger.log(e);
            return new FunctionResult(false, "Certificate exception: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            ExceptionLogger.log(e);
            return new FunctionResult(false, "No such algorithm exception: " + e.getMessage());
        } catch (KeyStoreException e) {
            ExceptionLogger.log(e);
            return new FunctionResult(false, "Key store exception: " + e.getMessage());
        }
    }

    private static class FunctionResult {
        final boolean success;
        final boolean result;
        @Nullable
        String reason;

        protected FunctionResult(boolean isSuccess, @NonNull String reason) {
            success = isSuccess;
            this.reason = reason;
            result = false;
        }

        protected FunctionResult(boolean isSuccess, boolean result) {
            success = isSuccess;
            this.result = result;
        }
    }

}
