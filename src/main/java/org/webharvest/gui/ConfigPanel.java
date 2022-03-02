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
package org.webharvest.gui;

import org.webharvest.definition.BaseElementDef;
import org.webharvest.definition.ConstantDef;
import org.webharvest.definition.IElementDef;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperRuntimeListener;
import org.webharvest.runtime.processors.BaseProcessor;
import org.webharvest.runtime.web.HttpClientManager;
import org.webharvest.utils.Constants;
import org.webharvest.gui.component.*;
import org.xml.sax.InputSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.BadLocationException;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URL;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

/**
 * Single panel containing XML configuration.
 * It is part of multiple-document interface where several such instances may exist at
 * the same time.
 */
public class ConfigPanel extends JPanel implements ScraperRuntimeListener, TreeSelectionListener, CaretListener {

    private static final String VIEW_RESULT_AS_TEXT = "View result as text";
    private static final String VIEW_RESULT_AS_XML = "View result as XML";
    private static final String VIEW_RESULT_AS_HTML = "View result as HTML";
    private static final String VIEW_RESULT_AS_IMAGE = "View result as image";
    private static final String VIEW_RESULT_AS_LIST = "View result as list";

    // basic skeletion for new opened configuration
    private static final String BASIC_CONFIG_SKELETION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<config>\n\t\n</config>";

    private XmlEditorScrollPane xmlEditorScrollPane;

    // loger for this configuration panel
    private Log logger;

    /**
     * Action listener for view menu items
     */
    private class ViewerActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String actionCommand = e.getActionCommand();

            int viewType = ViewerFrame.TEXT_VIEW;
            if ( VIEW_RESULT_AS_HTML.equalsIgnoreCase(actionCommand) ) {
                viewType = ViewerFrame.HTML_VIEW;
            } else if ( VIEW_RESULT_AS_IMAGE.equalsIgnoreCase(actionCommand) ) {
                viewType = ViewerFrame.IMAGE_VIEW;
            } else if ( VIEW_RESULT_AS_LIST.equalsIgnoreCase(actionCommand) ) {
                viewType = ViewerFrame.LIST_VIEW;
            } else if ( VIEW_RESULT_AS_XML.equalsIgnoreCase(actionCommand) ) {
                viewType = ViewerFrame.XML_VIEW;
            }

            DefaultMutableTreeNode treeNode;

