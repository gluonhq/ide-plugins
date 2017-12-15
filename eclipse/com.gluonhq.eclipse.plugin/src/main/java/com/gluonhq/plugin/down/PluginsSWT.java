/*
 * Copyright (c) 2017, Gluon Software
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * 
 * * Neither the name of the copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.plugin.down;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.dialogs.PluginDialog;
import org.eclipse.swt.graphics.Point;

public class PluginsSWT extends PluginDialog {

    private final static String GLUON_PLUGIN_URL = "http://docs.gluonhq.com/charm/javadoc/latest/com/gluonhq/charm/down/plugins/";
    private final static String GLUON_DOWN_URL = "http://docs.gluonhq.com/charm/latest/#_charm_down";

    private TableViewer sourceTableViewer;
    private TableViewer targetTableViewer;
    
    private final PluginsBean pluginsBean;
    private final List<Plugin> target, source, targetOriginal;
    private static Font fontAwesome;
    private final Font fontGap;
    private final Font rowTextFont;
    private final Font rowTitleFont;
    private final TextStyle styleTitle, styleGap, styleRow, styleTitleSelected, styleRowSelected;
    private static Color rowColorTitle;
    private static Color rowColorBack;
    private final Color rowColorSelection;
    
    private Button btnSelect;
    private Button btnSelectAll;
    private Button btnDeselect;
    private Button btnDeselectAll;
    
    private int rowHeight = -1;
    
    public PluginsSWT(Shell shell, List<String> lines) {
        super(shell);
        this.pluginsBean = new PluginsBean(lines);
        
        target = pluginsBean.getPlugins();
        source = Stream.of(Plugin.values())
                .filter(p -> ! target.contains(p))
                .collect(Collectors.toList());
        targetOriginal = target.stream().collect(Collectors.toList());
        
        rowColorTitle = new Color(display, 70, 130, 180);
        int color = myOS == OS.Mac ? 249 : 239;
        rowColorBack = new Color(display, color, color, color);
        rowColorSelection = display.getSystemColor(SWT.COLOR_WHITE);
      
        fontAwesome = loadCustomFont(getFontAwesomeSize());
        fontGap = new Font(display, fontName, 4, SWT.NORMAL);
        rowTitleFont = new Font(display, fontName, getTopFontSize(), SWT.BOLD);
        rowTextFont = new Font(display, fontName, getTopFontSize() - 2, SWT.NORMAL);
        styleTitle = new TextStyle(rowTitleFont, rowColorTitle, null);
        styleGap = new TextStyle(fontGap, display.getSystemColor(SWT.COLOR_BLACK), null);
        styleRow = new TextStyle(rowTextFont, display.getSystemColor(SWT.COLOR_BLACK), null);
        styleTitleSelected = new TextStyle(rowTitleFont, rowColorSelection, null);
        styleRowSelected = new TextStyle(rowTextFont, rowColorSelection, null);
    }

    public PluginsBean getPluginsBean() {
        return pluginsBean;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        
        createContent();
        
        redrawTables();

        btnSelectAll.setFocus();
        return composite;
    }
    
    private void createContent() {
        createTopContent("Gluon Mobile - Required Services", PluginsSWT.class.getResourceAsStream("gm.png"));
        createCenterContent();
    }
    
    @Override
    protected Composite createCenterContent() {
        Composite center = super.createCenterContent();
		
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 10;
        layout.marginRight = 10;
        layout.horizontalSpacing = 5;
        layout.verticalSpacing = 5;
        center.setLayout(layout);
        center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label label = new Label(center, SWT.CENTER);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
        label.setText("Available Services");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        label = new Label(center, SWT.CENTER);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 1, 1));
        label.setText(" ");
        
        label = new Label(center, SWT.CENTER);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, false, 1, 1));
        label.setText("Selected Services");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        sourceTableViewer = createTableViewer("SOURCE", center);
        
        btnSelect = new Button(center, SWT.PUSH);
        final GridData layoutData = new GridData(SWT.CENTER, SWT.END, true, false, 1, 1);
        layoutData.widthHint = 40;
        btnSelect.setLayoutData(layoutData);
        btnSelect.setText("\uf105");
        btnSelect.setFont(fontAwesome);
        btnSelect.setEnabled(false);

        btnSelect.addListener(SWT.Selection, e -> {
            Stream.of(sourceTableViewer.getTable().getSelection())
                .map(item -> (Plugin) item.getData())
                .forEach(plugin -> {
                    source.remove(plugin);
                    target.add(plugin);
                });
            redrawTables();
        });
        
        targetTableViewer = createTableViewer("TARGET", center);
        
        btnSelectAll = new Button(center, SWT.PUSH);
        final GridData layoutData1 = new GridData(SWT.CENTER, SWT.CENTER, true, false, 1, 1);
        layoutData1.widthHint = 40;
        btnSelectAll.setLayoutData(layoutData1);
        btnSelectAll.setText("\uf101");
        btnSelectAll.setFont(fontAwesome);
        btnSelectAll.setEnabled(false);
        btnSelectAll.addListener(SWT.Selection, e -> {
            Stream.of(sourceTableViewer.getTable().getItems())
                .map(item -> (Plugin) item.getData())
                .forEach(plugin -> {
                    source.remove(plugin);
                    target.add(plugin);
                });
            redrawTables();
        });
        
        btnDeselect = new Button(center, SWT.PUSH);
        btnDeselect.setLayoutData(layoutData1);
        btnDeselect.setText("\uf104");
        btnDeselect.setFont(fontAwesome);
        btnDeselect.setEnabled(false);
        btnDeselect.addListener(SWT.Selection, e -> {
            Stream.of(targetTableViewer.getTable().getSelection())
                .map(item -> (Plugin) item.getData())
                .forEach(plugin -> {
                    target.remove(plugin);
                    source.add(plugin);
                });
            redrawTables();
        });
        
        btnDeselectAll = new Button(center, SWT.PUSH);
        final GridData layoutData2 = new GridData(SWT.CENTER, SWT.BEGINNING, true, true, 1, 1);
        layoutData2.widthHint = 40;
		btnDeselectAll.setLayoutData(layoutData2);
        btnDeselectAll.setText("\uf100");
        btnDeselectAll.setFont(fontAwesome);
        btnDeselectAll.setEnabled(false);
        btnDeselectAll.addListener(SWT.Selection, e -> {
            Stream.of(targetTableViewer.getTable().getItems())
                .map(item -> (Plugin) item.getData())
                .forEach(plugin -> {
                    target.remove(plugin);
                    source.add(plugin);
                });
            redrawTables();
        });
        
        return center;
    }

    @Override
    protected Point getInitialSize() {
        return new Point(800, 600);
    }
    
    @Override
    protected Control createButtonBar(Composite parent) {
        final Composite buttonBar = (Composite) super.createButtonBar(parent);

        Button help = new Button(buttonBar, SWT.CENTER);
        final GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1);
        layoutData.widthHint = 100;
        help.setLayoutData(layoutData);
        help.setText("Help");
        help.addListener(SWT.Selection, e -> openURL(GLUON_DOWN_URL));
        help.moveAbove(buttonControl);
        
        return buttonBar;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Ok");
        ok.setEnabled(false);
        setButtonLayoutData(ok);
        ok.addListener(SWT.Selection, e -> pluginsBean.setPlugins(target));

        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Cancel");
        cancel.setFocus();
        setButtonLayoutData(cancel);
        cancel.addListener(SWT.Selection, e -> pluginsBean.setPlugins(null));
    }
    
    @Override
    public boolean close() {
        if (fontAwesome != null) {
            fontAwesome.dispose();
        }
        fontGap.dispose();
        rowTitleFont.dispose();
        rowTextFont.dispose();
        rowColorBack.dispose();
        rowColorTitle.dispose();
        return super.close();
    }
    
    private TableViewer createTableViewer(String id, Composite composite) {
    	
        final TableViewer tableViewer = new TableViewer(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.FULL_SELECTION  | SWT.BORDER);
        Table table = tableViewer.getTable();
        table.setLinesVisible(false);
        table.setHeaderVisible(false);
        final GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 4);
        gd.widthHint = 320;
        table.setLayoutData(gd);
        table.setData(-1);

        TableViewerColumn mainColumnViewer = new TableViewerColumn(tableViewer, SWT.NONE);
        final TableColumn column = mainColumnViewer.getColumn();
        column.setWidth(gd.widthHint);
        mainColumnViewer.setLabelProvider(new OwnerDrawLabelProvider() {

            @Override
            protected void paint(Event e, Object element) {
                if (element != null) {
                    TextLayout textLayout = getTextLayout((Plugin) element, (e.detail & SWT.SELECTED) != 0);
                    textLayout.draw(e.gc, e.x + 10, e.y + 10);
                }
            }

            @Override
            protected void measure(Event e, Object element) {
                if (element != null) {
                    if (rowHeight == -1) {
                        TextLayout textLayout = getTextLayout((Plugin) element, false);
                        e.width = table.getClientArea().width - table.getBorderWidth() * 2;
                        e.height = textLayout.getBounds().height + 20;
                        rowHeight = e.height;
                    } else {
                        e.height = rowHeight;
                    }
                }
            }

            @Override
            protected void erase(Event e, Object element) {
                int index = table.indexOf((TableItem) e.item);
                Color oldBackground = e.gc.getBackground();
                e.gc.setBackground(index % 2 == 0 ? rowColorBack : oldBackground);
                e.gc.fillRectangle(0, e.y, table.getClientArea().width, e.height);
                e.gc.setBackground(oldBackground);
                e.detail &= ~SWT.FOREGROUND;
            }

            @Override
            public String getToolTipText(Object element) {
                return "";
            }

        });
        table.addListener(SWT.Resize, e -> column.setWidth(table.getClientArea().width));
        table.addListener(SWT.Selection, e -> checkSelection(table));
        tableViewer.addDoubleClickListener(e -> {
            if (id.equals("SOURCE")) {
                btnSelect.setSelection(true);
                btnSelect.notifyListeners(SWT.Selection, new Event());
            } else {
                btnDeselect.setSelection(true);
                btnDeselect.notifyListeners(SWT.Selection, new Event());
            }
        });

        tableViewer.setContentProvider(new ArrayContentProvider());
        ViewColumnViewerToolTipSupport.enableFor(tableViewer);

        return tableViewer;
    }
    
    private TextLayout getTextLayout(Plugin plugin, boolean selected) {
        final TextLayout textLayout = new TextLayout(display);
        int length1 = plugin.getName().length();
        String text = plugin.getName() + "\n \n" + plugin.getDescription();
        textLayout.setText(text);
        textLayout.setStyle(selected ? styleTitleSelected : styleTitle, 0, length1);
        textLayout.setStyle(styleGap, length1 + 1, length1 + 2);
        textLayout.setStyle(selected ? styleRowSelected : styleRow, length1 + 3, text.length());
        return textLayout;
    }
    
    private void redrawTables() {
        Table sourceTable = sourceTableViewer.getTable();
        sourceTable.setRedraw(false);
        clean(sourceTable);
        source.sort(Plugin::compareTo);
        sourceTableViewer.setInput(source);
        btnSelectAll.setEnabled(source.size() > 0);
        sourceTable.setRedraw(true);
        checkSelection(sourceTable);

        Table targetTable = targetTableViewer.getTable();
        targetTable.setRedraw(false);
        clean(targetTable);
        target.sort(Plugin::compareTo);
        targetTableViewer.setInput(target);
        btnDeselectAll.setEnabled(target.size() > 0);
        targetTable.setRedraw(true);
        checkSelection(targetTable);

        if (getButton(IDialogConstants.OK_ID) != null) {
            getButton(IDialogConstants.OK_ID).setEnabled(! targetOriginal.equals(target));
        }
    }

    private void clean(final Table table) {
        Stream.of(table.getItems()).forEach(TableItem::dispose);
    }
    
    private void checkSelection(final Table table) {
        TableItem[] items = table.getSelection();
        if (items != null && items.length > 0) {
            for (TableItem item: items) {
                Plugin plugin = (Plugin) item.getData();
                if (plugin != null) {
                    if (source.stream().anyMatch(p -> p.equals(plugin))) {
                        btnSelect.setEnabled(true);
                    } else if (target.stream().anyMatch(p -> p.equals(plugin))) {
                        btnDeselect.setEnabled(true);
                    }
                    return;
                }
            }
        }
        btnSelect.setEnabled(false);
        btnDeselect.setEnabled(false);
    }
    
    private Font loadCustomFont(int fontSize) {
        String fontPath = ProjectUtils.extractResourceToTmp("fontawesome-webfont.ttf");
        boolean isLoaded = display.loadFont(fontPath);
        if (isLoaded) {
            return Stream.of(Display.getDefault().getFontList(null, true))
                    .filter(fd -> fd.getName().toLowerCase(Locale.ROOT).contains("fontawesome"))
                    .findFirst()
                    .map(fd -> {
                        fd.setHeight(fontSize);
                        return new Font(display, fd);
                    })
                    .orElse(null);
        }
        return null;   
    }
    
    private static class ViewColumnViewerToolTipSupport extends ColumnViewerToolTipSupport {

        protected ViewColumnViewerToolTipSupport(ColumnViewer viewer, int style, boolean manualActivation) {
            super(viewer, style, manualActivation);
        }

        @Override
        protected Composite createViewerToolTipContentArea(Event event, ViewerCell cell, Composite parent) {
            final Composite composite = new Composite(parent, SWT.NONE);
            composite.setLayout(new GridLayout(2, false));
            composite.setBackground(rowColorBack);
            Plugin plugin = (Plugin) cell.getElement();

            Hyperlink button = new Hyperlink(composite, SWT.FLAT);
            button.setText("\uf05A");
            button.setFont(fontAwesome);
            button.setBackground(composite.getBackground());
            button.setForeground(rowColorTitle);
            button.setUnderlined(false);
            button.addListener (SWT.MouseDown, e -> Program.launch(GLUON_PLUGIN_URL + plugin.getUrl()));
            button.setToolTipText("Click to access the service's JavaDoc");

            Label text = new Label(composite, SWT.LEFT);
            final String description = plugin.getDescription();
            text.setText(description.contains(".") ? description.substring(0, description.indexOf(".")) : description);
            text.setBackground(composite.getBackground());
            text.setForeground(rowColorTitle);
            composite.pack();
            return composite;
        }

        @Override
        public boolean isHideOnMouseDown() {
            return false;
        }

        public static final void enableFor(final ColumnViewer viewer) {
            new ViewColumnViewerToolTipSupport(viewer, ToolTip.NO_RECREATE, false);
        }
		
    }
    
}
