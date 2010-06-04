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
package net.bioclipse.chembl.ui.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class ChemblWizardPageTabs extends WizardPage {

	protected ChemblWizardPageTabs(String pageName) {
		super(pageName);
		
	}

	@Override
	public void createControl(Composite container) {
		// TODO Auto-generated method stub
	
	/**
	 * Creates tabs
	 * */
		final TabFolder tabFolder = new TabFolder (container, SWT.FILL);
			String[] tabs= {"Compound", "Target", "Special, About"}; 
			for (int i = 0; i < 3; i++) {
				TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
				tabItem.setText(tabs[i]);
			}
			// Container sizes set size to tabFolder. And Layoutdata makes tabFolder dynamic 
			container.setSize(1000, 1000);
			tabFolder.setSize(container.getSize().x,container.getSize().y);
			tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
			tabFolder.setBackgroundMode(container.getBackgroundMode());

	}
}
