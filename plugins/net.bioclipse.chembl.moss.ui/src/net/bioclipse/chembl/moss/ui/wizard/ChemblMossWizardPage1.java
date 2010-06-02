package net.bioclipse.chembl.moss.ui.wizard;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.rdf.model.IStringMatrix;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RefineryUtilities;
public class ChemblMossWizardPage1 extends WizardPage implements IRunnableContext{

	private IChEMBLManager chembl;
	private Label label, info, labelLow, labelHigh;
	private GridData gridData;
	private Combo cbox, cboxAct;
	private Table table;
	private TableColumn column1, column2, column3;
	private Spinner spinn, spinnLow, spinnHigh;
	private Button button, buttonb, check, buttonH, buttonUpdate;
	private Text text;
	XYSeries series;
	public static final String PAGE_NAME = "one";
	IStringMatrix matrixAct;
	private String index;
	public ChemblMossWizardPage1(String pagename){
		super(pagename);
		chembl = Activator.getDefault().getJavaChEMBLManager();

	}	
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp();		
	}

	@Override
	public void createControl(Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(4, false);
		layout.marginRight = 2;
		layout.marginLeft = 2;
		layout.marginBottom = -2;
		layout.marginTop = 10;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 5;
		container.setLayout(layout);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(container, "net.bioclipse.moss.business.helpmessage");
		setControl(container);
		setMessage("This is an application for MoSS. Compounds are collected from chEMBL by simply \nchosing a Kinase" +
		" protein family. For further information go to help. ");
		setPageComplete(false);


		label = new Label(container, SWT.NONE);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Choose Kinase Protein Familes");

		cbox = new Combo(container,SWT.READ_ONLY);
		cbox.setToolTipText("Kinase family");
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
		gridData.widthHint=100;
		cbox.setLayoutData(gridData);
		String[] items = { "TK","TKL","STE","CK1","CMGC","AGC","CAMK" };
		cbox.setItems(items);
		cbox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				final String selected = cbox.getItem(cbox.getSelectionIndex());

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
								index = cboxAct.getText();//cboxAct.getItem(cboxAct.getSelectionIndex());
								cboxAct.remove(0, oldlistsize-1);
								//Adds new items to the comboboxlist
								List<String> oldItemsList = new ArrayList<String>();
								for(int i = 0; i< oldItems.length;i++){
									oldItemsList.add(oldItems[i]);
								}

								//New query with the given settings
								//if(oldItemsList.contains((index))==true){
								if(list.contains((index))==true){

									spinn.setSelection(50);
									IStringMatrix matrix, matrix2;
									try {
										matrix = chembl.MossProtFamilyCompoundsAct(selected, index, spinn.getSelection());
										matrix2 = chembl.MossProtFamilyCompounds(selected,index);
										cboxAct.setText(index);
										info.setText("Distinct compunds: "+ matrix2.getRowCount());
										addToTable(matrix);
									} catch (BioclipseException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
							
							}else{
								setErrorMessage("The activity " + index +" does not exist for the protein family " + selected + ".");
								info.setText("Total compund hit:");
								setPageComplete(false);
								
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
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Choose one available activity");

		cboxAct = new Combo(container,SWT.READ_ONLY);
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 2;
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
					check.setSelection(false);
					spinnLow.setEnabled(false);
					spinnHigh.setEnabled(false);
					spinnLow.setSelection(0);
					spinnHigh.setSelection(1000);
					
					//IStringMatrix matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()), selected,50);
					//IStringMatrix matrix = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()), selected, spinn.getSelection());

					IStringMatrix matrix = chembl.MossProtFamilyCompoundsAct(cbox.getItem(cbox.getSelectionIndex()), selected,spinn.getSelection());
					addToTable(matrix);
					//Count the amount of compounds there is for one hit, i.e. same query without limit.
					IStringMatrix matrix2 = chembl.MossProtFamilyCompounds(cbox.getItem(cbox.getSelectionIndex()),cboxAct.getItem(cboxAct.getSelectionIndex()));
					info.setText("Distinct compounds: "+ matrix2.getRowCount());

					//Query for activities. Adds them to the plot series.
					matrixAct = chembl.MossProtFamilyCompoundsAct(cbox.getItem(cbox.getSelectionIndex()), selected);

					//Adds activity to histogram series
					series = new XYSeries("Activity for compounds");
					for(int i = 1; i< matrixAct.getRowCount()+1;i++){
						if( matrixAct.get(i,"actval").equals(""))series.add(0,0);	
						else series.add( Double.parseDouble(matrixAct.get(i,"actval")), Double.parseDouble(matrixAct.get(i,"actval")));
					}

					button.setEnabled(true);
					spinn.setEnabled(true);
					check.setEnabled(true);
					//cboxAct.setEnabled(true);
					buttonH.setEnabled(true);

				}catch(BioclipseException e1){
					e1.printStackTrace();
				}
				setPageComplete(true);
			} 
		});

		label = new Label(container, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan =2;
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

		//Button that adds all hits to the limit
		button = new Button(container, SWT.PUSH);
		button.setToolTipText("Add all compounds to the table");
		button.setText("Add all");
		button.setEnabled(false);
		button.setLayoutData(gridData);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//try {
				table.removeAll();
//				ProgressMonitorDialog dialog = new ProgressMonitorDialog(container.getShell());
//				
//				try {
//					dialog.run(true, true, new IRunnableWithProgress(){
//						public void run(IProgressMonitor monitor) {
//							monitor.beginTask("Searching for compounds", IProgressMonitor.UNKNOWN);
							try {
								IStringMatrix matrix = chembl.MossProtFamilyCompoundsAct(cbox.getItem(cbox.getSelectionIndex()), cboxAct.getItem(cboxAct.getSelectionIndex()));
								
//								final IStringMatrix matrix = chembl.MossProtFamilyCompoundsAct("TK", "Ki");
								addToTable(matrix);
								info.setText("Total hit(not always distinct compounds): " + matrix.getRowCount());
								spinn.setSelection(matrix.getRowCount());
								
							} catch (BioclipseException eb) {
								// TODO Auto-generated catch block
								eb.printStackTrace();
							}
							
//							
//							monitor.done();
//						}
//					});
//				} catch (InvocationTargetException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
				

				//				} catch (BioclipseException e1) {
				//					// TODO Auto-generated catch block
				//					e1.printStackTrace();
				//				}
			}
		});	
		check = new Button(container, SWT.CHECK);
		check.setText("Cut-off");
		check.setToolTipText("Modify data by specifying upper and lower activity limit");
		check.setEnabled(false);
		gridData = new GridData(GridData.BEGINNING);
		gridData.horizontalSpan = 1;
		check.setLayoutData(gridData);
		check.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				boolean selected = check.getSelection();
				if(selected == true){
					spinnLow.setEnabled(true);
					spinnHigh.setEnabled(true);
					buttonUpdate.setEnabled(true);
					labelHigh.setEnabled(true);
					labelLow.setEnabled(true);
				}
				else if(selected == false){
					spinnLow.setEnabled(false);
					spinnHigh.setEnabled(false);
					buttonUpdate.setEnabled(false);	
					labelHigh.setEnabled(false);
					labelLow.setEnabled(false);
				}
			}
		});
		buttonUpdate = new Button(container, SWT.PUSH);
		buttonUpdate.setText("Update table");
		buttonUpdate.setToolTipText("Update the table with the specified activity limits");
		buttonUpdate.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		buttonUpdate.setLayoutData(gridData);
		buttonUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				table.clearAll();
				table.removeAll();
				int cnt=0;

				for(int i = 1; i< matrixAct.getRowCount()+1;i++){

					if(matrixAct.get(i,"actval").contains("e")){

						if(spinnHigh.getSelection() >= 1000000){
							TableItem item= new TableItem(table, SWT.NONE);
							item.setText(0,String.valueOf(cnt+1));
							item.setText(2,matrixAct.get(i, "smiles"));		
							item.setText(1,matrixAct.get(i,"actval"));
							column1.pack();
							column2.pack();
							column3.pack();
							cnt++;
						}
					}
					else if( matrixAct.get(i,"actval").equals("")){

						if(spinnHigh.getSelection() == 0){
							TableItem item= new TableItem(table, SWT.NONE);
							item.setText(0,String.valueOf(cnt+1));
							item.setText(2,matrixAct.get(i, "smiles"));		
							item.setText(1,matrixAct.get(i,"actval"));
							column1.pack();
							column2.pack();
							column3.pack();
							cnt++;
						}
					}
					else if(Double.parseDouble(matrixAct.get(i,"actval")) >= spinnLow.getSelection() && 
							Double.parseDouble(matrixAct.get(i,"actval")) <= spinnHigh.getSelection()){
						//if(Double.parseDouble(matrixAct.get(i,"actval")) >= 10000 && Double.parseDouble(matrixAct.get(i,"actval")) <= 1000000){

						TableItem item= new TableItem(table, SWT.NONE);
						item.setText(0,String.valueOf(cnt+1));
						item.setText(2,matrixAct.get(i, "smiles"));		
						item.setText(1,matrixAct.get(i,"actval"));
						column1.pack();
						column2.pack();
						column3.pack();
						cnt++;
					}	
					spinn.setSelection(cnt);


					//					if(spinnHigh.getSelection() >= 1000000 && matrixAct.get(i,"actval").contains("e") ){
					//						System.out.print("hej1");
					//						TableItem item= new TableItem(table, SWT.NONE);
					//						item.setText(0,cnt+1 +" "+matrixAct.get(i, "smiles"));		
					//						item.setText(1,matrixAct.get(i,"actval"));
					//						column1.pack();
					//						cnt++;
					//					}
					//					 if(matrixAct.get(i,"actval").contains("e")){
					//						System.out.print(Double.parseDouble(matrixAct.get(i, "actval") + " hej2"));
					//						}
					//					else if(Double.parseDouble(matrixAct.get(i,"actval")) >= spinnLow.getSelection() && 
					//							Double.parseDouble(matrixAct.get(i,"actval")) <= spinnHigh.getSelection()){
					//						//if(Double.parseDouble(matrixAct.get(i,"actval")) >= 10000 && Double.parseDouble(matrixAct.get(i,"actval")) <= 1000000){
					//						System.out.print("hej3");
					//						TableItem item= new TableItem(table, SWT.NONE);
					//						item.setText(0,cnt+1 +" "+matrixAct.get(i, "smiles"));		
					//						item.setText(1,matrixAct.get(i,"actval"));
					//						column1.pack();
					//						cnt++;
					//					}	



					info.setText("Total compound hit: "+ cnt);
				}
			}
		});
		/*Limits the search
		 * 
		 * The users are able to limit there search or to be saved data.*/
		buttonH = new Button(container, SWT.PUSH);
		buttonH.setText("Histogram");
		buttonH.setToolTipText("Shows activity in a histogram(for all compounds)");
		buttonH.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalSpan = 2; 
		buttonH.setLayoutData(gridData);
		buttonH.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {		    	
				final XYSeriesCollection dataset = new XYSeriesCollection(series);
				JFreeChart chart = ChartFactory.createXYBarChart(
						"Activity chart",
						"Compounds", 
						false,
						"Activity value", 
						dataset,
						PlotOrientation.VERTICAL,
						true,
						true,
						false
				);
				ChartFrame frame = new ChartFrame("Activities", chart); 
				frame.pack(); 
				frame.setVisible(true);
			}
		});	
		
		labelLow = new Label(container, SWT.NONE);
		labelLow.setText("Lower activity limit");
		labelLow.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		labelLow.setLayoutData(gridData);

		spinnLow = new Spinner(container,SWT.NONE);
		spinnLow.setSelection(0);
		spinnLow.setMaximum(10000000);
		spinnLow.setIncrement(50);
		spinnLow.setEnabled(false);
		spinnLow.setToolTipText("Specify lower activity limit");
		gridData = new GridData();
		gridData.widthHint=100;
		gridData.horizontalSpan = 1;
		spinnLow.setLayoutData(gridData);
		spinnLow.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//				int selected = spinnLow.getSelection();

			}});
		labelHigh = new Label(container, SWT.NONE);
		labelHigh.setText("Upper activity limit");
		labelHigh.setEnabled(false);
		gridData = new GridData();
		gridData.horizontalSpan = 1;
		labelHigh.setLayoutData(gridData);

		spinnHigh = new Spinner(container,SWT.BORDER);
		spinnHigh.setSelection(1000);
		spinnHigh.setMaximum(1000000000);
		spinnHigh.setIncrement(50);
		spinnHigh.setEnabled(false);
		spinnHigh.setToolTipText("Specify upper activity limit");
		gridData = new GridData();
		gridData.widthHint=100;
		gridData.horizontalSpan = 1;
		spinnHigh.setLayoutData(gridData);
		spinnHigh.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//				int selected = spinnHigh.getSelection();

			}});


		info = new Label(container, SWT.NONE);
		gridData = new GridData();
		info.setLayoutData(gridData);
		gridData.horizontalSpan = 4;
		gridData.widthHint = 350;
		info.setText("Total compound hit:" );

		table = new Table(container, SWT.BORDER );
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.widthHint=300;
		gridData.heightHint=300;
		gridData.horizontalSpan = 4;
		table.setLayoutData(gridData);
		column1 = new TableColumn(table, SWT.NONE);
		column1.setText("Index");
		column2 = new TableColumn(table, SWT.NONE);
		column2.setText("Activity value"); 
		column3= new TableColumn(table, SWT.NONE);
		column3.setText("Compounds (SMILES)"); 

		label = new Label(container, SWT.NONE);
		label.setText("File directory: ");
		gridData = new GridData(GridData.FILL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 4;
		label.setLayoutData(gridData);

		text = new Text(container, SWT.BORDER|SWT.FILL);
		text.setText("MossFile");
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		text.setLayoutData(gridData);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (e.getSource() instanceof Text) {
					Text txt = (Text) e.getSource();
					((ChemblMossWizard) getWizard()).data.m  = txt.getText();
				}
			}
		});

		buttonb = new Button(container, SWT.NONE);
		buttonb.setText("Browse");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		buttonb.setLayoutData(gridData);
}//end container

// General method for adding items(i.e. compounds) to the table
public void addToTable(IStringMatrix matrix){
	
    //Adds a matrix to a table
	for(int r = 1; r < matrix.getRowCount()+1; r++){	
		TableItem item = new TableItem(table, SWT.NULL);
		item.setText(0,String.valueOf(r));
		for(int i = 1; i < matrix.getColumnCount()+1; i++){	
			item.setText(i, matrix.get(r, matrix.getColumnName(i)));
		}
	}
	column1.pack();
	column2.pack();
	column3.pack();
	
	//Save matrix into a datamodel
	((ChemblMossWizard) getWizard()).data.matrix = matrix; 
}
@Override
public void run(boolean fork, boolean cancelable,
		IRunnableWithProgress runnable) throws InvocationTargetException,
		InterruptedException {
	// TODO Auto-generated method stub

}

}

