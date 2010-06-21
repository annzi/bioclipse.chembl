package net.bioclipse.chembl.moss.ui.wizard;

import java.lang.reflect.InvocationTargetException;

import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import net.bioclipse.core.ResourcePathTransformer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class ChemblMossWizardPage3 extends WizardPage implements IRunnableContext{
	private Button buttonb;
	private IChEMBLManager chembl;
	private GridData gridData;
	private Label label;
	private Text text;
	
	public ChemblMossWizardPage3(String pagename){
		super(pagename);
		chembl = Activator.getDefault().getJavaChEMBLManager();

	}	
	public void performHelp() {
		PlatformUI.getWorkbench().getHelpSystem().displayHelp();		
	}
	@Override
	public void run(boolean fork, boolean cancelable,
			IRunnableWithProgress runnable) throws InvocationTargetException,
			InterruptedException {
		// TODO Auto-generated method stub
		
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
		buttonb.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent e) {

	            	

	            	DirectoryDialog dialId = new DirectoryDialog(getShell());
	                dialId.setFilterPath(Platform.getLocation().toOSString());
	                IWorkspace workspace = ResourcesPlugin.getWorkspace();
	              //  dialId.setText("MoSS file directory");
	                String dirId = dialId.open();
	                if (dirId != null) {
	                    text.setText(dirId+"/chemblmossOutput");
	                   
//	                  IFile f =ResourcePathTransformer.getInstance()
//                      .transform(dirId);
	                    ((ChemblMossWizard) getWizard()).data.m= dirId;
	  	              
	                } else {
	                    return;
	                }
	            }
	        });
	    }
	}
