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
package com.gluonhq.plugin.dialogs;

import java.io.InputStream;
import java.util.Locale;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;

public abstract class PluginDialog extends Dialog {

    protected static final OS myOS;
    protected static enum OS { Windows, Mac, Linux };
    
    private static final int MIN_HEIGHT;
    static {
        String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ROOT);
        if (os.contains("mac") || os.contains("darwin")) {
            myOS = OS.Mac;
        } else if (os.contains("win")) {
            myOS = OS.Windows;
        } else {
            myOS = OS.Linux;
        }
        MIN_HEIGHT = myOS == OS.Mac ? 372 : myOS == OS.Windows ? 400 : 430;
    }
    
    protected Composite composite;
    protected final Display display;
    protected final String fontName;
    protected final Color backColor, rowColorSelection;
    protected final Font titleFont, topFont;
    protected Control buttonControl;
   
    public PluginDialog(Shell shell) {
        super(shell);
        this.display = Display.getDefault();
        
        fontName = display.getSystemFont().getFontData()[0].getName();
        backColor = new Color(display, 244, 244, 244);
        rowColorSelection = display.getSystemColor(SWT.COLOR_WHITE);
        titleFont = new Font(display, fontName, getTitleFontSize(), SWT.NORMAL);
        topFont = new Font(display, fontName, getTopFontSize(), SWT.NORMAL);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        this.composite = (Composite) super.createDialogArea(parent);

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        
        composite.addPaintListener(new PaintListener() {
			
            @Override
            public void paintControl(PaintEvent e) {
                centerDialog(getShell());
                composite.removePaintListener(this);
            }
        });
        return composite;
    }
    
    protected void createTopContent(String title, InputStream imageName) {
        Composite top = new Composite(composite, SWT.NONE);

        top.setLayout(new GridLayout(2, false));
        top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        top.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
        
        final Image image = new Image(top.getDisplay(), imageName);
        Image resized = resizeImage(image, 48, 48);
        Label labelImage = new Label(top, SWT.CENTER);
        labelImage.setImage(resized);
        
        Label label = new Label(top, SWT.NONE);
        label.setText(title);
        final Font newFont = new Font(display, fontName, getTitleFontSize(), SWT.NORMAL);
        label.setFont(newFont);
        label.setBackground(rowColorSelection);
        
        createLineContent();
        
        top.addDisposeListener(e -> {
            newFont.dispose();
            resized.dispose();
        });
    }

    private void createLineContent() {
        Composite middle = new Composite(composite, SWT.NONE);
        middle.setBackground(backColor);
        middle.setLayout(new GridLayout(1, false));
        middle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        middle.addPaintListener(e -> {
            e.gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
            e.gc.drawLine(0, 0, middle.getBounds().width, 1);
        });
    }

    protected Composite createCenterContent() {
        Composite center = new Composite(composite, SWT.NONE);
        center.setBackground(backColor);
        return center;
    }
    
    protected Composite createBottomContent() {
        Composite bottom = new Composite(composite, SWT.NONE);
        bottom.setBackground(backColor);
        
        GridLayout layout = new GridLayout(2, true);
        layout.marginTop = 0;
        layout.marginLeft = 20;
        layout.marginRight = 20;
        bottom.setLayout(layout);
        bottom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        return bottom;
    }
    
    @Override
    protected Control createButtonBar(Composite parent) {
        final Composite buttonBar = new Composite(parent, SWT.NONE);
        buttonBar.setBackground(backColor);
        		
        final GridLayout layout = new GridLayout();
        layout.marginLeft = 10;
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
        buttonBar.setLayout(layout);

        final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = false;
        buttonBar.setLayoutData(data);

        buttonBar.setFont(parent.getFont());

        // add the dialog's button bar to the right
        buttonControl = super.createButtonBar(buttonBar);
        buttonControl.setBackground(backColor);
        buttonControl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

        return buttonBar;
    }

    @Override
    protected Point getInitialSize() {
        final Point initialSize = super.getInitialSize();
        if (initialSize.y < MIN_HEIGHT) {
            initialSize.y = MIN_HEIGHT;
        }
        return initialSize;
    }

    private void centerDialog(Shell shell) {
        Monitor primary = display.getPrimaryMonitor();
        Rectangle visualBounds = primary.getClientArea();
        final Point dialogSize = shell.getSize();
        int x = visualBounds.x + (visualBounds.width - dialogSize.x) / 2;
        int y = visualBounds.y + (visualBounds.height - dialogSize.y) / 2;
        shell.setLocation(x, y);
    }
    
    @Override
    public boolean close() {
        backColor.dispose();
        titleFont.dispose();
        topFont.dispose();
        return super.close();
    }
    
    protected void openURL(String url) {
        Program.launch(url);
    }
    
    protected final int getFontAwesomeSize() {
        return myOS == OS.Mac ? 16 : 14;
    }

    protected final int getTitleFontSize() {
        return myOS == OS.Mac ? 14 : 12;
    }

    protected final int getTopFontSize() {
        return myOS == OS.Mac ? 13 : 11;
    }
    
    private Image resizeImage(Image image, int width, int height) {
        Image scaled = new Image(Display.getDefault(), width, height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0,image.getBounds().width, image.getBounds().height, 0, 0, width, height);
        gc.dispose();
        image.dispose();
        return scaled;
    }
}
