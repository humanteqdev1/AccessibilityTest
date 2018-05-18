package com.example.back.accessibilitytest

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_SCROLLED
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED
import android.view.accessibility.AccessibilityNodeInfo
import com.example.back.accessibilitytest.models.VkLikesModel
import java.util.*

class TelemetryAccessibilityService7 : AccessibilityService() {
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
    private val FB = "com.facebook.katana"
    private val WHATSAPP = "com.whatsapp"
    private val VIBER = "com.viber.voip"
    private val MOZILLA_FIREFOX_URL_BAR_ID = "$MOZILLA_FIREFOX:id/url_bar_title"
    private val CHROME_URL_BAR_ID = "$CHROME:id/url_bar"
    private val YANDEX_BROWSER_URL_BAR_ID = "$YANDEX_BROWSER:id/bro_omnibar_address_title_view"
    private val YANDEX_BROWSER_URL_BAR_ID_2 = "$YANDEX_BROWSER:id/bro_omnibar_address_title_text"

    private val VK_POST_LIKES_ID = "$VK:id/post_likes"
    private val VK_LIKES_ID = "$VK:id/likes"
    private val VK_TV_LIKES_ID = "$VK:id/tv_likes"
    private val VK_IV_LIKES_ID = "$VK:id/iv_likes"
    private val VK_POST_VIEW_ID = "$VK:id/post_view"
    private val VK_POST_BTN_ID = "$VK:id/post_profile_btn"
    private val VK_POSTER_NAME_VIEW_ID = "$VK:id/poster_name_view"
    private val VK_GROUP_TITLE_ID = "$VK:id/title"
    private val VK_GROUP_LIST_ID = "$VK:id/list"

    private val WHATSAPP_SINGLE_MSG_ID = "$WHATSAPP:id/single_msg_tv"
    private val WHATSAPP_MESSAGE_TEXT_ID = "$WHATSAPP:id/message_text"
    private val WHATSAPP_CONTACT_NAME_ID = "$WHATSAPP:id/conversation_contact_name"

    private val MOZILLA_FIREFOX_BROWSER_TOOLBAR_ID = "org.mozilla.firefox:id/browser_toolbar"

    private val urlArray = arrayListOf<Triple<String?, Long?, Float>>()

    private var prevUrl: String? = null
    private var prevStartTime: Long = System.currentTimeMillis()
    private var prevScrollY: Float = 0F

    var postText = ""
    var postTitle = ""

    private val vkLikesArray = arrayListOf<VkLikesModel>()
    private val vkClickedTextArray = arrayListOf<String>()
    private val vkGroupArray = arrayListOf<String>()
    private val vkGroupClicksArray = arrayListOf<String>()

    private val whatsappSingleMsgSet = HashSet<String>()
    private val whatsappMessagesSet = HashSet<String>()
    private val whatsappClicksSet = HashSet<String>()

