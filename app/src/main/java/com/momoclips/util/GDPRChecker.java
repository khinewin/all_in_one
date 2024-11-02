package com.momoclips.util;

import android.app.Activity;
import android.util.Log;

import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;


public class GDPRChecker {
    private static final String TAG = "GDPRChecker";
    private Activity activity;
    private ConsentInformation consentInformation;


    protected GDPRChecker(Activity activity) {
        this.activity = activity;
        this.consentInformation = UserMessagingPlatform.getConsentInformation(activity);
    }

    public GDPRChecker() {
    }

    public GDPRChecker withContext(Activity activity) {
        return new GDPRChecker(activity);
    }

    public void check() {
        initGDPR();
    }

    private void initGDPR() {
        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .setForceTesting(true)
                .build();

        ConsentRequestParameters parameters = new ConsentRequestParameters.Builder()
                //.setConsentDebugSettings(debugSettings) // comment this line for production it is just used for testing outside eea
                .setTagForUnderAgeOfConsent(false)
                .build();

        consentInformation.requestConsentInfoUpdate(activity, parameters,
                () -> {
                    if (consentInformation.isConsentFormAvailable()) {
                        loadForm();
                    }
                }, formError -> Log.e(TAG, "onFailedToUpdateConsentInfo: " + formError.getMessage()));
    }

    public void loadForm() {
        UserMessagingPlatform.loadConsentForm(activity, consentForm -> {
            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED || consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.UNKNOWN) {
                consentForm.show(activity, formError -> {
                    if (consentInformation.getConsentStatus() != ConsentInformation.ConsentStatus.OBTAINED) {
                        // App can start requesting ads.
                        loadForm();
                    }
                });
            }
        }, formError -> loadForm());
    }


}
