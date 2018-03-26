package com.example.back.accessibilitytest

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Created by back on 26.03.18.
 */
class MyAccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
        Log.e("MyAccess", "interrupted")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        Log.e("MyAccess", "----------------------------------")
//        Log.e("MyAccess", "event: $event")
//        Log.e("MyAccess", "type: ${event?.eventType}")
//        Log.e("MyAccess", "action: ${event?.action}")
//        Log.e("MyAccess", "eventTime: ${event?.eventTime}")
//        Log.e("MyAccess", "packageName: ${event?.packageName}")
//        Log.e("MyAccess", "recordCount: ${event?.recordCount}")
//        Log.e("MyAccess", "movementGranularity: ${event?.movementGranularity}")

        val info = event?.source
        val rootInfo = rootInActiveWindow
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//            val windowinfo = windows
//            Log.e("windowinfo", "${windows.size} $windowinfo.")
//            printChildren(windowinfo)
//        }

        printChildren(info)
        printChildren(rootInfo)


//        Log.e("info", "${info}")
//        Log.e("rootInfo", "${rootInfo}")
//        Log.e("info", "${info?.childCount}")
//        Log.e("rootInfo", "${rootInfo?.childCount}")
    }

    private fun printChildren(info: List<AccessibilityWindowInfo>?) {
        info?.let {
            it.forEach {
                Log.e("window", " $it")
            }
        }
//        info?.let {
//            for (i in 0 until info.size) {
//                val child = info.getChild(i)
////                Log.e("info child", "${child.className}")
////                Log.e("info child", "${child.text}")
//                when (child?.className) {
////                    "android.widget.TextView" -> {
////                        Log.e("info child", "${child.text}")
////                    }
////                    "android.widget.EditText" -> {
////                        Log.e("info child", "${child.text}")
////                    }
//                    "android.widget.ImageView" -> {
//                        Log.e("info child", "${child}")
//
////                        Log.e("info child extra", "${child}")
//                    }
////                    "android.widget.ImageButton" -> {
////                        Log.e("info child", "${child}")
////                    }
//                }
//
//                printChildren(child)
//            }
//        }
    }

    private fun printChildren(info: AccessibilityNodeInfo?) {
        info?.let {
            for (i in 0 until info.childCount) {
                val child = info.getChild(i)
//                Log.e("info child", "${child.className}")
//                Log.e("info child", "${child.text}")
                when (child?.className) {
//                    "android.widget.TextView" -> {
//                        Log.e("info child", "${child.text}")
//                    }
//                    "android.widget.EditText" -> {
//                        Log.e("info child", "${child.text}")
//                    }
                    "android.widget.ImageView" -> {
                        Log.e("info child", "${child}")

//                        Log.e("info child extra", "${child}")
                    }
//                    "android.widget.ImageButton" -> {
//                        Log.e("info child", "${child}")
//                    }
                }

                printChildren(child)
            }
        }
    }
}