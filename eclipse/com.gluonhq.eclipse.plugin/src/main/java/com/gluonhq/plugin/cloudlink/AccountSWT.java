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

import java.util.concurrent.CompletableFuture;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;

import com.gluonhq.plugin.dialogs.DialogUtils;
import com.gluonhq.plugin.dialogs.PluginDialog;

public class AccountSWT extends PluginDialog {

    private final Color linkColor;
    
    private final Credentials credentials;
    private boolean rememberMe = true;
    private Label errorLabel;
    private Text userText, passwordText;
    private Button checkBox;
    private CompletableFuture<Void> futureTask;

    public AccountSWT(Shell shell) {
        super(shell);
        this.credentials = new Credentials();
        
        linkColor = new Color(display, 0, 148, 203);
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
    	createTopContent("Gluon CloudLink Applications", AccountSWT.class.getResourceAsStream("G_Cyan_Cloud.png"));
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
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gridData.widthHint = 560;
        label.setLayoutData(gridData);
        label.setText("By signing in with your Gluon CloudLink user and password, you will be able to access your Gluon CloudLink applications and link your mobile application to one of them.");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("User");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        userText = new Text(center, SWT.FILL | SWT.BORDER);
        userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        userText.setFont(topFont);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Password");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        passwordText = new Text(center, SWT.PASSWORD | SWT.FILL | SWT.BORDER);
        passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        passwordText.setFont(topFont);
        
        final ModifyListener textListener = e -> {
            getButton(IDialogConstants.OK_ID).setEnabled(userText.getText() != null && ! userText.getText().isEmpty() && 
                            passwordText.getText() != null && ! passwordText.getText().isEmpty());
        };
        userText.addModifyListener(textListener);
        passwordText.addModifyListener(textListener);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1));
        label.setText("");
        label.setFont(topFont);
        
        checkBox = new Button(center, SWT.CHECK);
        checkBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        checkBox.setSelection(true);
        checkBox.setText("Remember Me");
        checkBox.setFont(topFont);
        checkBox.addListener(SWT.Selection, e -> rememberMe = checkBox.getSelection());
        checkBox.setBackground(backColor);
        
        center.addDisposeListener(e -> {
            userText.removeModifyListener(textListener);
            passwordText.removeModifyListener(textListener);
        });
        return center;
    }
    
    @Override
    protected Composite createBottomContent() {
        Composite bottom = super.createBottomContent();
    
        Hyperlink linkIO = new Hyperlink(bottom, SWT.NONE);
        linkIO.setText("Create Gluon Dashboard Account");
        linkIO.setHref("http://gluonhq.com/products/cloudlink/buy");
        linkIO.setUnderlined(false);
        linkIO.setForeground(linkColor);
        linkIO.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
        linkIO.setBackground(backColor);
        linkIO.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                openURL(linkIO.getHref().toString());
            }
        });
        linkIO.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                composite.setFocus();
            }
        });
        
        Hyperlink linkPass = new Hyperlink(bottom, SWT.NONE);
        linkPass.setText("Recover password");
        linkPass.setHref("https://gluonhq.com/my-account/lost-password/");
        linkPass.setForeground(linkColor);
        linkPass.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true, 1, 1));
        linkPass.setBackground(backColor);
        linkPass.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                openURL(linkPass.getHref().toString());
            }
        });
        linkPass.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                composite.setFocus();
            }
        });
	    
        return bottom;
    }
    
    @Override
    protected Control createButtonBar(Composite parent) {
        final Composite buttonBar = (Composite) super.createButtonBar(parent);

        errorLabel = new Label(buttonBar, SWT.LEFT);
        errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        errorLabel.setText("The provided credentials are invalid");
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
        ok.setText("Sign In");
        ok.setEnabled(false);
        setButtonLayoutData(ok);
        ok.addListener(SWT.Selection, e -> {
            disableDialog();

            futureTask = DialogUtils.supplyAsync(new AccountTask(userText.getText(), passwordText.getText()))
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    restoreDialog(null); 
                    return null;
                })
                .thenAccept(this::restoreDialog);
        });

        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Cancel");
        setButtonLayoutData(cancel);
        cancel.addListener(SWT.Selection, e -> {
            if (futureTask != null && ! futureTask.isDone()) {
                futureTask.cancel(true);
            }
            credentials.setUserKey(false, null);
        });
    }
    
    private void disableDialog() {
        display.asyncExec(() -> {
            enableControls(false);
            errorLabel.setVisible(false);
            credentials.setUserKey(false, null);
            composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_WAIT));
        });
    }

    private void restoreDialog(String key) {
        display.asyncExec(() -> {
            errorLabel.setVisible(key == null); 
            composite.getParent().setCursor(new Cursor(display, SWT.CURSOR_ARROW));
            enableControls(true);
            if (key != null && ! key.isEmpty()) {
                credentials.setUserKey(rememberMe, key);
                close();
            }
        });
    }
    
    private void enableControls(boolean enable) {
        userText.setEnabled(enable);
        passwordText.setEnabled(enable);
        checkBox.setEnabled(enable);
        getButton(IDialogConstants.OK_ID).setEnabled(enable);
    }
    
    @Override
    protected void okPressed() {
        // NO-OP, removing default event handler
    }
    
    @Override
    public boolean close() {
        linkColor.dispose();
        return super.close();
    }
    
}
