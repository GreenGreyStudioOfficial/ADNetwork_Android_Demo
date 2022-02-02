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
package net.pubnative.player.util

import net.pubnative.player.util.VASTLog.d
import net.pubnative.player.util.VASTLog.e
import net.pubnative.player.util.VASTLog.v
import net.pubnative.player.util.VASTLog
import net.pubnative.player.util.XmlTools
import org.w3c.dom.CharacterData
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.*
import java.lang.Exception
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.Throws

object XmlTools {
    private val TAG = XmlTools::class.java.name
    fun logXmlDocument(doc: Document?) {
        d(TAG, "logXmlDocument")
        try {
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            val sw = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(sw))
            d(TAG, sw.toString())
        } catch (e: Exception) {
            e(TAG, e.message, e)
        }
    }

    fun xmlDocumentToString(doc: Document?): String? {
        var xml: String? = null
        d(TAG, "xmlDocumentToString")
        try {
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            val sw = StringWriter()
            transformer.transform(DOMSource(doc), StreamResult(sw))
            xml = sw.toString()
        } catch (e: Exception) {
            e(TAG, e.message, e)
        }
        return xml
    }

    fun xmlDocumentToString(node: Node?): String? {
        var xml: String? = null
        d(TAG, "xmlDocumentToString")
        try {
            val tf = TransformerFactory.newInstance()
            val transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
            transformer.setOutputProperty(OutputKeys.METHOD, "xml")
            transformer.setOutputProperty(OutputKeys.INDENT, "yes")
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4")
            val sw = StringWriter()
            transformer.transform(DOMSource(node), StreamResult(sw))
            xml = sw.toString()
        } catch (e: Exception) {
            e(TAG, e.message, e)
        }
        return xml
    }

    fun stringToDocument(doc: String?): Document? {
        d(TAG, "stringToDocument")
        val db: DocumentBuilder
        var document: Document? = null
        try {
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val `is` = InputSource()
            `is`.characterStream = StringReader(doc)
            document = db.parse(`is`)
        } catch (e: Exception) {
            e(TAG, e.message, e)
        }
        return document
    }

//    @Throws(IOException::class)
//    fun stringFromStream(inputStream: InputStream): String {
//        d(TAG, "stringFromStream")
//        val baos = ByteArrayOutputStream()
//        val buffer = ByteArray(1024)
//        var length = 0
//        while (inputStream.read(buffer).also { length = it } != -1) {
//            baos.write(buffer, 0, length)
//        }
//        val bytes = baos.toByteArray()
//        return String(bytes, "UTF-8")
//    }

    fun getElementValue(node: Node): String? {
        val childNodes = node.childNodes
        var child: Node
        var value: String? = null
        var cd: CharacterData
        for (childIndex in 0 until childNodes.length) {
            child = childNodes.item(childIndex)
            // value = child.getNodeValue().trim();
            cd = child as CharacterData
            value = cd.data.trim { it <= ' ' }
            if (value.isEmpty()) {
                // this node was whitespace
                continue
            }
            v(TAG, "getElementValue: $value")
            return value
        }
        return value
    }
}