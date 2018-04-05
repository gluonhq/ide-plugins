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
package com.gluonhq.plugin.intellij.menu.code;

import com.gluonhq.plugin.code.Code;
import com.gluonhq.plugin.code.CodeFX;
import com.gluonhq.plugin.intellij.menu.ProjectUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;

import javax.swing.*;

public class JCode extends JFrame {

    private final ProjectUtils utils;
    private final Project project;
    private final PsiJavaFile javaFile;
    private final PsiElement currentElement;

    public JCode(ProjectUtils utils, Project project, PsiJavaFile javaFile, PsiElement currentElement) {
        this.utils = utils;
        this.project = project;
        this.javaFile = javaFile;
        this.currentElement = currentElement;

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
                    ProjectUtils.runSafe(() ->
                            WriteCommandAction.runWriteCommandAction(project, () ->
                                    runTask(code)));
                }
                dispose();
            });
            final Scene scene = new Scene(codeFX);
            fxPanel.setScene(scene);

            ApplicationManager.getApplication().runReadAction(() ->
                    codeFX.loadRemoteFunctions(utils.getCloudLinkIdeKey()));
        });

        return fxPanel;
    }

    private void runTask(Code code) {
        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(project);
        fixImports(elementFactory, "com.gluonhq.connect", "com.gluonhq.cloudlink.client.data");

        PsiTypeElement type = elementFactory.createTypeElementFromText(code.getReturnedType() + "<" + code.getResultType() + ">", javaFile);
        PsiMethod method = elementFactory.createMethod(code.getFunctionName(), type.getType());
        if (code.getResultType().equals("T")) {
            PsiTypeParameterList typeParameterList = method.getTypeParameterList();
            typeParameterList.add(PsiElementFactory.SERVICE.getInstance(project).createTypeElementFromText("T", method));
        }

        PsiStatement functionBody = elementFactory.createStatementFromText(
                (code.getReturnedType().endsWith("Object") ? "RemoteFunctionObject" : "RemoteFunctionList") +
                " function = RemoteFunctionBuilder\n" +
                "     .create(\"" + code.getFunctionName() + "\")\n" +
                //"                .param(\"$paramName1\", values[0])\n" +
                //"                .param(\"$paramName2\", values[1])\n" +
                "     ." + (code.getReturnedType().endsWith("Object") ? "object" : "list") + "();", method);
        method.getBody().addAfter(functionBody, method.getBody().getLBrace());
        PsiStatement returnBody = elementFactory.createStatementFromText(
                "return function.call(" + (code.getResultType().equals("T") ? "clazz" : code.getResultType() + ".class") + ");", method);
        method.getBody().addBefore(returnBody, method.getBody().getRBrace());

        PsiParameterList parameterList = method.getParameterList();
        PsiManager manager = PsiManager.getInstance(project);
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        if (code.getResultType().equals("T")) {
            PsiTypeElement clazzType = elementFactory.createTypeElementFromText("Class<T>", method);
            PsiParameter paramT = elementFactory.createParameter("clazz", clazzType.getType());
            PsiUtil.setModifierProperty(paramT, PsiModifier.FINAL, true);
            parameterList.add(paramT);
        }

        PsiClassType javaLangString = PsiType.getJavaLangString(manager, scope);
        PsiEllipsisType ellipsisType = new PsiEllipsisType(javaLangString);
        PsiParameter paramS = elementFactory.createParameter("values", ellipsisType);
        PsiUtil.setModifierProperty(paramS, PsiModifier.FINAL, true);
        parameterList.add(paramS);

        PsiClass psiClass = PsiTreeUtil.getParentOfType(currentElement, PsiClass.class);
        psiClass.addAfter(method, currentElement);

        JavaCodeStyleManager.getInstance(project).optimizeImports(javaFile);
    }

    private void fixImports(PsiElementFactory elementFactory, String... importsDeclaration) {
        PsiImportList importList = javaFile.getImportList();
        if (importList == null) {
            return;
        }

        for (String importDeclaration : importsDeclaration) {
            for (PsiImportStatementBase importStatement : importList.getAllImportStatements()) {
                if (importStatement.getImportReference().getQualifiedName().equals(importDeclaration)) {
                    return;
                }
            }
            importList.add(elementFactory.createImportStatementOnDemand(importDeclaration));
        }
    }

}