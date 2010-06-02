package net.bioclipse.chembl.moss.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.rdf.model.IStringMatrix;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ChemblMossWizardPage2 extends WizardPage {

	private IChEMBLManager chembl;
	private Label label, title, type, target, info;
	private GridData gridData;
	private Combo cbox, cboxAct;
	private Table table;
	private TableColumn column1;
	private Spinner spinn;
	private Button button;

	public ChemblMossWizardPage2(String pagename){
		super(pagename);
		chembl = Activator.getDefault().getJavaChEMBLManager();
	}	
	
	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(2, false);
		layout.marginRight = 2;
	    layout.marginLeft = 2;
	    layout.marginBottom = -8;
	    layout.marginTop = 10;
	    layout.marginWidth = 2;
	    layout.marginHeight = 2;
	    layout.verticalSpacing = 5;
	    layout.horizontalSpacing = 5;
		container.setLayout(layout);
		setControl(container);
		setMessage("This is an application for MoSS. Compounds are collected from chEMBL by simply \nchosing a Kinase" +
		" protein family. For further information go to help. ");
		


		label = new Label(container, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText("Choose Kinase Protein Familes");

		cbox = new Combo(container,SWT.READ_ONLY);
		cbox.setToolTipText("Kinase family");
		gridData = new GridData(GridData.BEGINNING);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.widthHint=100;
		cbox.setLayoutData(gridData);
		String[] items = { "TK","TKL","STE","CK1","CMGC","AGC","CAMK" };
		cbox.setItems(items);
		cbox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				String selected = cbox.getItem(cbox.getSelectionIndex());
				try {
					table.clearAll();
					table.removeAll();
					setErrorMessage(null);
	
					List<String> list = chembl.MossAvailableActivities(selected);
					if(list.size()>0){
						String[] item = new String[list.size()];
						for(int i=0;i<list.size(); i++){
							item[i]= list.get(i);
						}		
					
						if(cboxAct.isEnabled()){
							if(cboxAct.getSelection().x == cboxAct.getSelection().y){
								cboxAct.setItems(item);
							}else{
								/*EMERGENCY SOLUTION.. To solve the problem
							that involves changing the protein family...
								 */

								//Brings the current activities to an array
								String oldItems[] = cboxAct.getItems();
								// Takes that array and makes it a list
								for(int i = 0; i< list.size(); i++){
									cboxAct.add(item[i]);	
								}
								//Remove the old items in the combobox
								int oldlistsize = cboxAct.getItemCount() - list.size();
								String index = cboxAct.getText();//cboxAct.getItem(cboxAct.getSelectionIndex());
								cboxAct.remove(0, oldlistsize-1);

								//Adds new items to the comboboxlist
								List<String> oldItemsList = new ArrayList<String>();
								for(int i = 0; i< oldItems.length;i++){
									oldItemsList.add(oldItems[i]);
								}
								
								//New query with the given settings
								//if(oldItemsList.contains((index))==true){
								if(list.contains((index))==true){
									IStringMatrix matrix, matrix2;
									try {
										spinn.setSelection(50);
										matrix = chembl.MossProtFamilyCompounds(selected, index, spinn.getSelection());
										matrix2 = chembl.MossProtFamilyCompounds(selected,index);
										info.setText("Total compund hit: "+ matrix2.getRowCount());
										cboxAct.setText(index);
										addToTable(matrix);
									} catch (BioclipseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}else{
									setErrorMessage("The activity " + index +" does not exist for the protein family " + selected + ".");
									info.setText("Total compund hit:");
									setPageComplete(false);
									button.setVisible(false);
								}
							} 
						}else{
							cboxAct.setItems(item);
							cboxAct.setEnabled(true);
						}	
					}

				}catch (BioclipseException e1) {
					e1.printStackTrace();
				}
				
			}
		});


		/*Returns the available compunds for the family*/
		label = new Label(container, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText("Choose one available activity");

		cboxAct = new Combo(container,SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.widthHint=100;
		String[] item = { "No available activity" };
		cboxAct.setItems(item);
		cboxAct.setLayoutData(gridData);
		cboxAct.setEnabled(false);
		cboxAct.setToolTipText("These activities are only accurate for chosen protein");
		//Listener for available activities(IC50, Ki etc)
		cboxAct.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				String selected = cboxAct.getItem(cboxAct.getSelectionIndex());
				try{
					setErrorMessage(null);
					table.clearAll();
					table.removeAll();
					spinn.setSelection(50);
					IStringMatrix matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()), selected,50);
					//IStringMatrix matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()), selected, spinn.getSelection());
					addToTable(matrix);
					button.setEnabled(true);
					spinn.setEnabled(true);
					cboxAct.setEnabled(true);
					/*Count the amount of compounds there is for one hit,
					 * i.e. same query without limit.
					 * */
					IStringMatrix matrix2;
					try {
						matrix2 = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()),cboxAct.getItem(cboxAct.getSelectionIndex()));
						info.setText("Total compund hit: "+ matrix2.getRowCount());
					} catch (BioclipseException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}catch(BioclipseException e1){
					e1.printStackTrace();
				}
				setPageComplete(true);
			} 
		});

		/*Limits the search
		 * The users are able to limit there search or to be saved data.*/

		label = new Label(container, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		label.setLayoutData(gridData);
		label.setText("Limit");

		spinn = new Spinner(container, SWT.BORDER);
		gridData = new GridData();
		spinn.setLayoutData(gridData);
		spinn.setSelection(50);
		spinn.setMaximum(10000000);
		spinn.setIncrement(50);
		spinn.setEnabled(false);
		gridData.widthHint=100;
		gridData.horizontalSpan = 1;
		spinn.setToolTipText("Limits the search, increases by 50");
		spinn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int selected = spinn.getSelection();
				try{
					table.clearAll();
					table.removeAll();
					IStringMatrix matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()),cboxAct.getItem(cboxAct.getSelectionIndex()), selected);
					table.setVisible(true);
					addToTable(matrix);
				}catch(BioclipseException e1){
					e1.printStackTrace();
				}
			}});
		info = new Label(container, SWT.NONE);
		gridData = new GridData();
		info.setLayoutData(gridData);
		gridData.horizontalSpan = 2;
		gridData.widthHint = 400;
		info.setText("Total compound hit:" );

		table = new Table(container, SWT.BORDER );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		//		table.setVisible(false);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint=300;
		gridData.heightHint=300;
		gridData.horizontalSpan = 2;
		table.setLayoutData(gridData);
		column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Compounds (SMILES)"); 

		//Button that adds all hits to the limit
		button = new Button(container, SWT.PUSH);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		button.setLayoutData(gridData);
		button.setText("Add all");
		button.setEnabled(false);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				IStringMatrix matrix;
				try {
					table.removeAll();
					matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()),cboxAct.getItem(cboxAct.getSelectionIndex()));
					spinn.setSelection(matrix.getRowCount());
					addToTable(matrix);
				} catch (BioclipseException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

	}//end container

	// General method for adding items(i.e. compounds) to the table
	public void addToTable(IStringMatrix matrix){
		for(int r = 1; r < matrix.getRowCount()+1; r++){	
			TableItem item= new TableItem(table, SWT.NONE);
			item.setText(0,r+" "+ matrix.get(r, matrix.getColumnName(1)));
			column1.pack();
		}
		//If there are more than one column this should be used since it will add to more then one 
		// column in the table
		//		for(int r = 1; r < matrix.getRowCount()+1; r++){	
		//			TableItem item = new TableItem(table, SWT.NULL);
		//			for(int i = 0; i < matrix.getColumnCount(); i++){	
		//				item.setText(i, matrix.get(r, matrix.getColumnName(i+1)));
		//			}

		((ChemblMossWizard) getWizard()).data.matrix = matrix; 
	}
	
	}
	