            TreePath path = tree.getSelectionPath();
            if (path != null) {
                treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
                if (treeNode != null) {
                    Object userObject = treeNode.getUserObject();
                    if (userObject instanceof TreeNodeInfo) {
                        TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                        Map properties = treeNodeInfo.getProperties();
                        Object value = properties == null ? null : properties.get(Constants.VALUE_PROPERTY_NAME);
                        final ViewerFrame viewerFrame = new ViewerFrame( scraper, Constants.VALUE_PROPERTY_NAME, value, treeNodeInfo, viewType );
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                viewerFrame.setVisible(true);
                                viewerFrame.toFront();
                            }
                        });
                    }
                }
            }
        }
    }

    private ConfigDocument configDocument;

    // initial configuration parameters
    private Map initParams = null;

    private Ide ide;

    private ScraperConfiguration scraperConfiguration;

    private DefaultMutableTreeNode topNode;
    private DefaultTreeModel treeModel;
    private TreeNodeInfo selectedNodeInfo;
    private JTextArea logTextArea;
    private Map nodeInfos = new Hashtable();
    private NodeRenderer nodeRenderer = new NodeRenderer();

    private JSplitPane bottomSplitter;
    private JSplitPane leftSplitter;
    private JSplitPane leftView;
    private JScrollPane bottomView;
    private int leftDividerLocation = 0;
    private int bottomDividerLocation = 0;
    
    private XmlTextPane xmlPane;
    private JTree tree;
    private Scraper scraper;
    private PropertiesGrid propertiesGrid;

    // tree popup menu items
    private JMenuItem textViewMenuItem;
    private JMenuItem xmlViewMenuItem;
    private JMenuItem htmlViewMenuItem;
    private JMenuItem imageViewMenuItem;
    private JMenuItem listViewMenuItem;

    // Log area popup menu items
    private JMenuItem logSelectAllMenuItem;
    private JMenuItem logClearAllMenuItem;

    /**
     * Constructor of the panel - initializes parent Ide instance and name of the document.
     * @param ide
     * @param name
     */
    public ConfigPanel(final Ide ide, String name) {
        super(new BorderLayout());

        this.ide = ide;

        this.topNode = new DefaultMutableTreeNode();
        this.treeModel = new DefaultTreeModel(this.topNode);

        this.tree = new JTree(this.treeModel);
        this.tree.setRootVisible(false);
        this.tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        ToolTipManager.sharedInstance().registerComponent(this.tree);
        this.tree.setCellRenderer(this.nodeRenderer);
        tree.setShowsRootHandles(true);
        this.tree.addTreeSelectionListener(this);

        // defines pop menu for the tree
        final JPopupMenu treePopupMenu = new WHPopupMenu();
        JMenuItem menuItem = new MenuElements.MenuItem("Locate in source");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path != null) {
                    locateInSource( (DefaultMutableTreeNode) path.getLastPathComponent(), false );
                    try {
                        int startPos = xmlPane.getCaretPosition();
                        String content = xmlPane.getDocument().getText( 0, xmlPane.getDocument().getLength() );
                        if (content != null && content.length() > startPos) {
                            int closingIndex = content.indexOf('>', startPos);
                            if (closingIndex > startPos) {
                                xmlPane.select(startPos, closingIndex + 1);
                            }
                        }
                    } catch (BadLocationException e1) {
                    }
                    xmlPane.requestFocus();
                }
            }
        });
        treePopupMenu.add(menuItem);

        treePopupMenu.addSeparator();

        ViewerActionListener viewContentActionListener = new ViewerActionListener();

        textViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_TEXT);
        textViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(textViewMenuItem);

        xmlViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_XML);
        xmlViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(xmlViewMenuItem);

        htmlViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_HTML);
        htmlViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(htmlViewMenuItem);

        imageViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_IMAGE);
        imageViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(imageViewMenuItem);

        listViewMenuItem = new MenuElements.MenuItem(VIEW_RESULT_AS_LIST);
        listViewMenuItem.addActionListener(viewContentActionListener);
        treePopupMenu.add(listViewMenuItem);

        treePopupMenu.setOpaque(true);
        treePopupMenu.setLightWeightPopupEnabled(true);

        this.tree.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ( e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3 ) {
                    TreePath path = tree.getClosestPathForLocation( e.getX(), e.getY() );
                    if (path != null) {
                        tree.setSelectionPath(path);
                    }
                    treePopupMenu.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                }
            }
        });

        JScrollPane treeView = new JScrollPane(this.tree);
        treeView.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Create the XML editor pane.
        this.xmlPane = new XmlTextPane();
        this.xmlPane.addCaretListener(this);

        final AutoCompleter autoCompleter = new AutoCompleter(this.xmlPane);
        this.xmlPane.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                    if ( (e.getModifiers() & KeyEvent.CTRL_MASK) != 0 ) {
                        autoCompleter.autoComplete();
                    }
                }
            }
        });

        xmlPane.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ( e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3 ) {
                    xmlPane.setLastClickPoint(e.getPoint());
                    ide.getEditorPopupMenu().show( (JComponent)e.getSource(), e.getX(), e.getY() );
                }
            }
        });

        // creates document for this configuration panel
        this.configDocument = new ConfigDocument(this, name);

        // initialize document content
        try {
            this.configDocument.load(BASIC_CONFIG_SKELETION);
        } catch (IOException e) {
            GuiUtils.showErrorMessage( e.getMessage() );
        }

        this.xmlEditorScrollPane = new XmlEditorScrollPane( this.xmlPane, this.ide.getSettings().isShowLineNumbersByDefault() );

        this.propertiesGrid = new PropertiesGrid(this);
        JScrollPane propertiesView = new JScrollPane(propertiesGrid);
        propertiesView.setBorder(new EmptyBorder(0, 0, 0, 0));
        propertiesView.getViewport().setBackground(Color.white);
        this.leftView = new ProportionalSplitPane(JSplitPane.VERTICAL_SPLIT);
        this.leftView.setResizeWeight(0.8d);
        this.leftView.setBorder(null);
        this.leftView.setTopComponent(treeView);
        this.leftView.setBottomComponent(propertiesView);
        this.leftView.setDividerLocation(0.8d);
        this.leftView.setDividerSize(Constants.SPLITTER_WIDTH);
