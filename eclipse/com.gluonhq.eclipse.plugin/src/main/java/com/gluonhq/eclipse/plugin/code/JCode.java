/*
 * Copyright (c) 2017, 2020, Gluon Software
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
package com.gluonhq.eclipse.plugin.code;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.core.manipulation.StubUtility;
import org.eclipse.jdt.internal.corext.CorextMessages;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2Core;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.refactoring.util.RefactoringASTParser;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.Resources;
import org.eclipse.jdt.internal.corext.util.ValidateEditException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.text.edits.TextEdit;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;
import com.gluonhq.plugin.code.Code;
import com.gluonhq.plugin.code.CodeSWT;

public class JCode {
	
    private final ProjectUtils utils;
    private final ISelection selection;
    private final JavaEditor javaEditor;
    
    public JCode(ProjectUtils utils, ISelection selection, JavaEditor javaEditor) {
        this.utils = utils;
        this.selection = selection;
        this.javaEditor = javaEditor;

        if (selection != null) {
            runCode();
        }
    }

    private void runCode() {
        CodeSWT codeSWT = new CodeSWT(null, utils.getCloudLinkIdeKey());
        Code code = codeSWT.getCode();

        codeSWT.open();
        
        if (code.getFunctionName() != null && code.getFunctionGivenName() != null &&
            code.getResultType() != null && code.getReturnedType() != null) {
            runTask(code);
        }
    		
    }
    
    private void runTask(Code code) {
        IType type = null;
        if (selection.isEmpty() || (selection instanceof ITextSelection)) {
            // Insert function called from active editor
            try {
                IJavaElement[] items = SelectionConverter.codeResolveForked(javaEditor, true);
                if (items.length > 0) {
                    type = (IType) items[0].getAncestor(IJavaElement.TYPE);
                } else {
                    IJavaElement element = SelectionConverter.getElementAtOffset(javaEditor);
                    if (element != null) {
                        type = (IType) element.getAncestor(IJavaElement.TYPE);
                    }
                }
            } catch (InvocationTargetException | InterruptedException | JavaModelException e) {
                e.printStackTrace();
            }
        } else if (selection instanceof IStructuredSelection) { 
            // Insert function called from Project Explorer
            IStructuredSelection selected = (IStructuredSelection) selection;
            Object firstelement = selected.getFirstElement();
            try {
                if (firstelement instanceof IType) {
                    type = (IType) firstelement;
                } else if (firstelement instanceof ICompilationUnit) {
                    type = ((ICompilationUnit) firstelement).findPrimaryType();
                }
                else if (firstelement instanceof IField) {
                    type= ((IField) firstelement).getDeclaringType();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (type != null) {
            try {
                final ICompilationUnit unit = type.getCompilationUnit();
                CompilationUnit astRoot = new RefactoringASTParser(ASTProvider.SHARED_AST_LEVEL).parse(unit, true);

                final AST ast = astRoot.getAST();
                final ASTRewrite astRewrite = ASTRewrite.create(ast);
                ListRewrite rewrite = null;
                if (type.isAnonymous()) {
                    final ClassInstanceCreation creation = (ClassInstanceCreation) ASTNodes
                                .getParent(NodeFinder.perform(astRoot, type.getNameRange()), ClassInstanceCreation.class);
                    if (creation != null) {
                        final AnonymousClassDeclaration declaration = creation.getAnonymousClassDeclaration();
                        if (declaration != null) {
                            rewrite = astRewrite.getListRewrite(declaration, AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
                        }
                    }
                } else {
                    final AbstractTypeDeclaration declaration = (AbstractTypeDeclaration) ASTNodes
                                .getParent(NodeFinder.perform(astRoot, type.getNameRange()), AbstractTypeDeclaration.class);
                    if (declaration != null) {
                        rewrite = astRewrite.getListRewrite(declaration, declaration.getBodyDeclarationsProperty());
                    }	
                }

                String functionContent = generateFunctionContent(code);
                ASTNode insertion = StubUtility2Core.getNodeToInsertBefore(rewrite, null);

                final String delimiter = StubUtility.getLineDelimiterUsed(type);
                final MethodDeclaration declaration = (MethodDeclaration) rewrite.getASTRewrite()
                                .createStringPlaceholder(CodeFormatterUtil.format(CodeFormatter.K_CLASS_BODY_DECLARATIONS, 
                                                functionContent, 0, delimiter, (IJavaProject) null), ASTNode.METHOD_DECLARATION);
                if (insertion != null) {
                    rewrite.insertBefore(declaration, insertion, null);
                } else {
                    rewrite.insertLast(declaration, null);
                }

                TextEdit fEdit = astRewrite.rewriteAST();
                applyEdit(unit, fEdit, true, null);

                ImportRewrite importRewrite = StubUtility.createImportRewrite(unit, true);
                importRewrite.addImport("com.gluonhq.connect." + code.getReturnedType()); //$NON-NLS-1$
                importRewrite.addImport("com.gluonhq.cloudlink.client.data.RemoteFunctionBuilder"); //$NON-NLS-1$
                importRewrite.addImport("com.gluonhq.cloudlink.client.data." + 
                    (code.getReturnedType().endsWith("Object") ? "RemoteFunctionObject" : "RemoteFunctionList")); //$NON-NLS-1$
                TextEdit edit = importRewrite.rewriteImports(null);
                applyEdit(unit, edit, true, null);

            } catch (CoreException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No valid type found");
        }
    }
    
    private String generateFunctionContent(Code code) throws JavaModelException {
        String lineDelim = "\n"; //$NON-NLS-1$
        StringBuilder buf = new StringBuilder();
        buf.append("public final ").append(code.getResultType().equals("T") ? "<T>" : "").append(" ").append(code.getReturnedType()).append("<").append(code.getResultType()).append(">");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        buf.append(" ").append(code.getFunctionGivenName()).append("(").append(code.getResultType().equals("T") ? "final Class<T> clazz, " : "").append("final String... values) {");  //$NON-NLS-1$//$NON-NLS-2$
        buf.append(lineDelim);
        buf.append(code.getReturnedType().endsWith("Object") ? "RemoteFunctionObject" : "RemoteFunctionList").append(" function = RemoteFunctionBuilder");
        buf.append(lineDelim);
        buf.append("     .create(\"").append(code.getFunctionName()).append("\")");
        buf.append(lineDelim);
        buf.append("     .").append(code.getReturnedType().endsWith("Object") ? "object" : "list").append("();");
        buf.append(lineDelim);
        buf.append("return function.call(").append(code.getResultType().equals("T") ? "clazz" : code.getResultType() + ".class").append(");");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        buf.append(lineDelim);
        buf.append("}"); //$NON-NLS-1$

        return buf.toString();
    }
    
    // From JavaModelUtil
    private static void applyEdit(ICompilationUnit cu, TextEdit edit, boolean save, IProgressMonitor monitor) throws CoreException, ValidateEditException {
        IFile file = (IFile) cu.getResource();
        if (!save || !file.exists()) {
            cu.applyTextEdit(edit, monitor);
        } else {
            if (monitor == null) {
                monitor = new NullProgressMonitor();
            }
            monitor.beginTask(CorextMessages.JavaModelUtil_applyedit_operation, 2);
            try {
                IStatus status= Resources.makeCommittable(file, null);
                if (!status.isOK()) {
                    throw new ValidateEditException(status);
                }
                cu.applyTextEdit(edit, SubMonitor.convert(monitor));
                cu.save(SubMonitor.convert(monitor), true);
            } finally {
                monitor.done();
            }
        }
    }
}
