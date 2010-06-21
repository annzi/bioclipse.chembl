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
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ChemblWizardPage extends WizardPage implements Listener {

	private IChEMBLManager chembl;
	private Label title, score, type, label, target, key, history;
	private GridData gridData, gridData2;
	private Table table, table2;
	private TableViewer tableViewer;
	private Button buttonGo,save, targetprot, compounds,check1,check2,check3,check4;
	private Button selectAll,delete;
	private Font font;
	private TableItem item;
	private TableColumn[] columns, columns2;
	private Text textfield;

	protected ChemblWizardPage(String pageName) {
		super(pageName);
		chembl = Activator.getDefault().getJavaChEMBLManager();
	}

	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp();        
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(5, false);
		container.setLayout(layout);
		setControl(container);

		setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
		" Need help? Push the help button for further information. ");
		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "net.bioclipse.chembl.ui.helpmessage");
		setPageComplete(false);

		label = new Label(container, SWT.NONE);
		label.setText("Search");
		font = new Font(container.getDisplay(), "Helvetica", 15, SWT.NONE);
		label.setFont(font);
		gridData = new GridData();
		gridData.horizontalSpan=5;
		label.setLayoutData(gridData);

		compounds = new Button(container, SWT.CHECK);
		compounds.setText("Compound search");
		compounds.setSelection(true);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		compounds.setLayoutData(gridData);

		targetprot = new Button(container, SWT.CHECK);
		targetprot.setText("Target search");
		targetprot.setSelection(false);
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		targetprot.setLayoutData(gridData);

		//gridData for the upcoming checkboxes
		gridData = new GridData();
		gridData.horizontalSpan = 1;

		check1 = new Button(container, SWT.RADIO);
		check1.setText("chebiId             ");
		check1.setSelection(true);
		check1.setVisible(true);
		check1.setLayoutData(gridData);

		check3 = new Button(container, SWT.RADIO);
		check3.setText("Keyword          ");
		check3.setSelection(false);
		check3.setVisible(true);
		check3.setLayoutData(gridData);

		check2 = new Button(container, SWT.RADIO);
		check2.setText("SMILES        ");
		check2.setSelection(false);
		check2.setVisible(true);
		check2.setLayoutData(gridData);

		check4 = new Button(container, SWT.RADIO);
		check4.setText("Fasta sequence          ");
		check4.setSelection(false);
		check4.setVisible(false);
		check4.setLayoutData(gridData);

		//Adding field to search in
		textfield = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;
		textfield.setLayoutData(gridData);

		buttonGo = new Button(container, SWT.PUSH);
		buttonGo.setText("Search");
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		buttonGo.setLayoutData(gridData);

		history = new Label(container, SWT.NONE);
		history.setText("Last search:  ");
		font = new Font(container.getDisplay(), "Helvetica", 15, SWT.NONE);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 4;
		history.setFont(font);
		history.setLayoutData(gridData);		

		//General griddata for upcoming labels
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData2 = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		gridData.grabExcessHorizontalSpace = true;

		//Labels filled when something is clicked in the table
		title = new Label(container, SWT.BORDER);
		title.setText("title: ");
		title.setLayoutData(gridData);

		type = new Label(container, SWT.NONE);
		type.setText("type: ");
		type.setLayoutData(gridData2);

		score = new Label(container, SWT.BORDER);
		score.setText("score: ");
		score.setLayoutData(gridData);
		
		target = new Label(container, SWT.BORDER);
		target.setText("target: ");
		target.setLayoutData(gridData2);

		key = new Label(container, SWT.BORDER);
		key.setText("key:      ");
		key.setLayoutData(gridData);

		label = new Label(container, SWT.NONE);
		font = new Font(container.getDisplay(), "Helvetica", 13, SWT.NONE);
		label.setFont(font);
		label.setText("Result table: ");
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		label.setLayoutData(gridData);

		table = new Table(container, SWT.BORDER );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		gridData = new GridData(gridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 100;
		gridData.heightHint = 350;
		gridData.horizontalSpan = 4;
		table.setLayoutData(gridData);

		createTableColumn(table,5,0);

		table.addMouseListener(new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
			}
			@Override
			public void mouseDown(MouseEvent e) {
				Point p = new Point(e.x, e.y);
				TableItem item = table.getItem(p);
				title.setText(columns[0].getText()+ ": " + item.getText(0));
				type.setText(columns[1].getText()+ ": " + item.getText(1));
				score.setText(columns[2].getText()+ ": " + item.getText(2));
				target.setText(columns[3].getText()+ ": " + item.getText(3));
				key.setText(columns[4].getText()+ ": " + item.getText(4));
			}

			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
			}
		});

		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		DragSource source = new DragSource(table, DND.DROP_MOVE | DND.DROP_COPY);
		source.setTransfer(types);
		source.addDragListener(new DragSourceAdapter() {
			public void dragSetData(DragSourceEvent event) {
				// Get the selected items in the drag source
				DragSource ds = (DragSource) event.widget;
				Table table = (Table) ds.getControl();
				TableItem[] selection = table.getSelection();

				StringBuffer buff = new StringBuffer();
				for (int i = 0, n = selection.length; i < n; i++) {
					buff.append(selection[i].getText(0)+"\t");
					buff.append(selection[i].getText(1)+"\t");
					buff.append(selection[i].getText(2)+"\t");
					buff.append(selection[i].getText(3)+"\t");
					buff.append(selection[i].getText(4)+"\t");
					buff.append(selection[i].getText(5)+"\t");
					buff.append(selection[i].getText(6)+"\t");
				}
				event.data = buff.toString();
			}
		});

		save = new Button(container, SWT.PUSH);
		save.setText("save all");
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		save.setLayoutData(gridData);

		label = new Label(container, SWT.NONE);
		label.setFont(font);
		label.setText("Data stored for saving: ");
		gridData = new GridData();
		gridData.horizontalSpan = 5;
		label.setLayoutData(gridData);

		table2 = new Table(container, SWT.CHECK |SWT.BORDER);
		table2.setHeaderVisible(true);
		table2.setLinesVisible(true);
		table2.setLayoutData(gridData);

		 tableViewer = new CheckboxTableViewer(table2);

		
		gridData = new GridData(gridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint = 100;
		gridData.heightHint = 300;
		gridData.horizontalSpan = 4;
		table2.setLayoutData(gridData);
		createTableColumn(table2,5,1);

		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(table2, operations);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] type = new Transfer[] {textTransfer};
		target.setTransfer(type);
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}
			@Override
			public void dragLeave(DropTargetEvent event) {
				// TODO Auto-generated method stub
			}
			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
				if (textTransfer.isSupportedType(event.currentDataType)) {
					// NOTE: on unsupported platforms this will return null
					Object o = textTransfer.nativeToJava(event.currentDataType);
					//String t = (String)o;

				}}
			@Override
			public void drop(DropTargetEvent event) {
				if (textTransfer.isSupportedType(event.currentDataType)) {
					String text = (String)event.data; 
					TableItem item = new TableItem(table2, SWT.NONE);

					//					columns2[0].setText("delete");
					String[]split = text.split("\t");
					int length = split.length;
					for(int i = 0; i<length ; i++){
						item.setText(i,split[i]);

					}
					((ChemblWizard) getWizard()).data.tab = table2;
					//					item.setText(0,split[0]);
					//					item.setText(1,split[1]);
					//					item.setText(2,split[2]);
					//					item.setText(3,split[3]);
					//					item.setText(4,split[4]);
					//					item.setText(5,split[5]);
					//					item.setText(6,split[6]);

					packColumns(columns2);
					setPageComplete(true);
				}

			}

			@Override
			public void dropAccept(DropTargetEvent event) {
				// TODO Auto-generated method stub
			}
		});

		delete = new Button(container, SWT.PUSH);
		delete.setText("Delete");
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		delete.setLayoutData(gridData);

		selectAll = new Button(container, SWT.CHECK);
		selectAll.setText("Select all");
		selectAll.setLayoutData(gridData);
		
		addListeners();
	}


	/** UPDATE1
	 * Updates the wizard when something happens 
	 * */
	private void update(String text){
		IStringMatrix matrix = null;
		createEmptyColumnNames(columns);   
		try{
			if(check1.getSelection()){
				if(isInteger(text)){
					Integer cid = Integer.parseInt(text);
					matrix = chembl.getCompoundInfo(cid);
					if(matrix.getRowCount()<=0){
						setMessage("No hits for your search.");
					}else{
						setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
						" Need help? Push the help button for further information. ");
						addToTable(matrix,table,columns);
						columns[0].setText(matrix.getColumnName(1));
						columns[1].setText(matrix.getColumnName(2));
						columns[2].setText(matrix.getColumnName(3));
						columns[3].setText(matrix.getColumnName(4));
						columns[4].setText(matrix.getColumnName(5));
						columns[5].setText(matrix.getColumnName(6));
					}
				}else if(isInteger(text)==false){
					setMessage("No hits for your search. You searched with a string instead of a chebi number, perhaps " +
					"a keyword search is a more appropriate search.");
			}
			}
			else if(check2.getSelection()){
				matrix =chembl.getCompoundInfoWithSmiles(text);
				if(matrix.getRowCount()==0){
					setMessage("No hits for your search.");
				}else{
					setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
					" Need help? Push the help button for further information. ");
					columns[0].setText(matrix.getColumnName(1));
					addToTable(matrix,table, columns);
				}
			}
			else if(check3.getSelection()){
				matrix = chembl.getCompoundInfoWithKeyword(text);
				if(matrix.getRowCount()==0){
					setErrorMessage("No hits for your search.");
				}else{
					setMessage("Search is limited to 1000 hits.");
					columns[0].setText(matrix.getColumnName(1));
					columns[1].setText(matrix.getColumnName(2));
					columns[2].setText(matrix.getColumnName(3));
					columns[3].setText(matrix.getColumnName(4));
					addToTable(matrix,table, columns);
				}
			}
			else {
				target.setText("");
				key.setText("");
				title.setText("");
				type.setText("");
				score.setText("");
				setErrorMessage("There exist an error somewhere, please try again.");
				setPageComplete(false );
				getWizard().getContainer().updateButtons();
			}
			setErrorMessage(null);
		} catch (BioclipseException e) {
			setErrorMessage("Could not update information.");
		}

		setPageComplete( true );
		//		getWizard().getContainer().updateButtons();
	}

	/**UPDATE2
	 * Update table1 but for target 
	 */
	private void update2(String text){
		IStringMatrix matrix = null;
		createEmptyColumnNames(columns);
		try{
			if(check1.getSelection()){
				if(isInteger(text)){
					matrix = chembl.getProteinData(Integer.parseInt(text));
					if(matrix.getRowCount()==0){
						setMessage("No hits for your search.");
					}else{
						setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
						" Need help? Push the help button for further information. ");
						addToTable(matrix,table,columns);
						columns[0].setText(matrix.getColumnName(1));
						columns[1].setText(matrix.getColumnName(2));
						columns[2].setText(matrix.getColumnName(3));
						columns[3].setText(matrix.getColumnName(4));
						columns[4].setText(matrix.getColumnName(5));
						columns[5].setText(matrix.getColumnName(6));
					}
				}else if(isInteger(text)==false){
					setMessage("No hits for your search. You searched with a string instead of a chebi number, perhaps " +
					"a keyword search is a more appropriate search.");
			}
			}
			else if(check2.getSelection()){
				matrix =chembl.getTargetIDWithEC(text);
				if(matrix.getRowCount()==0){
					setMessage("No hits for your search.");
				}else{
					setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
					" Need help? Push the help button for further information. ");
					columns[0].setText(matrix.getColumnName(1));
					columns[1].setText(matrix.getColumnName(2));
					addToTable(matrix,table, columns);
				}
			}
			else if(check3.getSelection()){
				matrix  = chembl.getTargetIDWithKeyword(text);
				if(matrix.getRowCount()==0){
					setMessage("No hits for your search.");
				}else{
					setMessage("This is a search tool for chEMBL. Searches may be done from eiter a compound or a protein perspective.\n" +
					" Need help? Push the help button for further information. ");
					columns[0].setText(matrix.getColumnName(1));
					columns[1].setText(matrix.getColumnName(2));
					addToTable(matrix,table, columns);
				}
			}
			else {
				setErrorMessage("There exist an error somewhere, please try again.");
				setPageComplete(false );
				getWizard().getContainer().updateButtons();
			}
			setErrorMessage(null);
		} catch (BioclipseException e) {
			setErrorMessage("Could not update information.");
		}
		setPageComplete(true);
	}


	/**
	  * Help methods 
	  */
	private TableColumn[] createTableColumn(Table table, int size, int id){
		if(id == 0){
			columns = new TableColumn[size];
			for(int i =0; i< columns.length;i++){
				columns[i] = new TableColumn(table, SWT.NONE);
				columns[i].setWidth(100);
			}
		}else if(id == 1){
			columns2 = new TableColumn[size];
			for(int i =0; i< columns2.length;i++){
				columns2[i] = new TableColumn(table, SWT.NONE);
				columns2[i].setWidth(100);
			}
		}
		if(id ==0)return columns;
		else return columns2;
	}

	private void createEmptyColumnNames(TableColumn[] columns){
		for(int i = 0; i<columns.length; i++){
			columns[i].setText("");
		}
	}
	private void packColumns(TableColumn[] columns){
		for(int i =0; i<columns.length; i++){
			columns[i].pack();
		}
	}

	//Returns true if a String is an integer
	public boolean isInteger(String check){
		try{
			Integer.parseInt(check);
			return true;
		}catch(Exception e){
			return false;
		}
	}	
	public void addToTable(IStringMatrix matrix,Table table, TableColumn[] columns){
		for(int r = 1; r < matrix.getRowCount()+1; r++){    
			item = new TableItem(table, SWT.NULL);

			for(int i = 0; i < matrix.getColumnCount(); i++){  //+1?
				item.setText(i, matrix.get(r, matrix.getColumnName(i+1)));
			}
		}
		packColumns(columns);
	}
	public void showMessage(String id, String title, String message) {
		if(id.equals("error")){
			MessageDialog.openError(getShell(), title, message);
		}else
			MessageDialog.openQuestion(getShell(), title, message);
	}


	private void addListeners()
	{
		check1.addListener(SWT.Selection, this);
		check2.addListener(SWT.Selection, this);
		check3.addListener(SWT.Selection, this);
		check4.addListener(SWT.Selection, this);
		compounds.addListener(SWT.Selection, this);
		targetprot.addListener(SWT.Selection, this);
		buttonGo.addListener(SWT.Selection, this);
		save.addListener(SWT.Selection, this);
		delete.addListener(SWT.Selection, this);
		selectAll.addListener(SWT.Selection,this);

	}

	@Override
	public void handleEvent(Event event) {
		if((event.widget == compounds && compounds.getSelection())){
			targetprot.setSelection(false);
			check1.setText("Chebi");
			check2.setText("SMILES");
			check3.setText("Keyword");
		}else if((event.widget == targetprot && targetprot.getSelection())){
			compounds.setSelection(false);
			check1.setText("Target Id");
			check2.setText("EC number");
			check4.setVisible(true);
		}

		if(event.widget == buttonGo){
			table.clearAll();
			table.removeAll();
			history.setText("Last search: " +textfield.getText());
			title.setText("target: ");
			type.setText("type: ");
			score.setText("score: ");
			target.setText("target: ");
			key.setText("key: ");
			if(targetprot.getSelection() && compounds.getSelection() == false){
				update2(textfield.getText());
			}
			else if(compounds.getSelection() == true && targetprot.getSelection() == false){		
				update(textfield.getText());
			}
			else{
				showMessage("error","Selection Error","Both boxes can't be checked");
			}
		}

		if(event.widget == save){
			TableItem[] ti = table.getItems();			
			for(int i =0; i< ti.length;i++){
				TableItem t = new TableItem(table2, SWT.CHECK);
				for(int j = 0; j < table.getColumnCount(); j++){  
					t.setText(j, ti[i].getText(j));
				}
			}
			packColumns(columns2);
			setPageComplete(true);
			((ChemblWizard) getWizard()).data.tab = table2;	
			 selectAll.setSelection(false);
		}
		if(event.widget == delete){
			TableItem[] ti = table2.getItems();
			ArrayList<Integer> helper = new ArrayList<Integer>();
			for(int i=0; i<ti.length;i++){	
				if(ti[i].getChecked()){
					helper.add(table2.indexOf(ti[i]));	    
				}
			}
			int[] indices = new int [helper.size()];
			for(int j=0; j< helper.size();j++){
				indices[j] = helper.get(j);
			}
			table2.remove(indices);
			selectAll.setSelection(false);
		}
		if(event.widget == selectAll && selectAll.getSelection()){
			((CheckboxTableViewer) tableViewer).setAllChecked(true);
			}
		if(event.widget == selectAll && selectAll.getSelection()==false){
			((CheckboxTableViewer) tableViewer).setAllChecked(false);
			}
	}
	
}//end


