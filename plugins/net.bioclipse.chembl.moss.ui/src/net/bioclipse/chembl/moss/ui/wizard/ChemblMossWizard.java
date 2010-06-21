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

import java.io.IOException;
import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import net.bioclipse.core.business.BioclipseException;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ChemblMossWizard extends Wizard implements INewWizard {

	private static final Logger logger = Logger.getLogger(ChemblMossWizard.class);
	private ChemblMossWizardPage1 firstpage;
	private ChemblMossWizardPage2 secondpage;
	private ChemblMossWizardPage3 thirdpage;
	private IChEMBLManager chembl;
	ChemblMossData data = new ChemblMossData();

	public ChemblMossWizard() {
		super();
		setWindowTitle("ChEMBL-MoSS");
		chembl = Activator.getDefault().getJavaChEMBLManager();
	}

	public void addPages(){
		firstpage = new ChemblMossWizardPage1("ChEMBL MoSS page");
		addPage(firstpage);

		secondpage = new ChemblMossWizardPage2("ChEMBL MoSS page 2");
		addPage(secondpage);
		
		thirdpage = new ChemblMossWizardPage3("ChEMBL MoSS page 3");
		addPage(thirdpage);
	}	

	
	@ Override 
	public boolean isHelpAvailable() {
		return true;
	}
	public boolean performCancel() {
	    boolean answer = MessageDialog.openConfirm(getShell(), "Confirmation", "Are you sure you want to cancel?");
	    if(answer)
	      return true;
	    else
	      return false;
	  }  
	/*Writes compounds to a MoSS supported file.
	 * Uses ChemblMossData to collect stored information.*/
	@Override
	public boolean performFinish() {
		try {
//			chembl.saveMossFormatFile(data.mm, data.matrix);
			if(data.matrix != null && data.matrix2 == null)
				chembl.MossSaveFormat(data.m, data.matrix);
			else if(data.matrix != null && data.matrix2 != null){
				chembl.MossSaveFormat(data.m, data.matrix, data.matrix2);}
			else if(data.matrix == null && data.matrix2 != null)
				chembl.MossSaveFormat(data.m, data.matrix2);

		} catch (BioclipseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}

}
