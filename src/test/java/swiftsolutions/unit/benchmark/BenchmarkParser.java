package swiftsolutions.unit.benchmark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BenchmarkParser {

	private String _folder;
	private File[] _graphs;
	private Map<String, ArrayList<File>> _processorCatagory = new HashMap<String, ArrayList<File>>();
	private Map<String, ArrayList<File>> _typeCatagory = new HashMap<String,  ArrayList<File>>();
	private Map<String, ArrayList<File>> _nodesCatagory = new HashMap<String,  ArrayList<File>>();


	public BenchmarkParser(String folder) {

		_folder = folder;
		_graphs = new File(_folder).listFiles();

	}

	public void catagoriseFiles() {

		for(File file : _graphs) {

			//String[0]: processors
			//String[1]: type
			//String[3]: nodes
			String[] name = file.getName().split("_");
			if(name.length >= 4) {
				
				if(name[3].equals("Nodes")) {name[3] = name [4];}

				if(!_processorCatagory.containsKey(name[0])) {

					ArrayList<File> newCat = new ArrayList<File>();
					newCat.add(file);
					_processorCatagory.put(name[0], newCat);

				}else {

					_processorCatagory.get(name[0]).add(file);

				}

				if(!_typeCatagory.containsKey(name[1])) {

					ArrayList<File> newCat = new ArrayList<File>();
					newCat.add(file);
					_typeCatagory.put(name[1], newCat);

				}else {

					_typeCatagory.get(name[1]).add(file);

				}

				if(!_nodesCatagory.containsKey(name[3])) {

					ArrayList<File> newCat = new ArrayList<File>();
					newCat.add(file);
					_nodesCatagory.put(name[3], newCat);

				}else {

					_nodesCatagory.get(name[3]).add(file);

				}

			}
		}

	}

	public Map<String, ArrayList<File>> getProcessorCatagory(){

		return _processorCatagory;

	}

	public Map<String, ArrayList<File>> getTypeCatagory(){

		return _typeCatagory;

	}

	public Map<String, ArrayList<File>> getNodesCatagory(){

		return _nodesCatagory;

	}

	public ArrayList<File> getAllGraphs(){

		return new ArrayList<File>(Arrays.asList(_graphs));

	}


}
