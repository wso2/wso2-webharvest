package org.webharvest.gui;

import org.webharvest.utils.CommonUtil;
import org.bounce.text.xml.XMLDocument;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.*;
import javax.swing.text.*;
import java.io.*;
import java.awt.*;
import java.net.URL;

/**
 * @author: Vladimir Nikic
 * Date: May 17, 2007
 */
public class ConfigDocument implements DocumentListener {

    // short name of this document
    private String name;

    // file of this configuration
    private File file;

    // url of this configuration, if it is downloaded from Web
    private String url;

    // flag that tells if configuration is changed
    private boolean isChanged = false;

    // components that interact with document instance and react on document changes
    private Ide ide;
    private ConfigPanel configPanel;
    private XmlTextPane xmlPane;

    private int lineCount = 0;

    /**
     * Constructor - initializes xml pane for this document.
     * @param configPanel
     */
    public ConfigDocument(ConfigPanel configPanel, String name) {
        this.configPanel = configPanel;
        this.ide = configPanel.getIde();
        this.xmlPane = configPanel.getXmlPane();
        this.name = name;
    }

    private void updateDocumentChanged(boolean changed) {
        this.isChanged = changed;
        if (changed) {
            this.configPanel.getXmlEditorScrollPane().onDocChanged();
        }
        updateGUI();
    }

    // methods for loading document from various sources

    private void load(Reader reader) throws IOException {
        xmlPane.read(reader, null);

        final Document document = xmlPane.getDocument();
        document.addDocumentListener(this);
        document.addUndoableEditListener( xmlPane.getUndoManager() );
        document.putProperty( PlainDocument.tabSizeAttribute, new Integer(4) );
        document.putProperty( XMLDocument.AUTO_INDENTATION_ATTRIBUTE, new Boolean(true) );
        document.putProperty( XMLDocument.TAG_COMPLETION_ATTRIBUTE, new Boolean(true) );

        Document doc = xmlPane.getDocument();
        int len = doc.getLength();
        try {
            this.lineCount = CommonUtil.countChars(doc.getText(0, len), '\n', 0, len - 1) + 1;
        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        updateGUI();
    }

    void load(String text) throws IOException {
        load( new StringReader(text) );
        updateDocumentChanged(false);
    }

    void load(File file) throws IOException {
        this.file = file;
        this.name = file.getName();
        String fileCharset = ide.getSettings().getFileCharset();
        load( new InputStreamReader(new FileInputStream(file), fileCharset) );
        ide.getSettings().addRecentFile( file.getAbsolutePath() );
    }

    void load(URL url) throws IOException {
        this.url = url.toString();
        this.name = CommonUtil.getFileFromPath(this.url);
        load( new InputStreamReader((InputStream)url.getContent()) );
    }

    // implementation of methods from DocumentListener interface
    
    public void changedUpdate(DocumentEvent e) {
        updateDocumentChanged(true);
    }

    public void insertUpdate(DocumentEvent e) {
        try {
            Document document = xmlPane.getDocument();
            int docLen = document.getLength();
            String text = document.getText(0, docLen);
            int offset = e.getOffset();
            int linesTo = CommonUtil.countChars(text, '\n', 0, offset - 1) + 1;
            int linesFrom = CommonUtil.countChars(text, '\n', offset, docLen - 1);
            int linesAdded = linesTo + linesFrom - lineCount;
            lineCount = linesFrom + linesTo;

            if (linesAdded > 0) {
                BreakpointCollection breakpoints = xmlPane.getBreakpoints();
                breakpoints.updateBreakpoints(linesTo, linesAdded);
            }
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        updateDocumentChanged(true);
    }

    public void removeUpdate(DocumentEvent e) {
        try {
            Document document = xmlPane.getDocument();
            int docLen = document.getLength();
            String text = document.getText(0, docLen);
            int offset = e.getOffset();
            int linesTo = CommonUtil.countChars(text, '\n', 0, offset - 1) + 1;
            int linesFrom = CommonUtil.countChars(text, '\n', offset, docLen - 1);
            int linesRemoved = lineCount - linesTo - linesFrom;
            lineCount = linesFrom + linesTo;

            if (linesRemoved > 0) {
                BreakpointCollection breakpoints = xmlPane.getBreakpoints();
                breakpoints.removeBreakpointsInRange(linesTo, linesTo + linesRemoved);
                breakpoints.updateBreakpoints(linesTo + linesRemoved + 1, -linesRemoved);
            }
        } catch (BadLocationException e1) {
            e1.printStackTrace();
        }
        updateDocumentChanged(true);
    }

    /**
     * Saves this document to the file. If file is already joined to the configuration then
     * that file is overwritten, or file-save dialog is offered otherwise.
     * @param isSaveAs
     */
    public void saveConfigToFile(boolean isSaveAs) {
        if (isSaveAs || file == null) {
            JFileChooser fileChooser = GuiUtils.getFileChooser();
            int returnVal = fileChooser.showSaveDialog(this.ide);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fileChooser.getSelectedFile();
                if (fileChooser.getFileFilter() instanceof XmlFileFilter) {
                    if ( !file.getName().toLowerCase().endsWith(".xml") ) {
                        file = new File( file.getAbsolutePath() + ".xml" );
                    }
                }

                if ( isSaveAs && file != null && file.exists() ) {
                    String msg = "File \"" + file.getAbsolutePath() + "\" already exists.\nAre you sure you want to overwrite it?";
                    boolean toContinue = GuiUtils.showWarningQuestionBox(msg, false) == JOptionPane.YES_OPTION;

                    // if user choose not to overwrite existing file, then give up 
                    if (!toContinue) {
                        return;
                    }
                }

                configPanel.setConfigFile(file);
            }
        }

        if (file != null) {
            try {
                String charset = ide.getSettings().getFileCharset();
                CommonUtil.saveStringToFile(file, configPanel.getXml(), charset);
                this.name = file.getName();
                updateDocumentChanged(false);
                updateGUI();
                ide.getSettings().addRecentFile(file.getAbsolutePath());
            } catch (IOException e) {
                GuiUtils.showErrorMessage( e.getMessage() );
            }
        }
    }


    /**
     * Warn user that document is changed but changes are not saved, and saving. 
     * @return True if not canceled by user, false otherwise.
     */
    public boolean offerToSaveIfChanged() {
        if (isChanged) {
            int result = GuiUtils.showWarningQuestionBox("Save file \"" + this.name + "\"?", true);
            if (result == JOptionPane.YES_OPTION) {
                saveConfigToFile(false);
            } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.DEFAULT_OPTION) {
                return false;
            }
        }

        return true;
    }


    /**
     * Updates GUI based on status of this document.
     */
    private void updateGUI() {
        JTabbedPane tabbedPane = ide.getTabbedPane();

        int index = ide.findTabIndex(this.configPanel);
        
        // if tab containing this document is found
        if (index >= 0) {
            tabbedPane.setTitleAt(index, this.name);
            tabbedPane.setForegroundAt(index, this.isChanged ? Color.blue : Color.black);
        }
    }

    // getters and setters

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void dispose() {
        this.ide = null;
        this.configPanel = null;
        this.xmlPane = null;
        this.file = null;
    }
    
}
