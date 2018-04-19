package com.example.back.accessibilitytest

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.*
import android.view.accessibility.AccessibilityNodeInfo
import com.example.back.accessibilitytest.models.VkLikesModel


/**
 * Created by back on 26.03.18.
 */
class MyAccessibilityService : AccessibilityService() {
    private val URL = "URL"
    private val VIEW = "android.view.View"
    private val WEBVIEW = "android.webkit.WebView"
    private val TEXTVIEW = "android.widget.TextView"
    private val EDITTEXT = "android.widget.EditText"
    private val CHROME = "com.android.chrome"
    private val MOZILLA_FIREFOX = "org.mozilla.firefox"
    private val YANDEX_BROWSER = "com.yandex.browser"
    private val TELEGRAM = "org.mozilla.firefox"
    private val VK = "com.vkontakte.android"
    private val MOZILLA_FIREFOX_URL_BAR_ID = "$MOZILLA_FIREFOX:id/url_bar_title"
    private val CHROME_URL_BAR_ID = "$CHROME:id/url_bar"
    private val YANDEX_BROWSER_URL_BAR_ID = "$YANDEX_BROWSER:id/bro_omnibar_address_title_view"
    private val YANDEX_BROWSER_URL_BAR_ID_2 = "$YANDEX_BROWSER:id/bro_omnibar_address_title_text"

    private val VK_LIKES_ID = "$VK:id/likes"
    private val VK_TV_LIKES_ID = "$VK:id/tv_likes"
    private val VK_IV_LIKES_ID = "$VK:id/iv_likes"
    private val VK_POST_VIEW_ID = "$VK:id/post_view"
    private val VK_POSTER_NAME_VIEW_ID = "$VK:id/poster_name_view"
    private val MOZILLA_FIREFOX_BROWSER_TOOLBAR_ID = "org.mozilla.firefox:id/browser_toolbar"

    private val urlArray = arrayListOf<Triple<String?, Long?, Float>>()

    private var prevUrl: String? = null
    private var prevStartTime: Long = System.currentTimeMillis()
    private var prevScrollY: Float = 0F

    var postText = ""
    var postTitle = ""
    private var expectUpdate = false
    private var prevLikeValue = -1

    private val vkLikesArray = arrayListOf<VkLikesModel>()
    private val vkClickedTextArray = arrayListOf<String>()

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
        tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY //Deprecated in Android 8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tempInfo.flags = tempInfo.flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        }
        serviceInfo = tempInfo
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            collect(rootInActiveWindow, arrayListOf(MOZILLA_FIREFOX,
                    CHROME,
                    YANDEX_BROWSER), arrayListOf(URL), it)

//            val eventTextPair = getTextFromEvents(event,
//                    arrayListOf(MOZILLA_FIREFOX, CHROME),
//                    arrayListOf(URL),
//                    TYPE_VIEW_TEXT_SELECTION_CHANGED)
//            if(eventTextPair != null)
//                Log.e("events", "event pair: $eventTextPair")

            collectForVK(event)

            /**
             * Collecting top scroll offset
             * Not working for: Firefox;
             * Works for: Chrome, Yandex;
             */
            if (event.eventType == TYPE_VIEW_SCROLLED && event.className == WEBVIEW) {
                val scrollY = getScrollPosition(event)
                if (scrollY > prevScrollY)
                    prevScrollY = scrollY
            }

