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
package com.gluonhq.plugin.code;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gluonhq.plugin.dialogs.DialogUtils;
import com.gluonhq.plugin.dialogs.PluginDialog;

public class CodeSWT extends PluginDialog {

    private Combo functionsBox;
    private Label errorLabel;

    private final String applicationIdeKey;
    private final Code code;
    private CompletableFuture<Void> futureTask;

    public CodeSWT(Shell shell, String applicationIdeKey) {
        super(shell);
        this.applicationIdeKey = applicationIdeKey;
        code = new Code();
    }

    public Code getCode() {
        return code;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        
        createContent();
        
        return composite;
    }
    
    private void createContent() {
        createTopContent("Gluon Function - Code Generation", CodeSWT.class.getResourceAsStream("G_Grey_charm.png"));
        createCenterContent();
        createBottomContent();
    }

    @Override
    protected Composite createCenterContent() {
        Composite center = super.createCenterContent();
	
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 20;
        layout.marginRight = 10;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 15;
        center.setLayout(layout);
        center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label label = new Label(center, SWT.WRAP  | SWT.LEFT);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
        gridData.widthHint = 560;
        label.setLayoutData(gridData);
        label.setText("Select from the list below the Gluon CloudLink application to which this mobile application will be linked to. Its key and secret tokens will be added to the mobile application.");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        label = new Label(center, SWT.RIGHT);
        final GridData layoutData = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        layoutData.widthHint = 125;
        label.setLayoutData(layoutData);
        label.setText("Remote Functions");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        functionsBox = new Combo(center, SWT.READ_ONLY);
        functionsBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        functionsBox.setFont(topFont);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(layoutData);
        label.setText("Function Name");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        Text functionGivenNameText = new Text(center, SWT.FILL | SWT.BORDER);
        functionGivenNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        functionGivenNameText.setFont(topFont);
        functionGivenNameText.setEditable(false);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(layoutData);
        label.setText("Result Type");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        Text resultTypeText = new Text(center, SWT.FILL | SWT.BORDER);
        resultTypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        resultTypeText.setFont(topFont);
        resultTypeText.setEditable(false);
        
        Button menuButton = new Button(center, SWT.FLAT | SWT.ARROW | SWT.DOWN);
        final GridData btnLayoutData = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
        btnLayoutData.widthHint = 20;
        menuButton.setLayoutData(btnLayoutData);
        menuButton.setEnabled(false);
        Menu menu = new Menu(getShell(), SWT.POP_UP);
        final SelectionAdapter itemListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                MenuItem selected = (MenuItem) event.widget;
                resultTypeText.setText(selected.getText());
            }
        };
    		
        Stream.of("Boolean", "Byte", "Integer", "Long", "Float", "Double", "String", "Object", "T")
            .forEach(s -> {
                MenuItem item = new MenuItem(menu, SWT.PUSH); 
                item.setText(s);
                item.addSelectionListener(itemListener);
            });
        menuButton.addListener(SWT.Selection, e -> {
            Point loc = menuButton.getLocation();
            Rectangle rect = menuButton.getBounds();
            Point mLoc = new Point(loc.x - 1, loc.y + rect.height);
            menu.setLocation(display.map(menuButton.getParent(), null, mLoc));
            menu.setVisible(true);
        });
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(layoutData);
        label.setText("Returned Type");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        Combo returnedTypeBox = new Combo(center, SWT.READ_ONLY);
        returnedTypeBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        returnedTypeBox.setFont(topFont);
        returnedTypeBox.setItems("GluonObservableObject", "GluonObservableList");
        returnedTypeBox.setText(returnedTypeBox.getItem(0));
        returnedTypeBox.setEnabled(false);
        
        functionsBox.addModifyListener(e -> {
            final String remoteFunction = functionsBox.getText();
            if (remoteFunction != null) {
                functionGivenNameText.setText(remoteFunction);
                functionGivenNameText.setEditable(true);
                resultTypeText.setText("String"); 
                resultTypeText.setEditable(true);
                menuButton.setEnabled(true);
                returnedTypeBox.setEnabled(true);
            } else {
                functionGivenNameText.setText("");
                functionGivenNameText.setEditable(false);
                resultTypeText.setText("");
                resultTypeText.setEditable(false);
                menuButton.setEnabled(false);
                returnedTypeBox.setEnabled(false);
            }
        });
        
        final ModifyListener textListener = e -> {
            final String funText = functionGivenNameText.getText();
            final String typeText = resultTypeText.getText();
            // TODO: Add validation
            if (funText != null && ! funText.isEmpty() && ! funText.contains(" ") && 
                    funText.substring(0, 1).matches("[a-zA-Z]") &&
                    typeText != null && ! typeText.isEmpty()) {
                code.setFunctionName(functionsBox.getText());
                code.setFunctionGivenName(functionGivenNameText.getText());
                code.setResultType(resultTypeText.getText());
                code.setReturnedType(returnedTypeBox.getText());
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            } else {
                code.setFunctionName(null);
                code.setFunctionGivenName(null);
                code.setResultType(null);
                code.setReturnedType(null);
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
        };
	    
        functionGivenNameText.addModifyListener(textListener);
        resultTypeText.addModifyListener(textListener);

        center.addDisposeListener(e -> {
            functionGivenNameText.removeModifyListener(textListener);
            resultTypeText.removeModifyListener(textListener);
        });

        center.addPaintListener(new PaintListener() {

            @Override
            public void paintControl(PaintEvent e) {
                retrieveFunctionsTask();
                center.removePaintListener(this);
            }
        });

        return center;
    }
    
    @Override
    protected Control createButtonBar(Composite parent) {
        final Composite buttonBar = (Composite) super.createButtonBar(parent);

        errorLabel = new Label(buttonBar, SWT.LEFT);
        errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
        errorLabel.setText("There are no defined remote functions");
        errorLabel.setForeground(display.getSystemColor(SWT.COLOR_RED));
        errorLabel.setVisible(false);
        errorLabel.setFont(topFont);
        errorLabel.moveAbove(super.buttonControl);

        return buttonBar;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Apply");
        ok.setEnabled(false);
        setButtonLayoutData(ok);
        
        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Cancel");
        setButtonLayoutData(cancel);
        cancel.addListener(SWT.Selection, e -> {
            if (futureTask != null && ! futureTask.isDone()) {
                futureTask.cancel(true);
            }
            code.setFunctionName(null);
            code.setFunctionGivenName(null);
            code.setResultType(null);
            code.setReturnedType(null);
        });
    }
    
    private void retrieveFunctionsTask() {
        disableDialog();
        futureTask = DialogUtils.supplyAsync(new CodeTask(applicationIdeKey))
            .exceptionally(ex -> {
                ex.printStackTrace();
                restoreDialog(null);
                return null;
            })
            .thenAccept(this::restoreDialog);
    }
    
    private void disableDialog() {
        display.asyncExec(() -> {
            functionsBox.removeAll();
            enableControls(false);
            composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_WAIT));
        });
    }
	
    private void restoreDialog(List<String> functionsList) {
        display.asyncExec(() -> { 
            composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_ARROW));
            enableControls(true);
            if (functionsList == null || functionsList.isEmpty()) {
                errorLabel.setVisible(true);
            } else {
                functionsBox.setItems(functionsList.stream().toArray(size -> new String[size]));
                errorLabel.setVisible(false);
            }
            getButton(IDialogConstants.OK_ID).setEnabled(! errorLabel.isVisible());
        });
    }

    private void enableControls(boolean enable) {
        functionsBox.setEnabled(enable);
    }
    
}
