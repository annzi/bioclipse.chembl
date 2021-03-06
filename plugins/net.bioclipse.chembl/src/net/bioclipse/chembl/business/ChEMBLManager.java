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
package net.bioclipse.chembl.business;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import net.bioclipse.core.ResourcePathTransformer;
import net.bioclipse.core.business.BioclipseException;
import net.bioclipse.managers.business.IBioclipseManager;
import net.bioclipse.rdf.Activator;
import net.bioclipse.rdf.business.IJavaRDFManager;
import net.bioclipse.rdf.model.IStringMatrix;
import net.bioclipse.rdf.model.StringMatrix;

public class ChEMBLManager implements IBioclipseManager {

	private IJavaRDFManager rdf = Activator.getDefault().getJavaManager();

	private String CHEMBL_SPARQL_ENDPOINT =
		"http://rdf.farmbio.uu.se/chembl/sparql/";

	/**
	 * Gives a short one word name of the manager used as variable name when
	 * scripting.
	 */
	public String getManagerName() {
		return "chembl";
	}

	public IStringMatrix cutter(IStringMatrix matrix){

		String[] columnNames = new String[matrix.getColumnCount()];

		for(int j = 1; j<matrix.getColumnCount()+1;j++){
			columnNames[j-1] = matrix.getColumnName(j);
		}

		for(int k = 0; k< columnNames.length; k++){
			String submatrix = null;
			for(int i = 1; i<matrix.getRowCount()+1; i++){
				if(columnNames[k].equals("asstype"))
					submatrix = matrix.get(i,columnNames[k]).substring(8);
				else if (columnNames[k].equals("score"))	
					submatrix = matrix.get(i, columnNames[k]).substring(0,1);
				else if (columnNames[k].equals("act"))
					submatrix = matrix.get(i,columnNames[k]).substring(42);
				else if (columnNames[k].equals("target"))
					submatrix = matrix.get(i, columnNames[k]).substring(40);
				else if (columnNames[k].equals("pubmed"))
					submatrix = matrix.get(i, columnNames[k]).substring(26);
				else if (columnNames[k].equals("chebi"))
					submatrix = matrix.get(i, columnNames[k]).substring(25);
				else if (columnNames[k].equals("uniprot"))
					submatrix = matrix.get(i, columnNames[k]).substring(31);
				else if (columnNames[k].equals("ec"))
					submatrix = matrix.get(i, columnNames[k]).substring(26);
				else{
					submatrix = matrix.get(i, columnNames[k]);
				}

				if(submatrix != null)
					matrix.set(i, columnNames[k], submatrix);
			}
		}
		return matrix;
	}

	public List<String> getActivities(Integer targetID)
	throws BioclipseException {
		List<String> activities = new ArrayList<String>();
	
		String sparql =
			"PREFIX blueobelisk: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?act " +
			"WHERE { " +
			" ?activ chembl:type ?act; " +
			"    chembl:onAssay ?ass. " +
			" ?ass chembl:hasTarget " + 
			"    <http://rdf.farmbio.uu.se/chembl/target/t" + targetID + ">. " +
			"}";
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		if (matrix.getRowCount() > 0)
			activities.addAll(matrix.getColumn("act"));
	
		return activities;
	}

	public String getChebiId(Integer molID)
	throws BioclipseException{
		String sparql=
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
			"SELECT DISTINCT ?chebi WHERE{"+
			"<http://rdf.farmbio.uu.se/chembl/molecule/m"+ molID +"> owl:sameAs ?chebi."+
			"FILTER regex(?chebi ,\"chebi\") }";
	
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		String chebi = matrix.getColumn("chebi").get(0).substring(25);
		return chebi;
	}

	public List<String> getChebiId(String molIDs)
	throws BioclipseException{
	
		String[] molID = molIDs.split(",");
		List<String> chebis = new ArrayList<String>();
	
		for(int i=0; i < molID.length; i++){
			String sparql=
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
				"SELECT DISTINCT ?chebi WHERE{"+
				"<http://rdf.farmbio.uu.se/chembl/molecule/m"+ molID[i] +"> owl:sameAs ?chebi."+
				"FILTER regex(?chebi ,\"chebi\") }";
			IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
			String chebi = matrix.getColumn("chebi").get(0).substring(25);
			chebis.add(chebi);
		} 
		return chebis;
	}

