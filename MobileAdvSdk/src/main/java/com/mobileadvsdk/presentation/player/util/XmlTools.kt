package com.mobileadvsdk.presentation.player.util

import com.mobileadvsdk.presentation.player.util.VASTLog.d
import com.mobileadvsdk.presentation.player.util.VASTLog.e
import com.mobileadvsdk.presentation.player.util.VASTLog.v
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

internal object XmlTools {
    private val TAG = XmlTools::class.java.name

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