//        this.leftView.setDividerSize(Constants.SPLITTER_WIDTH);

        //Add the scroll panes to a split pane.
        leftSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplitter.setBorder(null);
        leftSplitter.setLeftComponent(leftView);
        leftSplitter.setRightComponent( this.xmlEditorScrollPane );
        leftSplitter.setDividerSize(Constants.SPLITTER_WIDTH);

        leftSplitter.setDividerLocation(250);

//        JPanel bottomPanel = new JPanel( new BorderLayout() );
        logTextArea = new JTextArea();
        logTextArea.setFont( new Font("Courier New", Font.PLAIN, 11) );
        logTextArea.setEditable(false);

        // defines pop menu for the log area
        final JPopupMenu logPopupMenu = new WHPopupMenu();

        logSelectAllMenuItem = new MenuElements.MenuItem("Select All");
        logSelectAllMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.requestFocus();
                logTextArea.selectAll();
            }
        });
        logPopupMenu.add(logSelectAllMenuItem);

        logClearAllMenuItem = new MenuElements.MenuItem("Clear All");
        logClearAllMenuItem.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                logTextArea.setText("");
            }
        });
        logPopupMenu.add(logClearAllMenuItem);

        logTextArea.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if ( e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3 ) {
                    String text = logTextArea.getText();
                    logClearAllMenuItem.setEnabled( text != null && !"".equals(text) );
                    logSelectAllMenuItem.setEnabled( text != null && !"".equals(text) );
                    logPopupMenu.show( (JComponent)e.getSource(), e.getX(), e.getY() );
                }
            }
        });

        this.logger = LogFactory.getLog(this.toString() + System.currentTimeMillis());

