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

import org.webharvest.definition.FileDef;
import org.webharvest.exception.FileException;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.scripting.ScriptEngine;
import org.webharvest.runtime.templaters.BaseTemplater;
import org.webharvest.runtime.variables.*;
import org.webharvest.utils.CommonUtil;

import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * File processor.
 */
public class FileProcessor extends BaseProcessor {

    private FileDef fileDef;

    public FileProcessor(FileDef fileDef) {
        super(fileDef);
        this.fileDef = fileDef;
    }

    public Variable execute(Scraper scraper, ScraperContext context) {
        String workingDir = scraper.getWorkingDir();

        ScriptEngine scriptEngine = scraper.getScriptEngine();
        String action = BaseTemplater.execute( fileDef.getAction(), scriptEngine);
        String filePath = BaseTemplater.execute( fileDef.getPath(), scriptEngine);
        String type = BaseTemplater.execute( fileDef.getType(), scriptEngine);
        String charset = BaseTemplater.execute( fileDef.getCharset(), scriptEngine);
        if (charset == null) {
            charset = scraper.getConfiguration().getCharset();
        }
        String listFilter = BaseTemplater.execute( fileDef.getListFilter(), scriptEngine);
        String listFiles = BaseTemplater.execute( fileDef.getListFiles(), scriptEngine);
        boolean isListFiles = CommonUtil.getBooleanValue(listFiles, true);
        String listDirs = BaseTemplater.execute( fileDef.getListDirs(), scriptEngine);
        boolean isListDirs = CommonUtil.getBooleanValue(listDirs, true);
        String listRecursive = BaseTemplater.execute( fileDef.getListRecursive(), scriptEngine);
        boolean isListRecursive = CommonUtil.getBooleanValue(listRecursive, false);

        this.setProperty("Action", action);
        this.setProperty("File Path", filePath);
        this.setProperty("Type", type);
        this.setProperty("Charset", charset);
        this.setProperty("List Files", Boolean.valueOf(isListFiles));
        this.setProperty("List Directories", Boolean.valueOf(isListDirs));
        this.setProperty("List Recursive", Boolean.valueOf(isListRecursive));

        String fullPath = CommonUtil.getAbsoluteFilename(workingDir, filePath);

        // depending on file acton calls appropriate method
        if ( "write".equalsIgnoreCase(action) ) {
            return executeFileWrite(false, scraper, context, fullPath, type, charset);
        } else if ( "append".equalsIgnoreCase(action) ) {
            return executeFileWrite(true, scraper, context, fullPath, type, charset);
        } else if ( "list".equalsIgnoreCase(action) ) {
            return executeFileList(filePath, listFilter, isListFiles, isListDirs, isListRecursive);
        } else {
            return executeFileRead(fullPath, type, charset, scraper);
        }
    }

    private Variable executeFileList(String filePath, String listFilter, boolean listFiles, boolean listDirs, boolean listRecursive) {
        File dir = new File(filePath);
        if ( !dir.exists() ) {
            throw new FileException("Directory \"" + dir + "\" doesn't exist!");
        } else if ( !dir.isDirectory() ) {
            throw new FileException("\"" + dir + "\" is not directory!");
        }

        Collection collection = listFiles(dir, new CommandPromptFilenameFilter(listFilter), listRecursive);
        TreeSet sortedFileNames = new TreeSet();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            File file = (File) iterator.next();
            boolean isDir = file.isDirectory();
            if ( !((!listDirs && isDir) || (!listFiles && !isDir)) ) {
                sortedFileNames.add( file.getAbsolutePath() );
            }
        }

