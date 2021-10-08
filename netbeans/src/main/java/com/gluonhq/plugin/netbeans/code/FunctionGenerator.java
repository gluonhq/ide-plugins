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
package com.gluonhq.plugin.netbeans.code;
/*
import com.gluonhq.plugin.netbeans.menu.ProjectUtils;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.netbeans.spi.editor.codegen.CodeGeneratorContextProvider;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import java.util.Collections;
import java.util.List;*/

public class FunctionGenerator {/* implements CodeGenerator {
    
    private final ProjectUtils utils;
    private final JTextComponent textComp;
    private final boolean gluonMobile;

    *//**
     *
     * @param context containing JTextComponent
     * registered by {@link CodeGeneratorContextProvider}
     *//*
    private FunctionGenerator(Lookup context) { 
        textComp = context.lookup(JTextComponent.class);
        Document document = textComp.getDocument();
        DataObject dobj = getDataObject(document);
        Project p = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
        utils = new ProjectUtils(p);
        gluonMobile = utils.isGluonMobileProject();
    }

    private List<? extends CodeGenerator> getCollection() {
        if (gluonMobile) {
            return Collections.singletonList(this);
        }
        return Collections.emptyList();
    }
    
    private static DataObject getDataObject(Document doc) {
        Object sdp = doc == null ? null : doc.getProperty(Document.StreamDescriptionProperty);
        if (sdp instanceof DataObject) {
            return (DataObject) sdp;
        }
        return null;
    }

    @MimeRegistration(mimeType = "text/x-java", service = CodeGenerator.Factory.class)
    public static class FunctionFactory implements CodeGenerator.Factory {
        
        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            final FunctionGenerator functionGenerator = new FunctionGenerator(context);
            return functionGenerator.getCollection();
        }
        
    }

    *//**
     * The name which will be inserted inside Insert Code dialog
     * @return 
     *//*
    @Override
    public String getDisplayName() {
        return "Gluon Function...";
    }

    *//**
     * This will be invoked when user chooses this Generator from Insert Code
     * dialog
     *//*
    @Override
    public void invoke() {
        try {
            Document doc = textComp.getDocument();
            JavaSource javaSource = JavaSource.forDocument(doc);
            if (javaSource == null) {
                return;
            } 
            
            if (! utils.cloudLinkSignIn()) {
                return;
            }
            
            JCode jCode = new JCode(utils, javaSource);
            utils.showDialog(jCode);
            
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }*/
    
}