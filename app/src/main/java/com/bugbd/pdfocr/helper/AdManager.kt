package com.bugbd.pdfocr.helper

import android.app.Activity
import android.content.Context
import com.bugbd.pdfocr.local_bd.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.TimeUnit

class AdManager(private val context: Context) {
    private val preferenceManager = PreferenceManager(context)
    private var mInterstitialAd: InterstitialAd? = null

    companion object {
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
        private const val LAST_INTERSTITIAL_TIME = "last_interstitial_time"
        private val INTERVAL_MILLIS = TimeUnit.HOURS.toMillis(2)
    }

    fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    fun showInterstitialAdWithLogic(activity: Activity, onAdDismissed: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        val lastShowTime = preferenceManager.get(LAST_INTERSTITIAL_TIME, 0L, Long::class)

        if (currentTime - lastShowTime >= INTERVAL_MILLIS) {
            if (mInterstitialAd != null) {
                mInterstitialAd?.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        mInterstitialAd = null
                        preferenceManager.set(LAST_INTERSTITIAL_TIME, System.currentTimeMillis())
                        loadInterstitialAd()
                        onAdDismissed()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                        mInterstitialAd = null
                        onAdDismissed()
                    }
                }
                mInterstitialAd?.show(activity)
            } else {
                loadInterstitialAd()
                onAdDismissed()
            }
        }
    else {
            onAdDismissed()
        }
    }
}