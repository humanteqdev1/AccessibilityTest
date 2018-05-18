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

        Log.e("234", "2032-3094520")

        if(!isAccessibilityEnabled("com.example.back.accessibilitytest/.MyAccessibilityService")) {
            val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, 0)
        }
    }

    fun Context.isAccessibilityEnabled(id: String): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val runningServices = am.getEnabledAccessibilityServiceList(AccessibilityEvent.TYPES_ALL_MASK)

        runningServices.forEach {
            Log.e("service", " ${it.id}")
        }
        return runningServices.any {
            Log.e("service", " $it")
            id == it.id }
    }
}
