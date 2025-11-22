package edu.udb.sv.vamosapp

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        FacebookSdk.setClientToken(getString(R.string.facebook_client_token))

        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(this)
    }
}