//        bottomPanel.add(logTextArea , BorderLayout.CENTER );

        bottomSplitter = new ProportionalSplitPane(JSplitPane.VERTICAL_SPLIT);
        bottomSplitter.setResizeWeight(1.0d);
        bottomSplitter.setBorder(null);
        bottomSplitter.setTopComponent(leftSplitter);
        bottomSplitter.setDividerSize(Constants.SPLITTER_WIDTH);
        bottomView = new JScrollPane(logTextArea);
        bottomView.setBorder(new EmptyBorder(0, 0, 0, 0));
        bottomSplitter.setBottomComponent(this.bottomView);
        bottomSplitter.setDividerLocation(0.8d);


        this.add(bottomSplitter, BorderLayout.CENTER);

        if ( !ide.getSettings().isShowHierarchyByDefault() ) {
            showHierarchy();
        }

        if ( !ide.getSettings().isShowLogByDefault() ) {
            showLog();
        }

        updateControls();
    }

    /**
     * Occures whenever caret position is changed inside editor
     * @param e
     */
    public void caretUpdate(CaretEvent e) {
        ide.updateGUI();
    }

    private void updateControls() {
        boolean viewAllowed = false;

        if (this.scraper != null) {
            viewAllowed = this.scraper.getStatus() != Scraper.STATUS_READY;
        }

        this.textViewMenuItem.setEnabled(viewAllowed);
        this.xmlViewMenuItem.setEnabled(viewAllowed);
        this.htmlViewMenuItem.setEnabled(viewAllowed);
        this.imageViewMenuItem.setEnabled(viewAllowed);
        this.listViewMenuItem.setEnabled(viewAllowed);
    }

    private void releaseScraper() {
        if (scraper != null) {
            scraper.dispose();
            scraper = null;
        }
    }

    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node == null) {
            return;
        }

        Object userObject =  node.getUserObject();
        if (userObject instanceof TreeNodeInfo) {
            this.selectedNodeInfo = (TreeNodeInfo) userObject;
            PropertiesGridModel model =  this.propertiesGrid.getPropertiesGridModel();
            if (model != null) {
                model.setProperties( this.selectedNodeInfo.getProperties(), this.selectedNodeInfo );
            }
        }
    }

    /**
     * Recursively traverses the configuration and creates visual tree representation.
     * @param root
     * @param defs
     */
    private void createNodes(DefaultMutableTreeNode root, IElementDef[] defs) {
        if (defs != null) {
            for (int i = 0; i < defs.length; i++) {
                IElementDef elementDef = defs[i];
                // constant text is not interesting to be in the visual tree
                if ( !(elementDef instanceof ConstantDef) ) {
                    TreeNodeInfo treeNodeInfo = new TreeNodeInfo(elementDef);
                    this.nodeInfos.put( treeNodeInfo.getElementDef(), treeNodeInfo );
                    DefaultMutableTreeNode node = treeNodeInfo.getNode();
                    this.treeModel.insertNodeInto(node, root, root.getChildCount());
                    createNodes( node, elementDef.getOperationDefs() );
                }
            }
        }
    }

    /**
     * Loads configuration from the specified source.
     * @param source CAn be instance of File, URL or String
     */
    public void loadConfig(Object source) {
        try {
            if (source instanceof URL) {
                this.configDocument.load((URL)source);
            } else if (source instanceof File) {
                this.configDocument.load((File)source);
            } else {
                this.configDocument.load(source == null ? "" : source.toString());
            }
            
            refreshTree();
            InputSource in = new InputSource(new StringReader(xmlPane.getText()));
            ScraperConfiguration scraperConfiguration = new ScraperConfiguration(in);
            setScraperConfiguration(scraperConfiguration);
        } catch (IOException e) {
            GuiUtils.showErrorMessage( e.getMessage() );
        }
    }

    /**
     * Refreshes tree view.
     * @return
     */
    public boolean refreshTree() {
        releaseScraper();
        xmlPane.clearMarkerLine();
        xmlPane.clearErrorLine();
        xmlPane.clearStopDebugLine();
        updateControls();

        String xmlContent = this.xmlPane.getText();
        InputSource in = new InputSource( new StringReader(xmlContent) );
        try {
            ScraperConfiguration scraperConfiguration = new ScraperConfiguration(in);
            scraperConfiguration.setSourceFile( this.configDocument.getFile() );
            scraperConfiguration.setUrl( this.configDocument.getUrl() );

            setScraperConfiguration(scraperConfiguration);

            ide.setTabIcon(this, null);
        } catch(Exception e) {
            e.printStackTrace();

            String errorMessage = e.getMessage();

            StringWriter writer = new StringWriter();
            e.printStackTrace(new PrintWriter(writer));
            this.logger.error(errorMessage + "\n" + writer.getBuffer().toString());
            
            ide.setTabIcon(this, ResourceManager.SMALL_ERROR_ICON);
            GuiUtils.showErrorMessage(errorMessage);
            return false;
        }

        return true;
    }

    private void setScraperConfiguration(ScraperConfiguration scraperConfiguration) {
        this.scraperConfiguration = scraperConfiguration;

        java.util.List operationDefs = scraperConfiguration.getOperations();
        IElementDef[] defs = new IElementDef[operationDefs.size()];
        Iterator it = operationDefs.iterator();
        int index = 0;
        while (it.hasNext()) {
            defs[index++] = (IElementDef) it.next();
        }

        this.topNode.removeAllChildren();
        this.nodeInfos.clear();
        createNodes(this.topNode, defs);
        this.treeModel.reload();
        expandTree();
    }

    /**
     * Expands whole tree.
     */
    private void expandTree() {
        for (int row = 0; row < tree.getRowCount(); row++) {
            tree.expandRow(row);
        }
    }

    public void onNewProcessorExecution(Scraper scraper, BaseProcessor processor) {
        BaseElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = (TreeNodeInfo) this.nodeInfos.get(elementDef);
            if (nodeInfo != null) {
                nodeInfo.increaseExecutionCount();
                setExecutingNode(nodeInfo);
                int lineNumber = locateInSource( nodeInfo.getNode(), true ) - 1;
                if (xmlPane.getBreakpoints().isThereBreakpoint(lineNumber)) {
                    scraper.pauseExecution();
                    xmlPane.clearMarkerLine();
                    xmlPane.setStopDebugLine(lineNumber);
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_BREAKPOINT_ICON);
                } else if ( ide.getSettings().isDynamicConfigLocate() ) {
                    xmlPane.setMarkerLine(lineNumber);
                    xmlPane.repaint();
                }
            }
        }
    }

    public void onExecutionStart(Scraper scraper) {
        xmlPane.clearStopDebugLine();
        xmlPane.clearErrorLine();
        xmlPane.clearMarkerLine();
        updateControls();
        this.ide.updateGUI();
    }

    public void onExecutionContinued(Scraper scraper) {
        xmlPane.clearStopDebugLine();
        xmlPane.clearErrorLine();
        xmlPane.clearMarkerLine();
        this.ide.updateGUI();
    }

    public void onExecutionPaused(Scraper scraper) {
        this.ide.updateGUI();
    }

    public void onExecutionEnd(Scraper scraper) {
        final Settings settings = ide.getSettings();
        if ( settings.isDynamicConfigLocate() ) {
            this.xmlPane.setEditable(true);
        }

        int status = scraper.getStatus();
        final String message = scraper.getMessage();
        
        if (status == Scraper.STATUS_FINISHED) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
                    if (settings.isShowFinishDialog()) {
                        GuiUtils.showInfoMessage("Configuration \"" + configDocument.getName() + "\" finished execution.");
                    }
                }
            });
        } else if (status == Scraper.STATUS_STOPPED) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GuiUtils.showWarningMessage("Configuration \"" + configDocument.getName() + "\" aborted by user!");
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
                }
            });
        } else if ( status == Scraper.STATUS_EXIT && message != null && !"".equals(message.trim()) ) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GuiUtils.showWarningMessage("Configuration exited: " + message);
                    ide.setTabIcon(ConfigPanel.this, ResourceManager.SMALL_FINISHED_ICON);
                }
            });
        }

        // refresh last executing node
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        setExecutingNode(null);
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged( previousNodeInfo.getNode() );
        }

        xmlPane.clearMarkerLine();
        xmlPane.clearStopDebugLine();

        // update GUI controls
        this.ide.updateGUI();

        // releases scraper in order help garbage collector
