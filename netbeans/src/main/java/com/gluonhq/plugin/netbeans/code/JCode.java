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

//import com.gluonhq.plugin.code.Code;
//import com.gluonhq.plugin.code.CodeFX;
//import com.gluonhq.plugin.netbeans.menu.ProjectUtils;
//import com.sun.source.tree.*;
//import javafx.application.Platform;
//import javafx.embed.swing.JFXPanel;
//import javafx.scene.Scene;
//import org.netbeans.api.java.source.JavaSource;
//import org.netbeans.api.java.source.JavaSource.Phase;
//import org.netbeans.api.java.source.ModificationResult;
//import org.netbeans.api.java.source.TreeMaker;
//import org.openide.util.Exceptions;
//
//import javax.lang.model.element.Modifier;
import javax.swing.*;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;

public class JCode extends JFrame {
    
    /*private final ProjectUtils utils;
    private final JavaSource source;

    public JCode(ProjectUtils utils, JavaSource source) {
        this.utils = utils;
        this.source = source;
        getContentPane().add(runCode());
    }
    
    private JFXPanel runCode() {
        final JFXPanel fxPanel = new JFXPanel();
        fxPanel.setSize(600, 372);
        Platform.setImplicitExit(false);
        Platform.runLater(() -> {
            CodeFX codeFX = new CodeFX();
            Code code = codeFX.getCode();
            code.addPropertyChangeListener(e -> {
                if (code.getFunctionName() != null && code.getFunctionGivenName() != null &&
                        code.getResultType() != null && code.getReturnedType() != null) {
                    runTask(code);
                }
                dispose();
            });
            final Scene scene = new Scene(codeFX);
            fxPanel.setScene(scene);
            
            codeFX.loadRemoteFunctions(utils.getCloudLinkIdeKey());
        });
        
        return fxPanel;
    }

    private void runTask(Code code) {
        try {
            source.runModificationTask(workingCopy -> {
                workingCopy.toPhase(JavaSource.Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                cut.getTypeDecls().stream()
                        .filter(typeDecl -> Tree.Kind.CLASS == typeDecl.getKind())
                        .map(ClassTree.class::cast)
                        .forEach(clazz -> {
                            ModifiersTree methodModifiers =
                                    make.Modifiers(Collections.<Modifier>singleton(Modifier.PUBLIC),
                                            Collections.<AnnotationTree>emptyList());
                            List<VariableTree> params = new ArrayList<>();
                            if (code.getResultType().equals("T")) {
                                params.add(make.Variable(make.Modifiers(Collections.<Modifier>singleton(Modifier.FINAL),
                                            Collections.<AnnotationTree>emptyList()),
                                            "clazz",
                                            make.Identifier("Class<T>"),
                                            null));
                            }
                            params.add(make.Variable(make.Modifiers(Collections.<Modifier>singleton(Modifier.FINAL),
                                            Collections.<AnnotationTree>emptyList()),
                                            "values",
                                            make.Identifier("String..."),
                                            null));
                            List<TypeParameterTree> types = new ArrayList<>();
                            if (code.getResultType().equals("T")) {
                                types.add(make.TypeParameter("T", new ArrayList<>()));
                            }
                            MethodTree newMethod =
                                    make.Method(methodModifiers,
                                            code.getFunctionGivenName(),
                                            make.Type(code.getReturnedType() + "<" + code.getResultType() + ">"),
                                            types,
                                            params,
                                            Collections.<ExpressionTree>emptyList(),
                                            "{ \n" +
                                            (code.getReturnedType().endsWith("Object") ? "RemoteFunctionObject" : "RemoteFunctionList") + 
                                                    " function = RemoteFunctionBuilder\n" +
                                            "     .create(\"" + code.getFunctionName() + "\")\n" +
                                            //"                .param(\"$paramName1\", values[0])\n" +
                                            //"                .param(\"$paramName2\", values[1])\n" +
                                            "     ." + (code.getReturnedType().endsWith("Object") ? "object" : "list") + "();\n" +
                                            "return function.call(" + (code.getResultType().equals("T") ? "clazz" : code.getResultType() + ".class") + "); \n}",
                                            null);
                            ClassTree modifiedClazz = make.addClassMember(clazz, newMethod);
                            workingCopy.rewrite(clazz, modifiedClazz);
                        });
            }).commit();
            
            fixImports("com.gluonhq.connect." + code.getReturnedType(), 
                    "com.gluonhq.cloudlink.client.data.RemoteFunctionBuilder",
                    "com.gluonhq.cloudlink.client.data." + 
                            (code.getReturnedType().endsWith("Object") ? "RemoteFunctionObject" : "RemoteFunctionList"));
            // TODO: add connect and cloudlink dependencies to build.gradle
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void fixImports(String... importsDeclaration) {
        for (String importDeclaration : importsDeclaration) {
            Thread t = new Thread (() -> {
                try {
                    ModificationResult task = source.runModificationTask(workingCopy -> {
                        workingCopy.toPhase(Phase.ELEMENTS_RESOLVED);
                        
                        CompilationUnitTree cut = workingCopy.getCompilationUnit();
                        if (cut.getImports().stream()
                                .map(ImportTree::getQualifiedIdentifier)
                                .filter(MemberSelectTree.class::isInstance)
                                .map(MemberSelectTree.class::cast)
                                .map(ms -> ms.getExpression().toString() + "." + ms.getIdentifier().toString())
                                .noneMatch(pkg -> pkg.equals(importDeclaration))) {
                            TreeMaker make = workingCopy.getTreeMaker();
                            CompilationUnitTree copy = make.addCompUnitImport(cut,
                                    make.Import(make.Identifier(importDeclaration), false));
                            workingCopy.rewrite(cut, copy);
                        }
                    });
                    task.commit();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            t.start();
            try {
                t.join();
            } catch (InterruptedException ex) { }
        }
    }
    */
}
