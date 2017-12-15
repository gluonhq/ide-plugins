package com.gluonhq.eclipse.plugin.code;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import com.gluonhq.eclipse.plugin.menu.ProjectUtils;

public class InsertGluonFunctionHandler extends AbstractHandler {

    private ProjectUtils utils;

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow == null) {
            return null;
        }
        IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
        if (activePage == null) {
            return null;
        }
        IEditorPart editor = activePage.getActiveEditor();
        if (editor == null) {
            return null;
        }
        IEditorInput input = editor.getEditorInput();
        if (input == null || ! (input instanceof FileEditorInput)) {
            return null;
        }
        IFile file = ((FileEditorInput) input).getFile();
        if (file != null && file.getType() == IResource.FILE && file.getFileExtension().equals("java")) {
            utils = new ProjectUtils(file.getProject());
            if (utils.isGluonMobileProject()) {
                ISelection selection = HandlerUtil.getCurrentSelection(event);
                Display.getDefault().asyncExec(() -> new JCode(utils, selection,  (JavaEditor) editor));
            }
        }
        return null;
    }

}
