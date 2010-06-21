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

import java.util.ArrayList;
import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.rdf.model.IStringMatrix;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ChemblWizardPage2 extends WizardPage {

	private IChEMBLManager chembl;
	private Text textfield;
	private Label label;
	private GridData gridData;

	protected ChemblWizardPage2(String pageName) {
		super(pageName);
		chembl = Activator.getDefault().getJavaChEMBLManager();
	}

	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp();        
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);
		setControl(container);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "net.bioclipse.chembl.ui.helpmessage");
		
		label = new Label(container, SWT.NONE);
		label.setText("Add directory for file");
		gridData = new GridData();
		gridData.horizontalSpan=2;
		label.setLayoutData(gridData);
		
		textfield = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		textfield.setText("/Virtual/MySave");
		gridData.horizontalSpan = 2;
		textfield.setLayoutData(gridData);
		textfield.addModifyListener(new ModifyListener(){
			public void modifyText(ModifyEvent e) {
				Text txt = (Text)e.getSource();	
				((ChemblWizard) getWizard()).data.file = txt.getText();
			}});
	}
}//end


