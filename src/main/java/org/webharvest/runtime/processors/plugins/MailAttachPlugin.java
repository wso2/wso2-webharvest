package org.webharvest.runtime.processors.plugins;

import org.apache.commons.mail.*;
import org.webharvest.gui.*;
import org.webharvest.runtime.*;
import org.webharvest.runtime.processors.*;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.*;

import javax.activation.*;
import java.io.*;

/**
 * Mail attachment plugin - can be used only inside mail plugin.
 */
public class MailAttachPlugin extends WebHarvestPlugin {

    public String getName() {
        return "mail-attach";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) {
        BaseProcessor processor = scraper.getRunningProcessorOfType(MailPlugin.class);
        if (processor != null) {
            MailPlugin mailPlugin = (MailPlugin) processor;
            Email email = mailPlugin.getEmail();
            if (email instanceof HtmlEmail) {
                String attachmentName = evaluateAttribute("name", scraper);
                if (CommonUtil.isEmptyString(attachmentName)) {
                    attachmentName = mailPlugin.getNextAttachmentName();
                }
                String mimeType = evaluateAttribute("mimetype", scraper);
                boolean isInline = evaluateAttributeAsBoolean("inline", false, scraper);
                HtmlEmail htmlEmail = (HtmlEmail) email;
                Variable bodyVar = executeBody(scraper, context);
                try {
                    if (CommonUtil.isEmptyString(mimeType)) {
                        mimeType = isInline ? "image/jpeg" : "application/octet-stream";
                    }
                    DataSource dataSource = MailPlugin.createDataSourceOfVariable(bodyVar, scraper.getConfiguration().getCharset(), mimeType);
                    String cid = htmlEmail.embed(dataSource, attachmentName);
                    return isInline ? new NodeVariable("cid:" + cid) : new EmptyVariable();
                } catch (IOException e) {
                    throw new MailPluginException(e);
                } catch (EmailException e) {
                    throw new MailPluginException(e);
                }
            } else {
                throw new MailPluginException("Cannot use mail attach plugin if mail type is not html!");
            }
        } else {
            throw new MailPluginException("Cannot use mail attach plugin out of mail plugin context!");
        }
    }

    public String[] getValidAttributes() {
        return new String[] {"name", "mimetype", "inline"};
    }

    public String[] getAttributeValueSuggestions(String attributeName) {
        if ("mimetype".equalsIgnoreCase(attributeName)) {
            return ResourceManager.MIME_TYPES;
        } else if ("inline".equalsIgnoreCase(attributeName)) {
            return new String[] {"true", "false"};
        }
        return null;
    }


}