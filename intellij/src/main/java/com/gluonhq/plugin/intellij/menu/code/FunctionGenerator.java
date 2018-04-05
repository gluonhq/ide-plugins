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

import com.gluonhq.plugin.intellij.menu.ProjectUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

public class FunctionGenerator extends AnAction {

    private ProjectUtils utils;
    private PsiJavaFile javaFile;
    private PsiElement currentElement;

    @Override
    public void update(AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(false);
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null) {
            return;
        }
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (! (psiFile instanceof PsiJavaFile)) {
            // Not a Java file
            return;
        }
        javaFile = (PsiJavaFile) psiFile;

        currentElement = javaFile.findElementAt(e.getData(CommonDataKeys.CARET).getOffset());
        if (PsiTreeUtil.getParentOfType(currentElement, PsiMethod.class) != null) {
            // Can't add method inside other method
            return;
        }
        if (PsiTreeUtil.getParentOfType(currentElement, PsiClass.class) == null) {
            // Can't add method outside a class
            return;
        }
        if (PsiTreeUtil.getParentOfType(currentElement, PsiComment.class) != null) {
            // Can't add method inside comments
            return;
        }

        // find gluon mobile project
        Module module = ProjectFileIndex.SERVICE.getInstance(e.getProject()).getModuleForFile(file);
        if (module == null) {
            // no module found
            return;
        }

        VirtualFile moduleFile = module.getModuleFile();
        if (moduleFile == null || moduleFile.getParent() == null) {
            // if the file is in a root project, it has no parent
            return;
        }

        String subProjectName = moduleFile.getParent().getName();
        if (subProjectName == null || subProjectName.isEmpty()) {
            // no subproject name found
            return;
        }

        VirtualFile subProject = e.getProject().getBaseDir().findChild(subProjectName);
        if (subProject == null) {
            // no subproject file found
            return;
        }
        utils = new ProjectUtils(subProject);
        e.getPresentation().setEnabledAndVisible(utils.isGluonMobileProject());
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (! utils.cloudLinkSignIn()) {
            return;
        }

        JCode jCode = new JCode(utils, e.getProject(), javaFile, currentElement);
        utils.showDialog(jCode);
    }

}
