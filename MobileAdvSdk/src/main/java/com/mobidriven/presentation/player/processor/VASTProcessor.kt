package com.mobidriven.presentation.player.processor

import android.content.Context
import com.mobidriven.presentation.player.*
import com.mobidriven.presentation.player.model.VASTModel
import com.mobidriven.presentation.player.model.VastDocElements
import com.mobidriven.presentation.player.processor.VASTModelPostValidator.validate
import com.mobidriven.presentation.player.util.VASTLog
import com.mobidriven.presentation.player.util.XmlTools
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URL
import java.nio.charset.Charset
import javax.xml.parsers.DocumentBuilderFactory

/**
 * This class is responsible for taking a VAST 2.0 XML file, parsing it,
 * validating it, and creating a valid VASTModel object corresponding to it.
 *
 * It can handle "regular" VAST XML files as well as VAST wrapper files.
 */

private val TAG = VASTProcessor::class.java.name

// Maximum number of VAST files that can be read (wrapper file(s) + actual
// target file)
private const val MAX_VAST_LEVELS = 5

internal class VASTProcessor {

    var model: VASTModel? = null

    private val mergedVastDocs = StringBuilder(500)

    fun process(context: Context, xmlData: String): Int {
        VASTLog.d(TAG, "process")
        model = null
        val inputStream: InputStream? = try {
            ByteArrayInputStream(xmlData.toByteArray(charset(Charset.defaultCharset().name())))
        } catch (e: UnsupportedEncodingException) {
            VASTLog.e(TAG, e.message, e)
            return ERROR_XML_PARSE
        }
        val error = processUri(inputStream, 0)
        try {
            inputStream?.close()
        } catch (e: IOException) {
            VASTLog.e(TAG, "process", e)
        }

        if (error != ERROR_NONE) {
            return error
        }

        val mainDoc = wrapMergedVastDocWithVasts()
        mainDoc?.let { model = VASTModel(mainDoc) }?: run {
            return ERROR_XML_PARSE
        }

        return if (!validate(model, DefaultMediaPicker(context))) {
            ERROR_POST_VALIDATION
        } else ERROR_NONE
    }

    private fun wrapMergedVastDocWithVasts(): Document? {
        VASTLog.d(TAG, "wrapmergedVastDocWithVasts")
        mergedVastDocs.insert(0, "<VASTS>")
        mergedVastDocs.append("</VASTS>")
        val merged = mergedVastDocs.toString()
        VASTLog.v(TAG, "Merged VAST doc:\n$merged")
        return XmlTools.stringToDocument(merged)
    }

    private fun processUri(inputStream: InputStream?, depth: Int): Int {
        VASTLog.d(TAG, "processUri")

        if (depth >= MAX_VAST_LEVELS) {
            val message = "VAST wrapping exceeded max limit of $MAX_VAST_LEVELS."
            VASTLog.e(TAG, message)
            return ERROR_EXCEEDED_WRAPPER_LIMIT
        }

        val doc = createDoc(inputStream) ?: return ERROR_XML_PARSE
        merge(doc)

        // check to see if this is a VAST wrapper ad
        val uriToNextDoc = doc.getElementsByTagName(VastDocElements.VAST_AD_TAG_URI.value)

        return if (uriToNextDoc == null || uriToNextDoc.length == 0) {
            // This isn't a wrapper ad, so we're done.
            ERROR_NONE
        } else {
            // This is a wrapper ad, so move on to the wrapped ad and process it.
            VASTLog.d(TAG, "Doc is a wrapper. ")

            val node = uriToNextDoc.item(0)

            val nextUri = XmlTools.getElementValue(node)
            VASTLog.d(TAG, "Wrapper URL: $nextUri")
            val nextInputStream: InputStream? = try {
                val nextUrl = URL(nextUri)
                nextUrl.openStream()
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return ERROR_XML_OPEN_OR_READ
            }
            val error = processUri(nextInputStream, depth + 1)
            try {
                nextInputStream?.close()
            } catch (e: IOException) {
                VASTLog.d(TAG, "Doc is a wrapper. ")
            }
            error
        }
    }

    private fun createDoc(inputStream: InputStream?): Document? {
        VASTLog.d(TAG, "About to create doc from InputStream")
        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
            doc.documentElement.normalize()
            VASTLog.d(TAG, "Doc successfully created.")
            doc
        } catch (e: Exception) {
            VASTLog.e(TAG, e.message, e)
            null
        }
    }

    private fun merge(newDoc: Document) {
        VASTLog.d(TAG, "About to merge doc into main doc.")
        val nl = newDoc.getElementsByTagName("VAST")
        val newDocElement = nl.item(0)
        val doc = XmlTools.xmlDocumentToString(newDocElement)
        mergedVastDocs.append(doc)
        VASTLog.d(TAG, "Merge successful.")
    }
}