package phaseOne.cluster;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GetDataFromFile {

	private String filePath = null;
	
	public GetDataFromFile(String filePath) {
		this.filePath = filePath;
	}
	
	public List<float[]> getData() throws FileNotFoundException {
		List<float[]> data = new ArrayList<>();
		Scanner in = new Scanner(new InputStreamReader(new FileInputStream(filePath)));
		while(in.hasNextLine()) {
			String line = in.nextLine();
			String[] numsStr = line.split("\t");
			float[] tmp = new float[numsStr.length - 1];
			for(int i = 0; i <numsStr.length - 1; i++) {
				tmp[i] = Float.parseFloat(numsStr[i]);
			}
			data.add(tmp);
		}
		in.close();
		return data;
	}
	
}
