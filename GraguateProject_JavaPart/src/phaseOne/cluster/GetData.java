package phaseOne.cluster;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GetData {

	public static List<float[]> getData() {
		List<float[]> data = new ArrayList<>();
		try {
			URL url = new URL("http://archive.ics.uci.edu/ml/machine-learning-databases/wine/wine.data");
			URLConnection connection = url.openConnection();
			InputStream inputStream = connection.getInputStream();
			Scanner scanner = new Scanner(inputStream);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] numsStr = line.split(",");
				float[] tmp = new float[numsStr.length - 1];
				for(int i = 1; i < numsStr.length; i++) {
					tmp[i - 1] = Float.parseFloat(numsStr[i]);
				}
				data.add(tmp);
			}
			scanner.close();
			inputStream.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}
	
}