	public IStringMatrix getCompoundInfo(Integer chebiID)
	throws BioclipseException, IOException{
	
		String sparql=
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+    
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
			"  SELECT DISTINCT ?act ?asstype ?type ?score ?target ?title WHERE{"+
			"    ?act chembl:forMolecule <http://rdf.farmbio.uu.se/chembl/molecule/m" + chebiID + "> ." +
			"    <http://rdf.farmbio.uu.se/chembl/molecule/m" + chebiID + "> bo:smiles ?SMILES." +
			"    ?act chembl:type ?type;" +
			"        chembl:onAssay ?ass." +
			"    ?ass chembl:hasAssayType ?asstype;" +
			"          chembl:hasTarget ?target;" +
			"          chembl:hasConfScore ?score." +
			"    ?target dc:title ?title." +
			"}";
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
	
		return matrix;
	}

	public List<IStringMatrix> getCompoundInfo(String chebiIDs)
	throws BioclipseException, IOException{
	
		List<IStringMatrix> matrices = new ArrayList<IStringMatrix>();
		String[] chebiID = chebiIDs.split(",");
		for(int i = 0; i<chebiID.length; i++){
			String sparql=
	
				"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+    
				"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
	
				"SELECT DISTINCT ?act ?asstype ?type ?score ?target ?title WHERE{"+
				"  ?act chembl:forMolecule <http://rdf.farmbio.uu.se/chembl/molecule/m" + chebiID[i] + "> ." +
				"  <http://rdf.farmbio.uu.se/chembl/molecule/m" + chebiID[i] + "> bo:smiles ?SMILES." +
				"  ?act chembl:type ?type;" +
				"      chembl:onAssay ?ass." +
				"  ?ass chembl:hasAssayType ?asstype;" +
				"        chembl:hasTarget ?target;" +
				"        chembl:hasConfScore ?score." +
				"  ?target dc:title ?title." +
				"}";
	
			IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
			cutter(matrix);
			matrices.add(matrix);
		}
		return matrices;
	}

