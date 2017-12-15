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
package com.gluonhq.eclipse.plugin.menu;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

public class ValidationSourceProvider extends AbstractSourceProvider {

    public static final String SHOW_MENU = "com.gluonhq.eclipse.plugin.menu.show";
    private final static String GLUON_FOUND = "gluonFound"; 
    private final static String GLUON_NOT_FOUND = "gluonNotFound";
    
    public static final String SHOW_FUNCTION_MENU = "com.gluonhq.eclipse.plugin.menu.function.show";
    private final static String GLUON_FUNCTION_FOUND = "gluonFunctionFound"; 
    private final static String GLUON_FUNCTION_NOT_FOUND = "gluonFunctionNotFound";
    
    private final ISelectionListener listener;
    private boolean gluonFound = false; 
    private boolean gluonFunctionFound = false; 
	
    public ValidationSourceProvider() {
        listener = (IWorkbenchPart part, ISelection sel) -> {
            if (! (sel instanceof IStructuredSelection)) {
                setGluonFound(false);
                setGluonFunctionFound(false);
                return;
            }
            IStructuredSelection ss = (IStructuredSelection) sel;
            Object o = ss.getFirstElement();
            if (o == null) {
                setGluonFound(false);
                setGluonFunctionFound(false);
                return;
            }
            
            IContainer container = null;
            if (!(o instanceof IContainer)) {
                // Package Explorer
                IProject project = (IProject) Platform.getAdapterManager().getAdapter(o, IProject.class);
                if (project != null) {
                    container = (IContainer) project;
                }
            } else {
                // Project Explorer
                container = (IContainer) o;
            }
            if (container != null && container.isAccessible()) {
                ProjectUtils utils = new ProjectUtils(container);
                setGluonFound(utils.isGluonProject());
                setGluonFunctionFound(utils.isGluonFunctionProject());
            }
        };
        //Register listener
        Display.getDefault().asyncExec(() -> {
            ISelectionService ss = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
            ss.addSelectionListener(listener); 
        });
    }
	
    @Override
    public void dispose() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
            ISelectionService ss = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService();
            ss.removeSelectionListener(IPageLayout.ID_PROJECT_EXPLORER, listener); 
            ss.removeSelectionListener("org.eclipse.jdt.ui.PackageExplorer", listener); 
        }
    }

    @Override
    public Map<String, Object> getCurrentState() {
        Map<String, Object> map = new HashMap<>(1);
		
        String currentState = gluonFound ? GLUON_FOUND : GLUON_NOT_FOUND; 
        map.put(SHOW_MENU, currentState);
        
        currentState = gluonFunctionFound ? GLUON_FUNCTION_FOUND : GLUON_FUNCTION_NOT_FOUND; 
        map.put(SHOW_FUNCTION_MENU, currentState);
        
        return map;
    }

    @Override
    public String[] getProvidedSourceNames() {
        return new String[] { SHOW_MENU, SHOW_FUNCTION_MENU };
    }
	
    private void setGluonFound(boolean gluonFound) { 
        if (this.gluonFound == gluonFound) return;
		
        this.gluonFound = gluonFound;  
        String currentState = gluonFound ? GLUON_FOUND : GLUON_NOT_FOUND; 
        fireSourceChanged(ISources.WORKBENCH, SHOW_MENU, currentState); 
    }
    
    private void setGluonFunctionFound(boolean gluonFunctionFound) { 
        if (this.gluonFunctionFound == gluonFunctionFound) return;
		
        this.gluonFunctionFound = gluonFunctionFound;  
        String currentState = gluonFunctionFound ? GLUON_FUNCTION_FOUND : GLUON_FUNCTION_NOT_FOUND; 
        fireSourceChanged(ISources.WORKBENCH, SHOW_FUNCTION_MENU, currentState); 
    }

}
