package com.example.back.accessibilitytest

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityRecord


/**
 * Created by back on 26.03.18.
 */
class MyAccessibilityService : AccessibilityService() {
    override fun onInterrupt() {
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val tempInfo = serviceInfo
        tempInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK

        tempInfo.eventTypes = tempInfo.eventTypes or
                AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED or
                AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED

        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = tempInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if(it.eventType == TYPE_WINDOW_CONTENT_CHANGED)
                collect(rootInActiveWindow, arrayListOf(MOZILLA_FIREFOX, CHROME), arrayListOf(URL), it)

            val eventTextPair = getTextFromEvents(event,
                    arrayListOf(MOZILLA_FIREFOX, CHROME),
                    arrayListOf(URL),
                    TYPE_VIEW_TEXT_SELECTION_CHANGED)
            if(eventTextPair != null)
                Log.e("events", "event pair: $eventTextPair")

            /**
             * Not working for: Firefox;
             * Works(?) for: Chrome;
             */
            if(event.eventType == TYPE_VIEW_SCROLLED && event.className == WEBVIEW)
                Log.e("scrollY", "scroll: ${getScrollPosition(event)}")


//            if(urlArray.isNotEmpty()) {
//                Log.e("map", "${urlArray.size}=${urlArray.toList()}")
//            }
        }
    }

    private fun getScrollPosition(event: AccessibilityEvent): Float {
            val itemCount = event.itemCount
            val fromIndex = event.fromIndex

            // First, attempt to use (fromIndex / itemCount).
            if (fromIndex >= 0 && itemCount > 0) {
                return fromIndex / itemCount.toFloat()
            }

            val scrollY = event.scrollY
            val maxScrollY = event.maxScrollY

            if (scrollY > -1) {

                // Next, attempt to use (scrollY / maxScrollY). This will fail if the
                // getMaxScrollX() method is not available.
                if (scrollY >= 0 && maxScrollY > 0) {
                    return scrollY / maxScrollY.toFloat()
                }

                // Finally, attempt to use (scrollY / itemCount).
                // TODO(alanv): Hack from previous versions -- is it still needed?
                return if (scrollY >= 0 && itemCount > 0 && scrollY <= itemCount) {
                    scrollY / itemCount.toFloat()
                } else -1f
            }

        return -1f
    }

    private fun getTextFromEvents(event: AccessibilityEvent,
                                  packageNameList: List<String>,
                                  collectTypeList: List<String>,
                                  type: Int): Pair<CharSequence, CharSequence>? {
        if(event.eventType == type) {
            collectTypeList.forEach {
                if(it == URL) {
                    packageNameList.forEach {
                        if (event.packageName == it) {
                            return Pair("${event.contentDescription}", "${event.text}")
                        }
                    }
                }
            }
        }

        return null
    }

    private val URL = "URL"
    private val WEBVIEW = "android.webkit.WebView"
    private val TEXTVIEW = "android.widget.TextView"
    private val EDITTEXT = "android.widget.EditText"
    private val CHROME = "com.android.chrome"
    private val MOZILLA_FIREFOX = "org.mozilla.firefox"
    private val TELEGRAM = "org.mozilla.firefox"
    private val MOZILLA_FIREFOX_URL_BAR_ID = "$MOZILLA_FIREFOX:id/url_bar_title"
    private val CHROME_URL_BAR_ID = "$CHROME:id/url_bar"
    private val MOZILLA_FIREFOX_BROWSER_TOOLBAR_ID = "org.mozilla.firefox:id/browser_toolbar"

    private val urlArray = arrayListOf<Triple<String?, Long?, Float>>()

    private var prevUrl: String? = null
    private var prevStartTime: Long = 0L
    private var prevScrollY: Long = 0L
    private fun collect(info: AccessibilityNodeInfo?,
                        packageNameList: List<String>,
                        collectTypeList: List<String>,
                        event: AccessibilityEvent) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                child?.let {ch ->

//                    if(ch.text != null)
//                        Log.e("---", "\n\neventType: ${event.eventType}\nname: ${ch.className}\n" +
//                                "id: ${ch.viewIdResourceName}\n" +
//                                "text: ${ch.text}\n" +
//                                "contentDescription: ${ch.contentDescription}\n" +
//                                "hintText: ${ch.hintText}\n" +
//                                "inputType: ${ch.inputType}\n" +
//                                "labelFor: ${ch.labelFor}\n" +
//                                "packageName: ${ch.packageName}\n" +
//                                "eventTime: ${event.eventTime}\n" +
//                                "error: ${ch.error}\n........\n\n")


                    // Search for package
                    packageNameList.forEach { packageName ->
                        if(ch.packageName == packageName) {

                            // Search for type
                            collectTypeList.forEach {collectType ->

                                // Handle collect type
                                if (collectType == URL) {
                                    when (ch.viewIdResourceName) {
                                        CHROME_URL_BAR_ID -> {
                                            collectUrls(ch, event)
                                        }
                                        MOZILLA_FIREFOX_URL_BAR_ID -> {
                                            collectUrls(ch, event)
                                            }
                                        }
                                    }
                                }
                            }
                    }
                }

                collect(child, packageNameList, collectTypeList, event)
            }
        }
    }

    private fun collectUrls(ch: AccessibilityNodeInfo, event: AccessibilityEvent) {
        val url = ch.text.toString()
        if(prevUrl != url) {
            if(prevUrl != null) {
                urlArray.add(Triple(prevUrl.toString(),
                        SystemClock.elapsedRealtime() - prevStartTime, 0f))
            }
            prevUrl = url
            prevStartTime = event.eventTime
        }


//        Log.e("---", "\n\neventType: ${event.eventType}\nname: ${ch.className}\n" +
//                "id: ${ch.viewIdResourceName}\n" +
//                "text: ${ch.text}\n" +
//                "contentDescription: ${ch.contentDescription}\n" +
//                "hintText: ${ch.hintText}\n" +
////                                                        if (ch.hintText == "Search or enter address") "Address bar" else "" +
//                "inputType: ${ch.inputType}\n" +
//                "labelFor: ${ch.labelFor}\n" +
//                "packageName: ${ch.packageName}\n" +
//                "eventTime: ${event.eventTime}\n" +
//                "error: ${ch.error}\n........\n\n")
    }
}