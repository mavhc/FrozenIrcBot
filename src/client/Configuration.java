package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class Configuration extends HashMap<String, String> {
	private static final long serialVersionUID = -6820273033623985817L;

	public static Configuration fromFile(File input) {
		if (input.exists()) {
			Configuration result = new Configuration();
			result.clear();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(new FileInputStream(input)))) {
				String str;
				while ((str = reader.readLine()) != null) {
					if (str.contains("//"))
						str = str.substring(0, str.indexOf("//"));
					if (str.contains(":"))
						if (str.split(":").length==2)
							result.put(str.split(":")[0].trim(), str.split(":")[1].trim());
						else
							result.put(str.split(":")[0].trim(), "");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			return result;
		} else {
			Configuration result = new Configuration();
			result.clear();

			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(Configuration.class.getResourceAsStream("/res/config.default.yml")))) {
				String str;
				while ((str = reader.readLine()) != null) {
					if (str.contains("//"))
						str = str.substring(0, str.indexOf("//"));
					if (str.contains(":"))
						if (str.split(":").length==2)
							result.put(str.split(":")[0].trim(), str.split(":")[1].trim());
						else
							result.put(str.split(":")[0].trim(), "");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			result.toFile(input);
			
			return result;
		}
	}

	public void toFile(File output) {
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(output)))) {		
			for (Entry<String, String> entry : entrySet()) {
				writer.write(String.format("%s:%s", entry.getKey(),
						entry.getValue())+"\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String[] getHandlers() {
		return get("handler").split(",");
	}
}
