package com.example.back.accessibilitytest

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.e("starting...", "sdfe")
        if(!isAccessibilityEnabled("com.example.back.accessibilitytest/.MyAccessibilityService")) {
            Log.e("showing...", "sdfe")
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, 0)
        }
        Log.e("started", "sdfe")
    }

    fun Context.isAccessibilityEnabled(id: String): Boolean {
        Log.e("isAccessibilityEnabled", "checking")
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        Log.e("isAccessibilityEnabled", "$am")
        val runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)
        Log.e("isAccessibilityEnabled", "$runningServices")

        runningServices.forEach {
            Log.e("service", " ${it.id}")
        }
        return runningServices.any {
            Log.e("service", " $it")
            id == it.id }
    }
}
