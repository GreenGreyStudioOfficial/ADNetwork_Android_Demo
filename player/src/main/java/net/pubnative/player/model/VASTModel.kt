//
// Copyright (c) 2016, PubNative, Nexage Inc.
// All rights reserved.
// Provided under BSD-3 license as follows:
//
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
// Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// Redistributions in binary form must reproduce the above copyright notice, this
// list of conditions and the following disclaimer in the documentation and/or
// other materials provided with the distribution.
//
// Neither the name of Nexage, PubNative nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
package net.pubnative.player.model

import net.pubnative.player.model.VASTModel
import net.pubnative.player.util.VASTLog
import net.pubnative.player.util.XmlTools
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.Boolean
import java.math.BigInteger
import java.util.*
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class VASTModel(@field:Transient var vastsDocument: Document) : Serializable {
    var pickedMediaFileURL: String? = null
    val trackingUrls: HashMap<TRACKING_EVENTS_TYPE, MutableList<String>>?
        get() {
            VASTLog.d(TAG, "getTrackingUrls")
            var tracking: MutableList<String>
            val trackings = HashMap<TRACKING_EVENTS_TYPE, MutableList<String>>()
            val xpath = XPathFactory.newInstance().newXPath()
            try {
                val nodes = xpath.evaluate(XPATH_COMBINED_TRACKING, vastsDocument, XPathConstants.NODESET) as NodeList
                var node: Node
                var trackingURL: String
                var eventName: String
                var key: TRACKING_EVENTS_TYPE
                for (i in 0 until nodes.length) {
                    node = nodes.item(i)
                    val attributes = node.attributes
                    eventName = attributes.getNamedItem("event").nodeValue
                    key = try {
                        TRACKING_EVENTS_TYPE.valueOf(eventName)
                    } catch (e: IllegalArgumentException) {
                        VASTLog.w(TAG, "Event:$eventName is not valid. Skipping it.")
                        continue
                    }
                    trackingURL = XmlTools.getElementValue(node) ?: ""
                    if (trackings.containsKey(key)) {
                        tracking = trackings[key]!!
                        tracking.add(trackingURL)
                    } else {
                        tracking = ArrayList()
                        tracking.add(trackingURL)
                        trackings[key] = tracking
                    }
                }
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return null
            }
            return trackings
        }
    val skipOffset: Int
        get() {
            VASTLog.d(TAG, "getSkipOffset")
            var result = -1
            val xpath = XPathFactory.newInstance().newXPath()
            try {
                val nodes = xpath.evaluate(XPATH_INLINE_LINEAR, vastsDocument, XPathConstants.NODESET) as NodeList
                if (nodes != null) {
                    result = nodes.item(0).attributes.getNamedItem("skipoffset").nodeValue.toInt()
                }
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
            }
            return result
        }
    val mediaFiles: List<VASTMediaFile>?
        get() {
            VASTLog.d(TAG, "getMediaFiles")
            val mediaFiles = ArrayList<VASTMediaFile>()
            val xpath = XPathFactory.newInstance().newXPath()
            try {
                val nodes = xpath.evaluate(XPATH_MEDIA_FILE, vastsDocument, XPathConstants.NODESET) as NodeList
                var node: Node
                var mediaFile: VASTMediaFile
                var mediaURL: String?
                var attributeNode: Node?
                for (i in 0 until nodes.length) {
                    mediaFile = VASTMediaFile()
                    node = nodes.item(i)
                    val attributes = node.attributes
                    attributeNode = attributes?.getNamedItem("apiFramework")
                    mediaFile.apiFramework = attributeNode?.nodeValue
                    attributeNode = attributes.getNamedItem("bitrate")
                    mediaFile.bitrate = if (attributeNode == null) null else BigInteger(attributeNode.nodeValue)
                    attributeNode = attributes.getNamedItem("delivery")
                    mediaFile.delivery = attributeNode?.nodeValue
                    attributeNode = attributes.getNamedItem("height")
                    mediaFile.height = if (attributeNode == null) null else BigInteger(attributeNode.nodeValue)
                    attributeNode = attributes.getNamedItem("id")
                    mediaFile.id = attributeNode?.nodeValue
                    attributeNode = attributes.getNamedItem("maintainAspectRatio")
                    mediaFile.isMaintainAspectRatio =
                        if (attributeNode == null) null else Boolean.valueOf(attributeNode.nodeValue)
                    attributeNode = attributes.getNamedItem("scalable")
                    mediaFile.isScalable =
                        if (attributeNode == null) null else Boolean.valueOf(attributeNode.nodeValue)
                    attributeNode = attributes.getNamedItem("type")
                    mediaFile.type = attributeNode?.nodeValue
                    attributeNode = attributes.getNamedItem("width")
                    mediaFile.width = if (attributeNode == null) null else BigInteger(attributeNode.nodeValue)
                    mediaURL = XmlTools.getElementValue(node)
                    mediaFile.value = mediaURL
                    mediaFiles.add(mediaFile)
                }
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return null
            }
            return mediaFiles
        }
    val duration: String?
        get() {
            VASTLog.d(TAG, "getDuration")
            var duration: String? = null
            val xpath = XPathFactory.newInstance().newXPath()
            try {
                val nodes = xpath.evaluate(XPATH_DURATION, vastsDocument, XPathConstants.NODESET) as NodeList
                var node: Node?
                if (nodes != null) {
                    for (i in 0 until nodes.length) {
                        node = nodes.item(i)
                        duration = XmlTools.getElementValue(node)
                    }
                }
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return null
            }
            return duration
        }

    fun getExtensionURL(type: String?): String? {
        var result: String? = null
        val xPath = XPATH_BANNER.replace(EXTENSION_TYPE_KEY, type!!)
        val extensions = getListFromXPath(xPath)

        // We will use the first banner seen of this type
        if (extensions!!.size > 0) {
            result = extensions[0]
        }
        return result
    }

    val videoClicks: VideoClicks?
        get() {
            VASTLog.d(TAG, "getVideoClicks")
            val videoClicks = VideoClicks()
            val xpath = XPathFactory.newInstance().newXPath()
            try {
                val nodes = xpath.evaluate(XPATH_VIDEO_CLICKS, vastsDocument, XPathConstants.NODESET) as NodeList
                var node: Node
                for (i in 0 until nodes.length) {
                    node = nodes.item(i)
                    val childNodes = node.childNodes
                    var child: Node
                    for (childIndex in 0 until childNodes.length) {
                        child = childNodes.item(childIndex)
                        val nodeName = child.nodeName
                        when {
                            nodeName.equals("ClickTracking", ignoreCase = true) -> {
                                XmlTools.getElementValue(child)?.let { videoClicks.clickTracking.add(it) }
                            }
                            nodeName.equals("ClickThrough", ignoreCase = true) -> {
                                XmlTools.getElementValue(child)?.let { videoClicks.clickThrough = it }
                            }
                            nodeName.equals("CustomClick", ignoreCase = true) -> {
                                XmlTools.getElementValue(child)?.let { videoClicks.customClick.add(it) }

                            }
                        }
                    }
                }
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return null
            }
            return videoClicks
        }
    val impressions: List<String>?
        get() {
            VASTLog.d(TAG, "getImpressions")
            return getListFromXPath(XPATH_IMPRESSION)
        }
    val errorUrl: List<String>?
        get() {
            VASTLog.d(TAG, "getErrorUrl")
            return getListFromXPath(XPATH_ERROR)
        }

    private fun getListFromXPath(xPath: String): List<String>? {
        VASTLog.d(TAG, "getListFromXPath")
        val list = ArrayList<String>()
        val xpath = XPathFactory.newInstance().newXPath()
        try {
            val nodes = xpath.evaluate(xPath, vastsDocument, XPathConstants.NODESET) as NodeList
            var node: Node?
            for (i in 0 until nodes.length) {
                node = nodes.item(i)
                XmlTools.getElementValue(node)?.let { list.add(it) }
            }
        } catch (e: Exception) {
            VASTLog.e(TAG, e.message, e)
            return null
        }
        return list
    }

    @Throws(IOException::class)
    private fun writeObject(oos: ObjectOutputStream) {
        VASTLog.d(TAG, "writeObject: about to write")
        oos.defaultWriteObject()
        val data = XmlTools.xmlDocumentToString(vastsDocument)
        // oos.writeChars();
        oos.writeObject(data)
        VASTLog.d(TAG, "done writing")
    }

    @Throws(ClassNotFoundException::class, IOException::class)
    private fun readObject(ois: ObjectInputStream) {
        VASTLog.d(TAG, "readObject: about to read")
        ois.defaultReadObject()
        val vastString = ois.readObject() as String
        VASTLog.d(TAG, "vastString data is:\n$vastString\n")
        XmlTools.stringToDocument(vastString)?.let { vastsDocument = it }
        VASTLog.d(TAG, "done reading")
    }

    companion object {
        var EXTENSION_POSTVIEW_BANNER = "PN-Postview-Banner"
        private val TAG = VASTModel::class.java.name
        private const val serialVersionUID = 4318368258447283733L

        // Tracking XPATH
        private const val XPATH_INLINE_LINEAR = "/VASTS/VAST/Ad/InLine/Creatives/Creative/Linear"
        private const val XPATH_INLINE_LINEAR_TRACKING =
            "/VASTS/VAST/Ad/InLine/Creatives/Creative/Linear/TrackingEvents/Tracking"
        private const val XPATH_INLINE_NONLINEAR_TRACKING =
            "/VASTS/VAST/Ad/InLine/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking"
        private const val XPATH_WRAPPER_LINEAR_TRACKING =
            "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/Linear/TrackingEvents/Tracking"
        private const val XPATH_WRAPPER_NONLINEAR_TRACKING =
            "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking"
        private const val XPATH_COMBINED_TRACKING =
            "$XPATH_INLINE_LINEAR_TRACKING|$XPATH_INLINE_NONLINEAR_TRACKING|$XPATH_WRAPPER_LINEAR_TRACKING|$XPATH_WRAPPER_NONLINEAR_TRACKING"

        // Direct items XPATH
        private const val XPATH_MEDIA_FILE = "//MediaFile"
        private const val XPATH_DURATION = "//Duration"
        private const val XPATH_VIDEO_CLICKS = "//VideoClicks"
        private const val XPATH_IMPRESSION = "//Impression"
        private const val XPATH_ERROR = "//Error"
        private const val XPATH_BANNER = "//Extension[@type='{EXTENSION_TYPE}']/Banner"
        private const val EXTENSION_TYPE_KEY = "{EXTENSION_TYPE}"
    }
}