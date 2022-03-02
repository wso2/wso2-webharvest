package org.webharvest.runtime.web;

import org.webharvest.runtime.variables.*;

/**
 * Information about http request parameter.
 */
public class HttpParamInfo {

    // name of parameter
    private String name;

    // tells if part is file to be uploaded - applies only if http processor is multiparted
    private boolean isFile = false;

    // filename of upload file - applies only for multipart requests where partType = file
    private String fileName;

    // content type of upload file - applies only for multipart requests where partType = file
    private String contentType;

    // parameter value
    private Variable value;

    public HttpParamInfo(String name, boolean isFile, String fileName, String contentType, Variable value) {
        this.name = name;
        this.isFile = isFile;
        this.fileName = fileName;
        this.contentType = contentType;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public boolean isFile() {
        return isFile;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public Variable getValue() {
        return value;
    }

}