//        releaseScraper();
    }

    public void onProcessorExecutionFinished(Scraper scraper, BaseProcessor processor, Map properties) {
        BaseElementDef elementDef = processor.getElementDef();
        if (elementDef != null) {
            TreeNodeInfo nodeInfo = (TreeNodeInfo) this.nodeInfos.get(elementDef);
            if (nodeInfo != null) {
                nodeInfo.setProperties(properties);
                if ( nodeInfo == this.selectedNodeInfo ) {
                    PropertiesGridModel model =  this.propertiesGrid.getPropertiesGridModel();
                    if (model != null) {
                        model.setProperties( nodeInfo.getProperties(), nodeInfo );
                    }
                }

                java.util.List syncViews = nodeInfo.getSynchronizedViews();
                if (syncViews != null) {
                    for (int i = 0; i < syncViews.size(); i++) {
                        ViewerFrame viewerFrame = (ViewerFrame) syncViews.get(i);
                        viewerFrame.setValue(properties);
                    }
                }
            }
        }
    }

    public void onExecutionError(Scraper scraper, Exception e) {
        final Settings settings = ide.getSettings();
        if ( settings.isDynamicConfigLocate() ) {
            this.xmlPane.setEditable(true);
        }

        markException(e);
        String errorMessage = e.getMessage();

        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        if (this.scraper != null) {
            this.scraper.getLogger().error(errorMessage + "\n" + writer.getBuffer().toString());
        }

        if (settings.isShowFinishDialog()) {
            GuiUtils.showErrorMessage(errorMessage);
        }
        
        this.ide.setTabIcon(this, ResourceManager.SMALL_ERROR_ICON);
        this.ide.updateGUI();

        xmlPane.clearMarkerLine();
        xmlPane.clearStopDebugLine();

        // releases scraper in order to help garbage collector
//        releaseScraper();
    }

    private void setExecutingNode(TreeNodeInfo nodeInfo) {
        TreeNodeInfo previousNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (previousNodeInfo != null) {
            this.treeModel.nodeChanged( previousNodeInfo.getNode() );
        }
        this.nodeRenderer.setExecutingNodeInfo(nodeInfo);
        if (nodeInfo != null) {
            this.treeModel.nodeChanged( nodeInfo.getNode() );
        }
    }

    public void markException(Exception e) {
        this.nodeRenderer.markException(e);
        TreeNodeInfo treeNodeInfo = this.nodeRenderer.getExecutingNodeInfo();
        if (treeNodeInfo != null) {
            this.treeModel.nodeChanged( treeNodeInfo.getNode() );
            int line = locateInSource( treeNodeInfo.getNode(), true ) - 1;
            xmlPane.setErrorLine(line);
        }
    }

    public void runConfiguration() {
        if ( this.scraper != null && this.scraper.getStatus() == Scraper.STATUS_PAUSED ) {
            synchronized (this.scraper) {
                this.scraper.notifyAll();
            }

            ide.setTabIcon(this, ResourceManager.SMALL_RUN_ICON);
        } else if ( this.scraper == null || this.scraper.getStatus() != Scraper.STATUS_RUNNING ) {
            boolean ok = refreshTree();
            if (ok) {
                Settings settings = ide.getSettings();
                this.scraper = new Scraper(this.scraperConfiguration, settings.getWorkingPath());
                this.scraper.addVariablesToContext(initParams);
                if ( settings.isProxyEnabled() ) {
                    HttpClientManager httpClientManager = scraper.getHttpClientManager();

                    int proxyPort = settings.getProxyPort();
                    String proxyServer = settings.getProxyServer();
                    if (proxyPort > 0) {
                        httpClientManager.setHttpProxy(proxyServer, proxyPort);
                    } else {
                		httpClientManager.setHttpProxy(proxyServer);
                	}

                    if ( settings.isProxyAuthEnabled() ) {
                        String ntlmHost = settings.isNtlmAuthEnabled() ?  settings.getNtlmHost() : null;
                        String ntlmDomain = settings.isNtlmAuthEnabled() ?  settings.getNtlmDomain() : null;
                        httpClientManager.setHttpProxyCredentials(
                            settings.getProxyUserename(), settings.getProxyPassword(), ntlmHost, ntlmDomain
                        );
                    }
                }

                this.scraper.setDebug(true);
                this.logTextArea.setText(null);
                this.scraper.addRuntimeListener(this);

                ide.setTabIcon(this, ResourceManager.SMALL_RUN_ICON);

                // starts scrapping in separate thread
                new ScraperExecutionThread(this.scraper).start();
            }
        }
    }

    public Scraper getScraper() {
        return scraper;
    }

    public synchronized int getScraperStatus() {
        if (this.scraper != null) {
            return this.scraper.getStatus();
        }

        return -1;
    }

    public Ide getIde() {
        return ide;
    }

    public synchronized void stopScraperExecution() {
        if (this.scraper != null) {
            this.scraper.stopExecution();
        }
    }

    public synchronized void pauseScraperExecution() {
        if (this.scraper != null) {
            this.scraper.pauseExecution();
            ide.setTabIcon(this, ResourceManager.SMALL_PAUSED_ICON);
        }
    }

    private int locateInSource(DefaultMutableTreeNode treeNode, boolean locateAtLineBeginning) {
        if (treeNode != null) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof TreeNodeInfo) {
                TreeNodeInfo treeNodeInfo = (TreeNodeInfo) userObject;
                BaseElementDef elementDef = (BaseElementDef) treeNodeInfo.getElementDef();
                int lineNumber = elementDef.getLineNumber();
                int columnNumber = elementDef.getColumnNumber();

                String content = null;
                try {
                    content = this.xmlPane.getDocument().getText( 0, this.xmlPane.getDocument().getLength() );
                    String[] lines = content.split("\n");
                    int offset = 0;
                    int lineCount = 1;
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i];
                        if(lineCount == lineNumber) {
                            offset += locateAtLineBeginning ? 1 : columnNumber;
                            break;
                        }
                        lineCount++;
                        if(lineCount > 2) {
                            offset++;
                        }
                        offset += line.length();
                    }

                    if (offset < content.length()) {
                        content = content.substring(0, offset);
                    }

                    int startIndex = content.lastIndexOf('<');

                    this.xmlPane.setCaretPosition(startIndex >= 0 ? startIndex : 0);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }

                return lineNumber;
            }
        }

        return -1;
    }

    public void undo() {
        this.xmlPane.undo();
    }

    public void redo() {
        this.xmlPane.redo();
    }

    public String getXml() {
        return this.xmlPane.getText();
    }

    public XmlTextPane getXmlPane() {
        return xmlPane;
    }

    public JTree getTree() {
        return tree;
    }

    public XmlEditorScrollPane getXmlEditorScrollPane() {
        return xmlEditorScrollPane;
    }

    public void setConfigFile(File file) {
        if (this.scraperConfiguration != null) {
            this.scraperConfiguration.setSourceFile(file);
        }
    }

    public ConfigDocument getConfigDocument() {
        return configDocument;
    }

    public void showHierarchy() {
        boolean isVisible = this.leftView.isVisible();
        if (isVisible) {
            this.leftDividerLocation = this.leftSplitter.getDividerLocation();
        }
        this.leftView.setVisible(!isVisible);
        if (!isVisible) {
            this.leftSplitter.setDividerLocation(this.leftDividerLocation);
        }
    }

    public void showLog() {
        boolean isVisible = this.bottomView.isVisible();
        if (isVisible) {
            this.bottomDividerLocation = this.bottomSplitter.getDividerLocation();
        }
        this.bottomView.setVisible(!isVisible);
        if (!isVisible) {
            this.bottomSplitter.setDividerLocation(this.bottomDividerLocation);
        }
    }

    public boolean isHierarchyVisible() {
        return this.leftView.isVisible();
    }

    public boolean isLogVisible() {
        return this.bottomView.isVisible();
    }

    public Map getInitParams() {
        return initParams;
    }

    public void setInitParams(Map initParams) {
        this.initParams = initParams;
    }

    public void dispose() {
        if (this.configDocument != null) {
            this.configDocument.dispose();
        }
        if (this.scraper != null) {
            this.scraper.removeRuntimeListener(this);
            this.scraper = null;
        }

        this.xmlPane.removeCaretListener(this);
        this.tree.removeTreeSelectionListener(this);

        this.scraperConfiguration = null;
        this.ide = null;
        this.tree = null;
        this.treeModel = null;
        this.nodeInfos = null;
        this.nodeRenderer = null;
        this.configDocument = null;
        this.topNode = null;
    }
    
}