	public IStringMatrix getCompoundInfoWithKeyword(String keyword)
	throws BioclipseException{
	
		String sparql=
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			
			"SELECT DISTINCT ?target ?description ?chebi ?smiles WHERE {" +
			"  ?target a chembl:Target;" +
			"          chembl:hasDescription ?description ." +
			"  ?assay chembl:hasTarget ?target." +
			"  ?act chembl:onAssay ?assay;" +
			"       chembl:forMolecule ?mol." +
			"  ?mol bo:smiles ?smiles;" +
			"       owl:sameAs ?chebi." +
			"  FILTER regex(?chebi, \"chebi\" )."+
			"  FILTER regex(?description , " + "\"" + keyword + "\" , \"i\") ." +
			"} LIMIT 1000";
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	public IStringMatrix getCompoundInfoWithSmiles(String smiles)
	throws BioclipseException{
		smiles = smiles.replaceAll("\\(","\\\\\\\\(");
		smiles =smiles.replaceAll("\\)","\\\\\\\\)");
		smiles =smiles.replaceAll("\\[","\\\\\\\\[");
		smiles =smiles.replaceAll("\\]","\\\\\\\\]");
		smiles =smiles.replaceAll("\\+","\\\\\\\\+");
		smiles =smiles.replaceAll("\\.","\\\\\\\\.");
		smiles =smiles.replaceAll("\\*","\\\\\\\\*");
		smiles =smiles.replaceAll("\\/","\\\\\\\\/");
		//		smiles =smiles.replaceAll("\\\\","\\\\\\\\\\");
	
		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			" SELECT ?chebi ?title WHERE {" +
			"    ?mol a chembl:Compound ." +
			"    ?mol bo:smiles ?smiles ."+
			"    ?mol owl:sameAs ?chebi ." +
			" FILTER regex(?smiles, \"^"+ smiles + "$\") ."+
			" FILTER regex(?chebi , \"chebi\") ." +
			" OPTIONAL {?mol dc:title ?title} ."+
			"}";
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	public IStringMatrix getPCM(String actType, String classL6, String classL3)
	throws BioclipseException{
	
		String sparql =
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#>"+
			" SELECT DISTINCT ?target ?pubmed ?smiles ?l3 ?l4 ?l5 ?l6 ?seq ?val WHERE{"+
			"  ?act chembl:type ?type;"+ 
			"    chembl:onAssay ?ass;"+
			"    chembl:forMolecule ?mol;"+
			"    chembl:standardValue ?val."+
			"  ?mol bo:smiles ?smiles."+
			"  ?target a <http://rdf.farmbio.uu.se/chembl/onto/#Target> ;" +
			"    chembl:classL3 ?l3;" +  //+ " \"" + classL3 + "\" ;" +
			"    chembl:classL4 ?l4;" +
			"    chembl:classL5 ?l5;" +
			"    chembl:classL6 ?l6;" +
			"    chembl:sequence ?seq." +
			"  FILTER regex(?l3, " + "\"" + classL3 + "\" , \"i\")." +
			"  ?ass chembl:hasTarget ?target;"+
			"    chembl:extractedFrom ?journal."+
			"  ?ass chembl:hasTargetCount 1 ."+
			"  ?journal <http://purl.org/ontology/bibo/pmid> ?pubmed." +
			"  FILTER regex(?l6, " + "\"^" + classL6 + "$\" , \"i\")." +
			"  FILTER regex(?type,  \"^" + actType + "$\", \"i\" )"+
			"}"
			;
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	// Returns properties like title of target, type of target and organism.
	public IStringMatrix getProperties(Integer targetID)
	throws BioclipseException {
		String sparql =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/> " +
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?title ?type ?organism " +
			"WHERE { " +
			"  <http://rdf.farmbio.uu.se/chembl/target/t" + targetID + "> " +
			"  dc:title ?title ;" +
			"  chembl:hasTargetType ?type ;" +
			" chembl:organism ?organism ." +
			"}";
	
		return rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
	}

	public IStringMatrix getProteinData(Integer targetID) throws BioclipseException{
		String sparql =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>" +

			"SELECT DISTINCT ?acttype ?chebi ?organism ?description ?l3 ?l4 ?l5 ?l6 ?uniprot ?ec WHERE { " +
			"<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">" +
			"		chembl:organism ?organism;" +
			"		chembl:hasDescription ?description." +
			"?activ chembl:type ?acttype;" + 
			"       chembl:onAssay ?ass; " +
			"		chembl:forMolecule ?mol."+
			"?mol owl:sameAs ?chebi. " +
			"?ass chembl:hasTarget  <http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">. " +
			"	OPTIONAL {<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +"> chembl:classL3 ?l3.}."+
			"	OPTIONAL{<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">chembl:classL4 ?l4.}."+
			"	OPTIONAL{<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">  chembl:classL5 ?l5.}."+
			"	OPTIONAL{<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">  chembl:classL6 ?l6.}." +
			"	OPTIONAL{<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">  owl:sameAs ?uniprot." +
			"		FILTER regex(?uniprot, \"uniprot\")}."+
			"	OPTIONAL{<http://rdf.farmbio.uu.se/chembl/target/t" +targetID +">  owl:sameAs ?ec." +
			"		FILTER regex(?ec, \"ec\")}."+
			"		FILTER regex (?chebi, \"chebi\")."+
			"}";

		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}
	public Map<String, Double> getQSARData(Integer targetID, String activity)
	throws BioclipseException {
		Map<String, Double> results = new HashMap<String, Double>();
	
		String sparql =
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?smiles ?val " +
			"WHERE { " +
			"  ?act chembl:type \"" + activity + "\"; " +
			"  chembl:onAssay ?ass; " +
			"  chembl:standardValue ?val; " +
			"  chembl:forMolecule ?mol. " +
			" ?mol bo:smiles ?smiles. " +
			" ?ass chembl:hasTarget " + 
			"<http://rdf.farmbio.uu.se/chembl/target/t" + targetID + ">. " +
			"}";
	
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		for (int i=0; i<matrix.getRowCount(); i++) {
			try {
				String smiles = matrix.get(i, "smiles");
				if (smiles != null && smiles.length() != 0) {
					Double value = Double.valueOf(matrix.get(i, "val"));
					if (value != Double.NaN)
						results.put(matrix.get(i, "smiles"), value);
				}
			} catch (NumberFormatException exception) {
				results.put(matrix.get(i, "smiles"), Double.NaN);
			}
		}
	
		return results;
	}

	public IStringMatrix getQSARData2(Integer targetID, String activity)
	throws BioclipseException {

		String sparql =
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/> "+
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?smiles ?val ?score ?unit " +
			"WHERE { " +
			" ?act a chembl:Activity."+
			" ?act chembl:type \"" + activity + "\"; " +
			"  chembl:onAssay ?ass; " +
			"  chembl:standardValue ?val; " +
			"  chembl:standardUnits ?unit;" +
			"  chembl:forMolecule ?mol. " +
			" ?mol bo:smiles ?smiles. " +
			" ?ass chembl:hasTarget " +
			"<http://rdf.farmbio.uu.se/chembl/target/t" + targetID + ">; " +
			"  chembl:hasConfScore ?score."+
			"}";

		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	public IStringMatrix getTargetIDWithEC(String ecNumber)
	throws BioclipseException{
		String sparql=
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
			"SELECT DISTINCT ?target ?description WHERE {" +
			" ?target a chembl:Target;" +
			"         dc:identifier "+ "\""+  ecNumber +"\""+ " ;" +
			"		  chembl:hasDescription ?description."+
			"}";
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	public IStringMatrix getTargetIDWithKeyword(String keyword)
	throws BioclipseException{
		String sparql=
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX dc: <http://purl.org/dc/elements/1.1/>"+
			"SELECT DISTINCT ?target ?key  WHERE {" +
			" ?target a chembl:Target." +
			"  ?target chembl:hasDescription ?key ." +
			"   FILTER regex(?key , " + "\"(" + keyword + ")\" , \"i\") ." +
			"}";
		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		cutter(matrix);
		return matrix;
	}

	public List<String> getTargetIDWithProteinSeq(String proteinSeq)
	throws BioclipseException{
		List<String> id = new ArrayList<String>();

		String sparql=
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?target WHERE {" +
			" ?target a chembl:Target;" +
			"         chembl:sequence "+ "\""+  proteinSeq +"\""+ " ." +
			"}";

		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		if(matrix.getColumn("target") != null){
			id = matrix.getColumn("target");
		}
		return id;

	}
	public List<String> getTargetSequence(Integer targetID)
	throws BioclipseException {
		List<String> countActivities = new ArrayList<String>();

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"SELECT DISTINCT ?seq "+
			"WHERE {" +
			"<http://rdf.farmbio.uu.se/chembl/target/t" + targetID + "> chembl:sequence ?seq ."+
			"}";

		IStringMatrix matrix = rdf.sparqlRemote(CHEMBL_SPARQL_ENDPOINT, sparql);
		if (matrix.getRowCount() > 0)
			countActivities.addAll(matrix.getColumn("seq"));

		return countActivities;
	}


	public void saveCSV(IFile filename, IStringMatrix fam, IProgressMonitor monitor)
	throws BioclipseException, IOException {
		String s;
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}

		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			for(int i=1; i<fam.getColumnCount()+1;i++){
				if(i<fam.getColumnCount()) s= fam.getColumnName(i) +", ";
				else s= fam.getColumnName(i) +"\n";
				byte but[]= s.getBytes();
				output.write(but);
			}

			for(int i=1; i<fam.getRowCount()+1; i++){
				for(int j=1; j< fam.getColumnCount()+1; j++){

					if(j < fam.getColumnCount()){
						s =fam.get(i, j) + ",";
					}                    
					else { s =fam.get(i, j) + "\n";
					}
					byte but[]= s.getBytes();
					output.write(but);
				}
			}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing file.", e);
		}

		monitor.worked(100);
		monitor.done();
	}
	public void saveCSV(IFile filename, ArrayList<TableItem> lst, IProgressMonitor monitor)
	throws BioclipseException, IOException {
		String s;
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			
//			for(int i = 0; i<lst.size(); i++){
				s=
				lst.get(0)+"";//.getText()+",";
//				lst.get(i).getText(i+1)+",";
//				lst.get(i).getText(i+2)+","+
//				lst.get(i).getText(i+3)+","+
//				lst.get(i).getText(i+4)+"/n";
			
				byte but[]= s.getBytes();
				output.write(but);
//				}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing file.", e);
		}

		monitor.worked(100);
		monitor.done();
	}
	
	//H�T
    public void saveCSVT(String in, Table tab, IProgressMonitor monitor)
	throws BioclipseException, IOException {
		
		IFile filename = ResourcePathTransformer.getInstance().transform(in);
		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}
		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			TableItem[] ti = tab.getItems();			
			for(int i =0; i< ti.length;i++){
				String s="";
				for(int j = 0; j < tab.getColumnCount(); j++){  
					if(ti[i].getText(j).equals("")){
						j = tab.getColumnCount();
					}else s= s+ ti[i].getText(j) + ",";
				}
				s=s+"\n";
				byte but[]= s.getBytes();
				output.write(but);	
			}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing file.", e);
		}

		monitor.worked(100);
		monitor.done();
	}
	
	/* MoSS Section
	* Methods that involve MoSS interaction.
	*/

	public void MossSaveFormat(IFile filename, IStringMatrix matrix, IProgressMonitor monitor)
	throws BioclipseException, IOException {

		String s; 
		//IFile filename =ResourcePathTransformer.getInstance().transform(file);
		ArrayList<String> fam = new ArrayList<String>();

		for(int i=1; i<matrix.getRowCount()+1; i++){
			fam.add(matrix.get(i,"smiles"));
		}

		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}

		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			for(int i = 0; i<fam.size(); i++){
				s = i+1 +",0," + fam.get(i) + "\n";	
				byte but[]= s.getBytes();
				output.write(but); 
			}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing moss file.", e);
		} 

		monitor.worked(100);
		monitor.done();
	}
	public void MossSaveFormat(IFile filename, IStringMatrix matrix,IStringMatrix matrix2, IProgressMonitor monitor)
	throws BioclipseException, IOException {

		String s; 
		//IFile filename =ResourcePathTransformer.getInstance().transform(file);
		ArrayList<String> fam = new ArrayList<String>();
		ArrayList<String> fam2 = new ArrayList<String>();

		//Add compounds from matrix to lists
		for(int i=1; i<matrix.getRowCount()+1; i++){
			fam.add(matrix.get(i,"smiles"));
		}
		for(int i=1; i<matrix2.getRowCount()+1; i++){
			fam2.add(matrix2.get(i,"smiles"));
		}

		if (filename.exists()) {
			throw new BioclipseException("File already exists!");
		}

		if (monitor == null)
			monitor = new NullProgressMonitor();
		monitor.beginTask("Writing file", 100);
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			for(int i = 0; i<fam.size(); i++){
				s = "A"+(i+1) +",0," + fam.get(i) + "\n";	
				byte but[]= s.getBytes();
				output.write(but); 

			}
			for(int i=0 ; i<fam2.size(); i++){
				s = "B"+(i+1) +",0," + fam2.get(i) + "\n";	
				byte but[]= s.getBytes();
				output.write(but); 	
			}
			output.close();
			filename.create(
					new ByteArrayInputStream(output.toByteArray()),
					false,
					monitor
			);
		}
		catch (Exception e) {
			monitor.worked(100);
			monitor.done();
			throw new BioclipseException("Error while writing moss file.", e);
		} 

		monitor.worked(100);
		monitor.done();
	}

	//Collects compounds from a protein family
	public IStringMatrix MossProtFamilyCompounds(String fam, String actType, int limit) throws BioclipseException{

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			"SELECT DISTINCT ?smiles where{ " +
			"	?target a chembl:Target;" +
			"       chembl:classL5 ?fam. " + //+ " \"" + fam + "\"." +
			"	?assay chembl:hasTarget ?target ."+
			"   ?activity chembl:onAssay ?assay ;" +
			//			"       chembl:standardValue ?st ;" +
			"	    chembl:type ?actType ; " + //" \"" + actType + "\"."+ 
			"	    chembl:forMolecule ?mol ."+
			"	?mol bo:smiles ?smiles.  " +
			" FILTER regex(?fam, " + "\"^" + fam + "$\"" + ", \"i\")."+
			" FILTER regex(?actType, " + "\"^" + actType + "$\"" + ", \"i\")."+
			" }  LIMIT " + limit; 


		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql",sparql);
		return matrix;
	}

	public IStringMatrix MossProtFamilyCompounds(String fam, String actType) 
	throws BioclipseException{

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			"SELECT DISTINCT ?smiles where{ " +
			"	?target a chembl:Target;" +
			"       chembl:classL5 ?fam. " + 
			"	?assay chembl:hasTarget ?target . " +
			"   ?activity chembl:onAssay ?assay ;" +
			"	    chembl:type ?actType ; " + 
			"	    chembl:forMolecule ?mol ."+
			"	?mol bo:smiles ?smiles.  " +
			" FILTER regex(?fam, " + "\"^" + fam + "$\"" + ", \"i\")."+
			" FILTER regex(?actType, " + "\"^" + actType + "$\"" + ", \"i\")."+
			" }"; 

		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql",sparql);
		return matrix;
	}

	public IStringMatrix MossGetProtFamilyCompAct(String fam, String actType) 
	throws BioclipseException{

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			"SELECT DISTINCT ?smiles ?actval where{ " +
			"	?target a chembl:Target;" +
			"       chembl:classL5 ?fam. " + 
			"	?assay chembl:hasTarget ?target . " +
			"   ?activity chembl:onAssay ?assay ;" +
			"	    chembl:type ?actType ; " + 
			"		chembl:standardValue ?actval;"+
			"	    chembl:forMolecule ?mol ."+
			"	?mol bo:smiles ?smiles.  " +
			" FILTER regex(?fam, " + "\"^" + fam + "$\"" + ", \"i\")."+
			" FILTER regex(?actType, " + "\"^" + actType + "$\"" + ", \"i\")."+
			" }"; 

		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql",sparql);
		return matrix;
	}

	public IStringMatrix MossGetProtFamilyCompAct(String fam, String actType, Integer limit) 
	throws BioclipseException{

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			"SELECT DISTINCT ?smiles ?actval where{ " +
			"	?target a chembl:Target;" +
			"       chembl:classL5 ?fam. " + 
			"	?assay chembl:hasTarget ?target . " +
			"   ?activity chembl:onAssay ?assay ;" +
			"	    chembl:type ?actType ; " + 
			"		chembl:standardValue ?actval;"+
			"	    chembl:forMolecule ?mol ."+
			"	?mol bo:smiles ?smiles.  " +
			" FILTER regex(?fam, " + "\"^" + fam + "$\"" + ", \"i\")."+
			" FILTER regex(?actType, " + "\"^" + actType + "$\"" + ", \"i\")."+
			" } LIMIT" + limit; 

		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql",sparql);
		return matrix;
	}

	public IStringMatrix MossGetProtFamilyCompActBounds(String fam, String actType, Integer lower, Integer upper) 
	throws BioclipseException{
		
		return MossSetActivityBound(MossGetProtFamilyCompAct(fam, actType), lower, upper);
	
	}	

	public List<String> MossAvailableActivities(String fam) throws BioclipseException{
		List<String> list = new ArrayList<String>();

		String sparql =
			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
			"SELECT DISTINCT ?actType where{ " +
			"	?activity chembl:type ?actType; " +  
			"             chembl:onAssay ?assay ." +
			"	?assay chembl:hasTarget ?target ." +
			"	?target a chembl:Target;" +
			"           chembl:classL5 ?fam. " + 
			" FILTER regex(?fam, " + "\"^" + fam + "$\"" + ", \"i\")."+
			"}";

		//			"PREFIX chembl: <http://rdf.farmbio.uu.se/chembl/onto/#> " +
		//			"PREFIX bo: <http://www.blueobelisk.org/chemistryblogs/>"+
		//			"SELECT ?actType where{ " +
		//			"	?target a chembl:Target;" +
		//			"           chembl:classL5 ?fam. " + 
		//			"	?assay chembl:hasTarget ?target ." +
		//			"   ?activity chembl:onAssay ?assay ." +
		//			"	?activity chembl:type ?actType . " + //" \"" + actType + "\"."+ 
		//			" FILTER regex(?fam, " + "\"" + fam + "\"" + ", \"i\")."+
		//		    "}LIMIT ";

		IStringMatrix matrix = rdf.sparqlRemote("http://rdf.farmbio.uu.se/chembl/sparql", sparql);
		if(matrix.getColumnCount() >0 && matrix.getRowCount() > 0){
			for(int i = 0; i<matrix.getRowCount()+1; i++){
				if(!list.contains(matrix.get(i,1)) && !matrix.get(i, 1).equals("")){
					list.add(matrix.get(i,1));
				}
			}
		}
		return list;
	}

	public IStringMatrix MossSetActivityBound(IStringMatrix matrix, int lower, int upper)
	throws BioclipseException{
		int cnt =1;
		StringMatrix modified = new StringMatrix();
		modified.setColumnName(1, "actval");
		modified.setColumnName(2, "smiles");

		//test if it contains smiles and activities. Not optimal...
		for(int i = 1; i<matrix.getColumnCount()+1; i++){
			if(!(matrix.getColumnName(i).equals("actval")||matrix.getColumnName(i).equals("smiles"))){
				throw new BioclipseException("The given matrix does not contain right information" +
				"for a MoSS activity cut-off.");
			}
		}
		
		for(int i = 1; i<matrix.getColumnCount()+1; i++){
			if(matrix.getColumnName(i).equals("actval")){
				for(int ii = 1; ii< matrix.getRowCount()+1;ii++){
					if( matrix.get(ii,"actval").equals("")){
						//matrix.set(ii, "actval", String.valueOf(0));
						if(0 >= lower && 0 <= upper){
							modified.set(cnt, 1, matrix.get(ii, "actval"));
							modified.set(cnt,2,matrix.get(ii,"smiles"));
							cnt++;
						}
					}
					else if(Double.parseDouble(matrix.get(ii,"actval")) >= lower && 
							Double.parseDouble(matrix.get(ii,"actval")) <= upper){
						modified.set(cnt, 1, matrix.get(ii, "actval"));
						modified.set(cnt,2,matrix.get(ii,"smiles"));
						cnt++;
					}
				}
			}
		}
		return modified;
	}
	public void MoSSViewHistogram(IStringMatrix matrix) throws BioclipseException{
		
		XYSeries series = new XYSeries("Activity for compounds");
		HistogramDataset histogramSeries = new HistogramDataset();
		histogramSeries.setType(HistogramType.FREQUENCY);
		ArrayList<Double> activites = new ArrayList<Double>();
		double value;
	 	int cnt =1;
		double[] histact = new double[matrix.getRowCount()+1];
		for(int i = 1; i< matrix.getRowCount()+1;i++){
			if(matrix.get(i,"actval").equals("")){ value =0;}
			else{value = Double.parseDouble(matrix.get(i,"actval"));}
			activites.add(value);
		}
		//Sort list to increasing order of activities and adds them to histogram
		Collections.sort(activites);
		for(int i=0; i< activites.size(); i++){
			double d=activites.get(i);
			histact[i]=d;
			int t= activites.size()-1;
			if(i == t){
				series.add(d,cnt);
			}else{
				double dd= activites.get(i+1);

				if(d==dd){
					cnt++;
				}
				else{
					histact[i]=d;
					series.add(d,cnt);
					cnt =1;
				}
			}
		}
		histogramSeries.addSeries("Histogram",histact,matrix.getRowCount());
		JFreeChart jfreechart = ChartFactory.createXYLineChart("Histogram", "Activity values", 
				"Number of compounds", histogramSeries,
				PlotOrientation.VERTICAL, true, false, false); 

		ChartFrame frame = new ChartFrame("Activities",jfreechart); 
		frame.pack(); 
		frame.setVisible(true);
	}

	
	
	

}//End
