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

import org.webharvest.definition.IncludeDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.exception.FileException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.EmptyVariable;
import org.webharvest.runtime.variables.Variable;
import org.webharvest.utils.CommonUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Include processor.
 */
public class IncludeProcessor extends BaseProcessor {

    private IncludeDef includeDef;

    public IncludeProcessor(IncludeDef includeDef) {
        super(includeDef);
        this.includeDef = includeDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        boolean isUrl = false;
        
        String path = BaseTemplater.execute( includeDef.getPath(), scraper.getScriptEngine() );

        this.setProperty("Path", path);
        
        path = CommonUtil.adaptFilename(path);
        String fullPath = path;

        ScraperConfiguration configuration = scraper.getConfiguration(); 
        File originalFile = configuration.getSourceFile();
        String originalUrl = configuration.getUrl();
        if (originalFile != null) {
            String originalPath = CommonUtil.adaptFilename( originalFile.getAbsolutePath() );
            int index = originalPath.lastIndexOf('/');
            if (index > 0) {
                String workingPath = originalPath.substring(0, index);
                fullPath = CommonUtil.getAbsoluteFilename(workingPath, path);
            }
        } else if (originalUrl != null) {
            fullPath = CommonUtil.fullUrl(originalUrl, path);
            isUrl = true;
        }

        ScraperConfiguration includedConfig;
        try {
            includedConfig = isUrl ? new ScraperConfiguration(new URL(fullPath)) : new ScraperConfiguration(fullPath);
            scraper.execute(includedConfig.getOperations());
            return new EmptyVariable();
        } catch (FileNotFoundException e) {
            throw new FileException("Cannot include configuration file " + fullPath, e);
        } catch (MalformedURLException e) {
            throw new FileException("Cannot include configuration file " + fullPath, e);
        } catch (IOException e) {
            throw new FileException("Cannot include configuration file " + fullPath, e);
        }
    }

}