            if (urlArray.isNotEmpty()) {
                Log.e("map", "$urlArray")
            }
        }
    }

    private fun collectForVK(event: AccessibilityEvent) {
        if (event.packageName == VK) {
            if (event.eventType == TYPE_VIEW_FOCUSED) {
                //Collect vk clicks
                val node = event.source
                node?.let {
                    if (it.viewIdResourceName == VK_POST_VIEW_ID && event.text != null && event.text.isNotEmpty()) {
                        vkClickedTextArray.add("${event.text}")
                        Log.e("text", "${vkClickedTextArray.size} $vkClickedTextArray")
                    }
                }
            }

            if (event.eventType == TYPE_VIEW_CLICKED || event.eventType == TYPE_VIEW_CONTEXT_CLICKED) {
                //Collect vk likes
                val node = event.source
                node?.let {
                    expectUpdate = false
                    if (node.viewIdResourceName == VK_LIKES_ID) {
                        val parent = node.parent?.parent?.parent
                        parent?.let {
                            getLikeEvents(parent)
                            if (postText.isNotEmpty() || postTitle.isNotEmpty()) {
                                try {
                                    val textValue = "${event.text}"
                                    if (prevLikeValue != -1) {
                                        val newLikeValue = textValue.slice(1..textValue.length - 2).toInt()
                                        vkLikesArray.add(VkLikesModel(postTitle, postText,
                                                if (newLikeValue >= prevLikeValue) "LIKED" else "DISLIKED"))
                                        Log.e("likes", " $vkLikesArray")
                                        prevLikeValue = newLikeValue
                                    } else {
                                        prevLikeValue = textValue.slice(1..textValue.length - 2).toInt()
                                        expectUpdate = true
                                    }
                                } catch (ex: Exception) {
                                    expectUpdate = false
                                }
                            }
                        }
                    }
                }
            } else if (expectUpdate) {
                val node = event.source
                node?.let {
                    val parent = node.parent
                    parent?.let {
                        for (i in 0 until parent.childCount) {
                            val ch = parent.getChild(i)
                            ch?.let {
                                if (ch.viewIdResourceName == VK_TV_LIKES_ID) {
                                    try {
                                        val newLikeValue = "${ch.text}".toInt()
                                        vkLikesArray.add(VkLikesModel(postTitle, postText,
                                                if (newLikeValue > prevLikeValue) "LIKED" else "DISLIKED"))
                                        Log.e("likes", " $vkLikesArray")
                                    } catch (ex: Exception) {
                                    } finally {
                                        expectUpdate = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLikeEvents(info: AccessibilityNodeInfo?) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                child?.let { ch ->
                    if (ch.viewIdResourceName == VK_POSTER_NAME_VIEW_ID) {
                        postTitle = "${ch.text}"
                        return
                    } else if (ch.viewIdResourceName == VK_POST_VIEW_ID) {
                        postText = "${ch.text}"
                        return
                    }

                    getLikeEvents(ch)
                }
            }
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


    private fun collectChildren2(info: AccessibilityNodeInfo?) {
        info?.let {
            var counter = 0
            var parent = info.parent
            while (parent != null) {
                Log.e("--- counter", " ${counter++}")
                if (parent.parent != null)
                    parent = parent.parent
                else break
            }

            getLikeEvents(parent)
        }
    }

    private fun collectParent(info: AccessibilityNodeInfo?) {
        info?.let {
            val parent = it.parent?.parent?.parent
            parent?.let {
                //                    Log.e("--- PARENT", "\n\n" +
//                            "name: ${parent.className}\n" +
//                            "id: ${parent.viewIdResourceName}\n" +
//                            "text: ${parent.text}\n" +
//                            "contentDescription: ${parent.contentDescription}\n" +
//                            "packageName: ${parent.packageName}\n")

                getLikeEvents(parent)
            }
        }
    }

    /**
     * Collecting text
     * Not working for: Firefox;
     * Works for: Chrome;
     */
    private fun collect(info: AccessibilityNodeInfo?,
                        packageNameList: List<String>,
                        collectTypeList: List<String>,
                        event: AccessibilityEvent) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                child?.let { ch ->

                    //                    if (event.eventType == TYPE_VIEW_CLICKED || event.eventType == TYPE_VIEW_CONTEXT_CLICKED) {
//                        if(!ch.text.isNullOrEmpty() || ch.viewIdResourceName == VK_IV_LIKES_ID) {
//                            Log.e("--- CLICKED", "\n\n" +
//                                    "eventType: ${event.eventType}\n" +
//                                    "name: ${ch.className}\n" +
//                                    "id: ${ch.viewIdResourceName}\n" +
//                                    "text: ${ch.text}\n" +
//                                    "contentDescription: ${ch.contentDescription}\n" +
//                                    "packageName: ${ch.packageName}\n")
//                        }
//                    } else {
//                        if (!ch.text.isNullOrEmpty() && ch.packageName == VK)
//                            Log.e("---", "\n\n" +
//                                    "eventType: ${event.eventType}\n" +
//                                    "name: ${ch.className}\n" +
//                                    "id: ${ch.viewIdResourceName}\n" +
//                                    "text: ${ch.text}\n" +
//                                    "contentDescription: ${ch.contentDescription}\n" +
//                                    "packageName: ${ch.packageName}\n")
//
//                    }
                    //GET text from browsers
//                    if(!ch.text.isNullOrEmpty() && ch.className == VIEW) {
//                        Log.e("---", "text: ${ch.text}\n")
//                    }


                    // Search for package
                    if (event.eventType == TYPE_WINDOW_CONTENT_CHANGED) {
                        packageNameList.forEach { packageName ->
                            if (ch.packageName == packageName) {

                                // Search for type
                                collectTypeList.forEach { collectType ->

                                    // Handle collect type
                                    if (collectType == URL) {
                                        when (ch.viewIdResourceName) {
                                            CHROME_URL_BAR_ID -> {
                                                collectUrls(ch)
                                            }
                                            MOZILLA_FIREFOX_URL_BAR_ID -> {
                                                collectUrls(ch)
                                            }
                                            YANDEX_BROWSER_URL_BAR_ID, YANDEX_BROWSER_URL_BAR_ID_2 -> {
                                                collectUrls(ch)
                                            }
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

    private fun collectUrls(ch: AccessibilityNodeInfo) {
        val time = (System.currentTimeMillis() - prevStartTime) / 1000
        if (time < 6) return

        val url = if (ch.text != null)
            "${ch.text}"
        else
            "${ch.contentDescription}"

        if(prevUrl != url) {
            if(prevUrl != null) {
                urlArray.add(Triple(prevUrl.toString(), time, prevScrollY))
            }

            prevUrl = url
            prevStartTime = System.currentTimeMillis()
            prevScrollY = 0F
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