    override fun onInterrupt() {
        Log.e("INTERRUPT", " onInterrupt")
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
            showEverything(it)

//            val eventTextPair = getTextFromEvents(event,
//                    arrayListOf(MOZILLA_FIREFOX, CHROME),
//                    arrayListOf(URL),
//                    TYPE_VIEW_TEXT_SELECTION_CHANGED)
//            if(eventTextPair != null)
//                Log.e("events", "event pair: $eventTextPair")
//
//
//            collectForVK(event)
//            collectForBrowsers(event)
//            collectForWhatsapp(event)
//            collectForViber(event)
        }
    }

    private fun collectForViber(event: AccessibilityEvent) {

    }

    private fun collectForWhatsapp(event: AccessibilityEvent) {
        if (event.packageName == WHATSAPP) {
            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || event.eventType == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED) {
                if (event.text != null && event.text.isNotEmpty())
                    whatsappClicksSet.add("${event.text}")
            }

            saveEverythingUserCanSee(event)

            if (whatsappClicksSet.isNotEmpty())
                Log.e("tw", "clicks: ${whatsappClicksSet}")
            if (whatsappMessagesSet.isNotEmpty())
                Log.e("tw", "messages: ${whatsappMessagesSet}")
            if (whatsappSingleMsgSet.isNotEmpty())
                Log.e("tw", "single: ${whatsappSingleMsgSet}")
        }
    }

    private fun saveEverythingUserCanSee(event: AccessibilityEvent) {
        var parent = event.source
        while (parent != null) {
            if (parent.parent != null)
                parent = parent.parent
            else break
        }

        saveWhatsappText(parent)
    }

    private fun saveWhatsappText(info: AccessibilityNodeInfo?) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                child?.let { ch ->

                    if (ch.viewIdResourceName == WHATSAPP_SINGLE_MSG_ID) {
                        if (ch.text != null && ch.text.isNotEmpty()) {
                            whatsappSingleMsgSet.add("${ch.text}")
                        }
                    }
                    if (ch.viewIdResourceName == WHATSAPP_MESSAGE_TEXT_ID) {
                        if (ch.text != null && ch.text.isNotEmpty()) {
                            whatsappMessagesSet.add("${ch.text}")
                        }
                    }

                    saveWhatsappText(ch)
                }
            }
        }
    }

    private fun collectForBrowsers(event: AccessibilityEvent) {
        collect(rootInActiveWindow, arrayListOf(MOZILLA_FIREFOX,
                CHROME,
                YANDEX_BROWSER), arrayListOf(URL), event)
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
    private fun showEverything(event: AccessibilityEvent) {
//        if(event.packageName != VIBER)
//            return

        if (event.text != null && event.text.isNotEmpty())
            Log.e("234", " ${event.source?.viewIdResourceName} ${event.text} ${event.eventType}")

        var parent = event.source
        while (parent != null) {
            if (parent.parent != null)
                parent = parent.parent
            else break
        }

        showChildren(parent)
    }


    private fun collectForVK(event: AccessibilityEvent) {
        if (event.packageName == VK) {
            listenForGroups(event)

            if (event.eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
                //Collect vk clicks
                val node = event.source
                node?.let {
                    if (it.viewIdResourceName == VK_POST_VIEW_ID && event.text != null && event.text.isNotEmpty()) {
                        vkClickedTextArray.add("${event.text}")
                        Log.e("text", "${vkClickedTextArray.size} $vkClickedTextArray")
                    }
                }
            }

            if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED || event.eventType == AccessibilityEvent.TYPE_VIEW_CONTEXT_CLICKED) {
                val node = event.source

                //Collect group clicks
                node?.let {
                    if(it.childCount == 3) {
                        val ch = it.getChild(1)
                        ch.let {
                            if(ch.viewIdResourceName == VK_GROUP_TITLE_ID) {
                                if (ch.text != null) {
                                    vkGroupClicksArray.add("${ch.text}")
                                    Log.e("vkGroupClicksArray", "$vkGroupClicksArray")
                                }
                            }
                        }
                    }

                    //Collect vk likes
                    when {
                        node.viewIdResourceName == VK_POST_BTN_ID -> {
                            val parent = event.source?.parent?.parent?.parent?.parent
                            parent?.let {
                                getLikeEvents(parent)

                                vkClickedTextArray.add("${event.text}")
                                Log.e("text", "${vkClickedTextArray.size} $vkClickedTextArray")
                            }
                        }
                        node.viewIdResourceName == VK_POST_LIKES_ID -> {
                            val parent = event.source?.parent
                            parent?.let {
                                getLikeEvents(parent)

                                addToArrayText(postTitle, postText, "USER_LIKE/DISLIKE")
                            }
                        }
                        node.viewIdResourceName == VK_LIKES_ID -> {
                            val parent = event.source?.parent?.parent?.parent?.parent
                            parent?.let {
                                getTextForVkPost(parent)
                                addToArrayText(postTitle, postText, "POST_LIKE/DISLIKE")
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun listenForGroups(event: AccessibilityEvent) {
        event.let {
            val node = it.source
            node?.let {
                if (node.viewIdResourceName == VK_GROUP_LIST_ID) {
                    for (i in 0 until node.childCount) {
                        val child = node.getChild(i)
                        if (child.childCount == 3) {
                            val subChild = child.getChild(1)
                            subChild?.let { ch ->
                                if (ch.text != null && ch.viewIdResourceName == VK_GROUP_TITLE_ID) {
                                    vkGroupArray.add("${ch.text}")
                                }
                            }
                        }
                    }
                }
            }
        }
        if(vkGroupArray.size > 0)
            Log.e("--", "groups: ${vkGroupArray}")
    }

    private fun addToArrayText(title: String, text: String, type: String) {
        if (title.isNotEmpty() || text.isNotEmpty()) {
            vkLikesArray.add(VkLikesModel(title, text, type))
        }

        postTitle = ""
        postText = ""
        Log.e("likes", " $vkLikesArray")
    }

//    private fun getPrevValue(info: AccessibilityNodeInfo?) {
//        info?.let {
//            for (i in 0 until it.childCount) {
//                val child = it.getChild(i)
//                child?.let { ch ->
//                    if(ch.text != null && ch.text.isNotEmpty()) {
////                        Log.e("adf", "L4 ${ch.text}}")
////                        prevLikeValue = "${ch.text}".toInt()
//                    }
//
//                    getPrevValue(ch)
//                }
//            }
//        }
//    }

    private fun getTextForVkPost(info: AccessibilityNodeInfo?) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                if(child != null) {
//                    //                    if(ch.viewIdResourceName == VK_TV_LIKES_ID) {
//                        Log.e("adf", "${child.text} ${child.viewIdResourceName}}")
////                        prevLikeValue = "${ch.text}".toInt()
////                    }
                    getTextForVkPost(child)

                    if (child.viewIdResourceName == VK_POSTER_NAME_VIEW_ID) {
                        if(!postTitle.isEmpty())
                            continue

                        postTitle = "${child.text}"
                    } else if (child.viewIdResourceName == VK_POST_VIEW_ID) {
                        if(!postText.isEmpty())
                            continue

                        postText = "${child.text}"
                    } else if(child.viewIdResourceName == VK_POST_VIEW_ID) {
                        if(!postText.isEmpty())
                            continue

                        postText = "${child.text}"
                    }
                }
            }
        }
    }
    private fun getLikeEvents(info: AccessibilityNodeInfo?) {
        info?.let {
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                if(child != null) {
                    //                    if(ch.viewIdResourceName == VK_TV_LIKES_ID) {
//                        Log.e("adf", "L3 ${child.text}}")
//                        prevLikeValue = "${ch.text}".toInt()
//                    }

                    if (child.viewIdResourceName == VK_POSTER_NAME_VIEW_ID) {
                        if(!postTitle.isEmpty())
                            continue

                        postTitle = "${child.text}"
                        getLikeEvents(child)
//                        return
                    } else if (child.viewIdResourceName == VK_POST_VIEW_ID) {
                        if(!postText.isEmpty())
                            continue

                        postText = "${child.text}"
                        getLikeEvents(child)
//                        return
                    } else if(child.viewIdResourceName == VK_POST_VIEW_ID) {
                        if(!postText.isEmpty())
                            continue

                        postText = "${child.text}"
                        getLikeEvents(child)
//                        return
                    }
                }
            }
        }
    }

    private fun showChildren(info: AccessibilityNodeInfo?, from: String? = "") {
        info?.let {
            Log.e("234", "from: ${it.viewIdResourceName}")
//            Log.e("234", "----------- from: ${it.viewIdResourceName}")
            for (i in 0 until it.childCount) {
                val child = it.getChild(i)
                child?.let { ch ->

                    if (ch.text != null)
                        Log.e("ch", " from: ${from} ${ch.viewIdResourceName} ${ch.text}")

                    showChildren(ch, ch.viewIdResourceName)
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
                    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
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