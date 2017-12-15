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
package com.gluonhq.plugin.function;

import java.util.Locale;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.gluonhq.plugin.dialogs.PluginDialog;
import com.gluonhq.plugin.templates.TemplateUtils;

public class FunctionSWT extends PluginDialog {

    private final Function function;
    
    public FunctionSWT(Shell shell) {
        super(shell);

        function = new Function();
    }

    public Function getFunction() {
        return function;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        
        createContent();
        
        return composite;
    }
    
    private void createContent() {
        createTopContent("Gluon Function", FunctionSWT.class.getResourceAsStream("G_Grey_charm.png"));
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
        label.setText("Add a Gluon Function subproject to your project by providing a function name and a package name. Later on, this function can be pushed to Gluon CloudLink at any time using Upload Gluon Function.");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Function Name");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        Text functionText = new Text(center, SWT.FILL | SWT.BORDER);
        functionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        functionText.setFont(topFont);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("Package Name");
        label.setFont(topFont);
        label.setBackground(backColor);
        
        Text packageText = new Text(center, SWT.FILL | SWT.BORDER);
        packageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        packageText.setFont(topFont);
        
        label = new Label(center, SWT.RIGHT);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("");
        label.setFont(topFont);
        
        final ModifyListener textListener = e -> {
            final String funText = functionText.getText();
            final String pacText = packageText.getText();
            // TODO: Add validation
            if (funText != null && ! funText.isEmpty() && ! funText.contains(" ") && funText.substring(0, 1).matches("[a-zA-Z]") &&
                            pacText != null && ! pacText.isEmpty() && TemplateUtils.isValidPackageName(pacText)) {
                function.setFunctionName(TemplateUtils.upperCaseWord(funText));
                function.setPackageName(pacText.toLowerCase(Locale.ROOT));
                getButton(IDialogConstants.OK_ID).setEnabled(true);
            } else {
                function.setFunctionName(null);
                function.setPackageName(null);
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
        };
	    
        functionText.addModifyListener(textListener);
        packageText.addModifyListener(textListener);

        center.addDisposeListener(e -> {
            functionText.removeModifyListener(textListener);
            packageText.removeModifyListener(textListener);
        });

        return center;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);

        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Create");
        ok.setEnabled(false);
        setButtonLayoutData(ok);

        Button cancel = getButton(IDialogConstants.CANCEL_ID);
        cancel.setText("Cancel");
        setButtonLayoutData(cancel);
        cancel.addListener(SWT.Selection, e -> {
            function.setFunctionName(null);
            function.setPackageName(null);
        });
    }
    
}
