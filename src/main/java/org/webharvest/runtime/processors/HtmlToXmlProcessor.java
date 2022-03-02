/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.runtime.processors;

import org.htmlcleaner.*;
import org.webharvest.definition.HtmlToXmlDef;
import org.webharvest.exception.ParserException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.utils.CommonUtil;

import java.io.IOException;

/**
 * HTML to XML processor.
 */
public class HtmlToXmlProcessor extends BaseProcessor {

    private HtmlToXmlDef htmlToXmlDef;

    public HtmlToXmlProcessor(HtmlToXmlDef htmlToXmlDef) {
        super(htmlToXmlDef);
        this.htmlToXmlDef = htmlToXmlDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        Variable body = getBodyTextContent(htmlToXmlDef, scraper, context);

        HtmlCleaner cleaner = new HtmlCleaner();
        CleanerProperties properties = cleaner.getProperties();

        final ScriptEngine scriptEngine = scraper.getScriptEngine();

        final String advancedXmlEscape = BaseTemplater.execute( htmlToXmlDef.getAdvancedXmlEscape(), scriptEngine);
        if ( advancedXmlEscape != null) {
            properties.setAdvancedXmlEscape(CommonUtil.isBooleanTrue(advancedXmlEscape) );
        }

        final String cdataForScriptAndStyle = BaseTemplater.execute( htmlToXmlDef.getUseCdataForScriptAndStyle(), scriptEngine);
        if ( cdataForScriptAndStyle != null) {
            properties.setUseCdataForScriptAndStyle(CommonUtil.isBooleanTrue(cdataForScriptAndStyle) );
        }

        final String specialEntities = BaseTemplater.execute( htmlToXmlDef.getTranslateSpecialEntities(), scriptEngine);
        if ( specialEntities != null) {
            properties.setTranslateSpecialEntities(CommonUtil.isBooleanTrue(specialEntities) );
        }

        final String recognizeUnicodeChars = BaseTemplater.execute(htmlToXmlDef.getRecognizeUnicodeChars(), scriptEngine);
        if ( recognizeUnicodeChars != null) {
            properties.setRecognizeUnicodeChars( CommonUtil.isBooleanTrue(recognizeUnicodeChars) );
        }

        final String omitUnknownTags = BaseTemplater.execute(htmlToXmlDef.getOmitUnknownTags(), scriptEngine);
        if ( omitUnknownTags != null) {
            properties.setOmitUnknownTags( CommonUtil.isBooleanTrue(omitUnknownTags) );
        }

        final String useEmptyElementTags = BaseTemplater.execute(htmlToXmlDef.getUseEmptyElementTags(), scriptEngine);
        if ( useEmptyElementTags != null) {
            properties.setUseEmptyElementTags( CommonUtil.isBooleanTrue(useEmptyElementTags) );
        }

        final String treatUnknownTagsAsContent = BaseTemplater.execute(htmlToXmlDef.getTreatUnknownTagsAsContent(), scriptEngine);
        if ( treatUnknownTagsAsContent != null) {
            properties.setTreatUnknownTagsAsContent( CommonUtil.isBooleanTrue(treatUnknownTagsAsContent) );
        }

        final String omitDeprecatedTags = BaseTemplater.execute(htmlToXmlDef.getOmitDeprecatedTags(), scriptEngine);
        if ( omitDeprecatedTags != null) {
            properties.setOmitDeprecatedTags( CommonUtil.isBooleanTrue(omitDeprecatedTags) );
        }

        final String treatDeprTagsAsContent = BaseTemplater.execute(htmlToXmlDef.getTreatDeprecatedTagsAsContent(), scriptEngine);
        if ( treatDeprTagsAsContent != null) {
            properties.setTreatDeprecatedTagsAsContent( CommonUtil.isBooleanTrue(treatDeprTagsAsContent) );
        }

        final String omitXmlDecl = BaseTemplater.execute(htmlToXmlDef.getOmitXmlDecl(), scriptEngine);
        if ( omitXmlDecl != null) {
            properties.setOmitXmlDeclaration( CommonUtil.isBooleanTrue(omitXmlDecl) );
        }

        final String omitComments = BaseTemplater.execute(htmlToXmlDef.getOmitComments(), scriptEngine);
        if ( omitComments != null) {
            properties.setOmitComments( CommonUtil.isBooleanTrue(omitComments) );
        }

        final String omitHtmlEnvelope = BaseTemplater.execute(htmlToXmlDef.getOmitHtmlEnvelope(), scriptEngine);
        if ( omitHtmlEnvelope != null) {
            properties.setOmitHtmlEnvelope( CommonUtil.isBooleanTrue(omitHtmlEnvelope) );
        }

        final String allowMultiWordAttributes = BaseTemplater.execute(htmlToXmlDef.getAllowMultiWordAttributes(), scriptEngine);
        if ( allowMultiWordAttributes != null) {
            properties.setAllowMultiWordAttributes( CommonUtil.isBooleanTrue(allowMultiWordAttributes) );
        }

        final String allowHtmlInsideAttributes = BaseTemplater.execute(htmlToXmlDef.getAllowHtmlInsideAttributes(), scriptEngine);
        if ( allowHtmlInsideAttributes != null) {
            properties.setAllowHtmlInsideAttributes( CommonUtil.isBooleanTrue(allowHtmlInsideAttributes) );
        }

        final String namespacesAware = BaseTemplater.execute(htmlToXmlDef.getNamespacesAware(), scriptEngine);
        if ( namespacesAware != null) {
            properties.setNamespacesAware( CommonUtil.isBooleanTrue(namespacesAware) );
        } else {
            properties.setNamespacesAware(false);
        }

        final String hyphenReplacement = BaseTemplater.execute(htmlToXmlDef.getHyphenReplacement(), scriptEngine);
        if ( hyphenReplacement != null) {
            properties.setHyphenReplacementInComment(hyphenReplacement);
        }

        final String pruneTags = BaseTemplater.execute(htmlToXmlDef.getPrunetags(), scriptEngine);
        if ( pruneTags != null) {
            properties.setPruneTags(pruneTags);
        }

        final String booleanAtts = BaseTemplater.execute(htmlToXmlDef.getBooleanAtts(), scriptEngine);
        if ( booleanAtts != null) {
            properties.setBooleanAttributeValues(booleanAtts);
        }

        String outputType = BaseTemplater.execute(htmlToXmlDef.getOutputType(), scriptEngine);

        try {
            TagNode node = cleaner.clean(body.toString());
            String result;

            if ( "simple".equalsIgnoreCase(outputType) ) {
                result = new SimpleXmlSerializer(properties).getXmlAsString(node);
            } else if ( "pretty".equalsIgnoreCase(outputType) ) {
                result = new PrettyXmlSerializer(properties).getXmlAsString(node);
            } else if ( "browser-compact".equalsIgnoreCase(outputType) ) {
                result = new BrowserCompactXmlSerializer(properties).getXmlAsString(node);
            }  else {
                result = new CompactXmlSerializer(properties).getXmlAsString(node);
            }

            return new NodeVariable(result);
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

}