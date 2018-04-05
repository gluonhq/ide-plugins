/*
 * Copyright (c) 2018, Gluon Software
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
package com.gluonhq.plugin.netbeans.template.visuals;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class GluonBasePanelVisual extends JPanel implements ChangeListener, DocumentListener {

    protected GluonBaseWizardPanel panel;
    
    protected abstract boolean valid(WizardDescriptor wizardDescriptor);
    
    protected abstract void store(WizardDescriptor d);
    
    protected abstract void read(WizardDescriptor settings);
    
    protected void validate(WizardDescriptor d) throws WizardValidationException {
        // nothing to validate
    }
    
    // Implementation of ChangeListener --------------------------------------
    @Override
    public void stateChanged(ChangeEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    // Implementation of DocumentListener --------------------------------------
    @Override
    public void changedUpdate(DocumentEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        panel.fireChangeEvent(); // Notify that the panel changed
    }
    
}
