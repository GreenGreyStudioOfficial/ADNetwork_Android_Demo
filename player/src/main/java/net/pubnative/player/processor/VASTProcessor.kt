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
package net.pubnative.player.processor

import net.pubnative.player.VASTParser
import net.pubnative.player.model.VASTModel
import net.pubnative.player.model.VAST_DOC_ELEMENTS
import net.pubnative.player.processor.VASTModelPostValidator.validate
import net.pubnative.player.util.VASTLog
import net.pubnative.player.util.XmlTools
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
class VASTProcessor(private val mediaPicker: VASTMediaPicker) {
    var model: VASTModel? = null
        private set
    private val mergedVastDocs = StringBuilder(500)
    fun process(xmlData: String): Int {
        VASTLog.d(TAG, "process")
        model = null
        var `is`: InputStream? = null
        `is` = try {
            ByteArrayInputStream(xmlData.toByteArray(charset(Charset.defaultCharset().name())))
        } catch (e: UnsupportedEncodingException) {
            VASTLog.e(TAG, e.message, e)
            return VASTParser.ERROR_XML_PARSE
        }
        val error = processUri(`is`, 0)
        try {
            `is`?.close()
        } catch (e: IOException) {
        }
        if (error != VASTParser.ERROR_NONE) {
            return error
        }
        val mainDoc = wrapMergedVastDocWithVasts()
        mainDoc?.let { model = VASTModel(mainDoc) }

        if (mainDoc == null) {
            return VASTParser.ERROR_XML_PARSE
        }
        return if (!validate(model, mediaPicker)) {
            VASTParser.ERROR_POST_VALIDATION
        } else VASTParser.ERROR_NONE
    }

    private fun wrapMergedVastDocWithVasts(): Document? {
        VASTLog.d(TAG, "wrapmergedVastDocWithVasts")
        mergedVastDocs.insert(0, "<VASTS>")
        mergedVastDocs.append("</VASTS>")
        val merged = mergedVastDocs.toString()
        VASTLog.v(TAG, "Merged VAST doc:\n$merged")
        return XmlTools.stringToDocument(merged)
    }

    private fun processUri(`is`: InputStream?, depth: Int): Int {
        VASTLog.d(TAG, "processUri")
        if (depth >= MAX_VAST_LEVELS) {
            val message = "VAST wrapping exceeded max limit of " + MAX_VAST_LEVELS + "."
            VASTLog.e(TAG, message)
            return VASTParser.ERROR_EXCEEDED_WRAPPER_LIMIT
        }
        val doc = createDoc(`is`) ?: return VASTParser.ERROR_XML_PARSE
        merge(doc)

        // check to see if this is a VAST wrapper ad
        val uriToNextDoc = doc.getElementsByTagName(VAST_DOC_ELEMENTS.VAST_AD_TAG_URI.value)
        return if (uriToNextDoc == null || uriToNextDoc.length == 0) {

            // This isn't a wrapper ad, so we're done.
            VASTParser.ERROR_NONE
        } else {

            // This is a wrapper ad, so move on to the wrapped ad and process
            // it.
            VASTLog.d(TAG, "Doc is a wrapper. ")
            val node = uriToNextDoc.item(0)
            val nextUri = XmlTools.getElementValue(node)
            VASTLog.d(TAG, "Wrapper URL: $nextUri")
            var nextInputStream: InputStream? = null
            nextInputStream = try {
                val nextUrl = URL(nextUri)
                nextUrl.openStream()
            } catch (e: Exception) {
                VASTLog.e(TAG, e.message, e)
                return VASTParser.ERROR_XML_OPEN_OR_READ
            }
            val error = processUri(nextInputStream, depth + 1)
            try {
                nextInputStream?.close()
            } catch (e: IOException) {
            }
            error
        }
    }

    private fun createDoc(`is`: InputStream?): Document? {
        VASTLog.d(TAG, "About to create doc from InputStream")
        return try {
            val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(`is`)
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

    companion object {
        private val TAG = VASTProcessor::class.java.name

        // Maximum number of VAST files that can be read (wrapper file(s) + actual
        // target file)
        private const val MAX_VAST_LEVELS = 5
        private const val IS_VALIDATION_ON = false
    }
}