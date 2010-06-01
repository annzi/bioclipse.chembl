package net.bioclipse.chembl.ui.wizard;


import net.bioclipse.chembl.Activator;
import net.bioclipse.chembl.business.IChEMBLManager;
import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ChemblWizard extends Wizard implements INewWizard {

    private static final Logger logger = Logger.getLogger(ChemblWizard.class);
    private ChemblWizardPage firstpage;
    private IChEMBLManager chembl;
    //ChemblData data = new ChemblData();
 
    public ChemblWizard() {
        super();
        setWindowTitle("ChEMBL wizard");
        chembl = Activator.getDefault().getJavaChEMBLManager();
    }

    public void addPages(){
        firstpage = new ChemblWizardPage("ChEMBL page");
        addPage(firstpage);
    }    

    //Writes compounds to a MoSS supported file
    @Override
    public boolean performFinish() {
    
        return false;
    }
    @ Override
    public boolean isHelpAvailable() {
        return true;
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub
		
	}
}

