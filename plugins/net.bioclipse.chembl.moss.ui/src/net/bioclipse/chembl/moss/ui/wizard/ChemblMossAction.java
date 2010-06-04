/* Copyright (c) 2010  Egon Willighagen <egonw@users.sf.net>
 *               2010  Annsofie Andersson <annzi.andersson@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contact: http://www.bioclipse.net/
 */
package net.bioclipse.chembl.moss.ui.wizard;


import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ChemblMossAction implements IObjectActionDelegate {

	private IWorkbenchPart part;
    private ISelection selection;
    private IResource parent;
    
    public ChemblMossAction() {
        super();
    }
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		part = targetPart;
		
	}

	@Override
	public void run(IAction action) {
		ChemblMossWizard wizard = new ChemblMossWizard();
		 WizardDialog dialog = new WizardDialog(part.getSite().getShell(),
	                wizard);

	        dialog.create();
	        dialog.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

}