        return new ListVariable( new ArrayList(sortedFileNames) );
    }

    /**
     * Writing content to the specified file.
     * If parameter "append" is true, then append content, otherwise write
     */
    private Variable executeFileWrite(boolean append, Scraper scraper, ScraperContext context, String fullPath, String type, String charset) {
        Variable result;
        
        try {
        	// ensure that target directory exists
        	new File( CommonUtil.getDirectoryFromPath(fullPath) ).mkdirs();
        	
            FileOutputStream out = new FileOutputStream(fullPath, append);
            byte[] data;

            if ( Types.TYPE_BINARY.equalsIgnoreCase(type) ) {
                Variable bodyListVar = new BodyProcessor(fileDef).execute(scraper, context);
                result = appendBinary(bodyListVar);
                data = result.toBinary();
            } else {
                Variable body = getBodyTextContent(fileDef, scraper, context);
                String content = body.toString();
                data = content.getBytes(charset);
                result = new NodeVariable(content);
            }

            out.write(data);
            out.flush();
            out.close();

            return result;
        } catch (IOException e) {
            throw new FileException("Error writing data to file: " + fullPath, e);
        }
    }

    /**
     * Reading the specified file.
     */
    private Variable executeFileRead(String fullPath, String type, String charset, Scraper scraper) {
        if ( Types.TYPE_BINARY.equalsIgnoreCase(type) ) {
            try {
                byte[] data = CommonUtil.readBytesFromFile(new File(fullPath));
                if ( scraper.getLogger().isInfoEnabled() ) {
                    scraper.getLogger().info("Binary file read processor: " + data.length + " bytes read.");
                }
                return new NodeVariable(data);
            } catch (IOException e) {
                throw new FileException("Error reading file: " + fullPath, e);
            }
        } else {
            try {
                String content = CommonUtil.readStringFromFile(new File(fullPath), charset);
                if ( scraper.getLogger().isInfoEnabled() ) {
                    scraper.getLogger().info( "Text file read processor: " + (content == null ? 0 : content.length()) + " characters read." );
                }
                return new NodeVariable(content);
            } catch (IOException e) {
                throw new FileException("Error reading the file: " + fullPath, e);
            }
        }
    }

    public NodeVariable appendBinary(Variable body) {
        if (body == null) {
            return new NodeVariable("");
        }

        byte[] result = null;

        Iterator iterator = body.toList().iterator();
        while (iterator.hasNext()) {
            Variable currVariable =  (Variable) iterator.next();
            byte bytes[] = currVariable.toBinary();
            if (bytes != null) {
                if (result == null) {
                    result = bytes;
                } else {
                    byte[] newResult = new byte[result.length + bytes.length];
                    System.arraycopy(result, 0, newResult, 0, result.length);
                    System.arraycopy(bytes, 0, newResult, result.length, bytes.length);
                    result = newResult;
                }
            }
        }

        return new NodeVariable(result);
    }

    private Collection listFiles(File directory, FilenameFilter filter, boolean recurse) {
        Vector files = new Vector();
        File[] entries = directory.listFiles();
        if (entries != null) {
            for (int f = 0; f < entries.length; f++) {
                File entry = entries[f];
                if (filter == null || filter.accept(directory, entry.getName())) {
                    files.add(entry);
                }
                if (recurse && entry.isDirectory()) {
                    files.addAll(listFiles(entry, filter, recurse));
                }
            }
        }
        return files;
    }

    private class CommandPromptFilenameFilter implements FilenameFilter {
        Pattern pattern = Pattern.compile(".*");

        private CommandPromptFilenameFilter(String filter) {
            if ( !CommonUtil.isEmpty(filter) ) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < filter.length(); i++) {
                    char ch = filter.charAt(i);
                    switch (ch) {
                        case '.' : buffer.append("\\."); break;
                        case '*' : buffer.append(".*"); break;
                        case '?' : buffer.append("."); break;
                        default : buffer.append(ch); break;
                    }
                }
                try {
                    pattern = Pattern.compile( buffer.toString() );
                } catch (Exception e) {
                    pattern = Pattern.compile("");
                }
            }
        }

        public boolean accept(File dir, String name) {
            return pattern.matcher(name).matches();
        }
    }

}