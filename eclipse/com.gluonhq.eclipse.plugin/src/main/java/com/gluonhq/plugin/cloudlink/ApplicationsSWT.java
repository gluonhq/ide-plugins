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
package com.gluonhq.plugin.cloudlink;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.json.Json;
import javax.json.JsonObject;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gluonhq.plugin.dialogs.DialogUtils;
import com.gluonhq.plugin.dialogs.PluginDialog;

public class ApplicationsSWT extends PluginDialog {

    private Combo currentAppBox;
    
    private final Credentials credentials;
    private final String jsonConfig;
    private final boolean allowDisableApply;
    private final List<Application> applicationsList;
    private Application existingApp;
    private Text keyText, secretText;
    private Button logout;
    private CompletableFuture<Void> futureTask;

    public ApplicationsSWT(Shell shell, String userKey, boolean keepLogged, String jsonConfig, boolean allowDisableApply) {
        super(shell);
        this.credentials = new Credentials(userKey, keepLogged, null);
        this.jsonConfig = jsonConfig;
        this.allowDisableApply = allowDisableApply;
        
        applicationsList = new ArrayList<>();
    }

    public Credentials getCredentials() {
        return credentials;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        
        createContent();
        
        return composite;
    }
    
    private void createContent() {
        createTopContent("Gluon CloudLink Applications", ApplicationsSWT.class.getResourceAsStream("G_Cyan_Cloud.png"));
        createCenterContent();
        createBottomContent();
    }

    @Override
    protected Composite createCenterContent() {
        Composite center = super.createCenterContent();
		
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 20;
        layout.marginRight = 10;
        layout.horizontalSpacing = 10;
        layout.verticalSpacing = 15;
        center.setLayout(layout);
        center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        Label label = new Label(center, SWT.WRAP  | SWT.LEFT);
        label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
        label.setText("Select from the list below the Gluon CloudLink application to which this mobile application will be linked to. Its key and secret tokens will be added to the mobile application.");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Applications");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        currentAppBox = new Combo(center, SWT.READ_ONLY);
        currentAppBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        currentAppBox.setFont(topFont);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Key");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        keyText = new Text(center, SWT.FILL | SWT.BORDER);
        keyText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        keyText.setFont(topFont);
        keyText.setEditable(false);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Secret");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        secretText = new Text(center, SWT.FILL | SWT.BORDER);
        secretText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        secretText.setFont(topFont);
        secretText.setEditable(false);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
        label.setText("");
        label.setFont(topFont);
        
        currentAppBox.addModifyListener(e -> {
            final Application app = applicationsList.stream()
                    .filter(a -> a.getName().equals(currentAppBox.getText()))
                    .findFirst()
                    .orElse(null);
            if (app != null) {
                keyText.setText(app.getIdentifier());
                secretText.setText(app.getSecret());
            } else {
                keyText.setText("");
                secretText.setText("");
            }
            credentials.setApplication(app);
            getButton(IDialogConstants.OK_ID).setEnabled(! (app == null || (allowDisableApply && app.equals(existingApp))));
        });
        
        center.addPaintListener(new PaintListener() {
			
            @Override
            public void paintControl(PaintEvent e) {
                retrieveAppsTask(); 
                center.removePaintListener(this);
            }
        });
        return center;
    }
    
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        centerDialog(shell, 600, 400);
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        final Composite buttonBar = (Composite) super.createButtonBar(parent);

        logout = new Button(buttonBar, SWT.CENTER);
        final GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        layoutData.widthHint = 100;
        logout.setLayoutData(layoutData);
        logout.setText("Log Out");
        logout.setVisible(credentials.isKeepLogged());
        logout.addListener(SWT.Selection, e -> {
            credentials.setApplication(null);
            credentials.setUserKey(false, null);
            close();
        });
        logout.moveAbove(super.buttonControl);

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
            credentials.setApplication(null); 
        });
    }
    
    private void retrieveAppsTask() {
        if (credentials.getUserKey() == null) {
            return;
        }
        disableDialog();

        futureTask = DialogUtils.supplyAsync(new ApplicationsTask(credentials.getUserKey()))
            .exceptionally(ex -> {
                ex.printStackTrace();
                restoreDialog(null);
                return null;
            })
            .thenAccept(this::restoreDialog);
    }
    
    private void disableDialog() {
        display.asyncExec(() -> {
            applicationsList.clear();
            currentAppBox.removeAll();
            enableControls(false);
            composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_WAIT));
        });
    }
	
    private void restoreDialog(List<Application> list) {
        display.asyncExec(() -> { 
                composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_ARROW));
                enableControls(true);
                if (list != null && ! list.isEmpty()) {
                    applicationsList.addAll(list);
                    currentAppBox.setItems(list.stream().map(Application::getName).toArray(size -> new String[size]));

                if (jsonConfig != null && !jsonConfig.isEmpty()) {
                    JsonObject object = Json.createReader(new StringReader(jsonConfig)).readObject()
                            .getJsonObject("gluonCredentials");
                    if (object != null) {
                        existingApp = list.stream()
                            .filter(app -> app.getIdentifier().equals(object.getString("applicationKey")) &&
                                    app.getSecret().equals(object.getString("applicationSecret")))
                            .findFirst()
                            .orElse(null);
                        if (existingApp != null) {
                            currentAppBox.setText(existingApp.getName());
                        }
                    }
                }
            }
        });
    }

    private void enableControls(boolean enable) {
        currentAppBox.setEnabled(enable);
        keyText.setEnabled(enable);
        secretText.setEnabled(enable);
        logout.setEnabled(enable);
        getButton(IDialogConstants.OK_ID).setEnabled(enable);
    }

}
