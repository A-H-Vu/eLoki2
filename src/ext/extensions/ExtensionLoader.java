package ext.extensions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import clients.Client;
import clients.SeleniumChrome;
import clients.SeleniumFirefox;

/**
 * Class used to load extensions into the specified SeleniumBrowser
 * @author Allen
 *
 */
public class ExtensionLoader {
	
	public static void loadExtension(Client client, ExtensionsList... extensions) {
		for(ExtensionsList e:extensions) {
			if(client instanceof SeleniumChrome) {
				try {
					String name = extractExtension(e.getChromePath());
					((SeleniumChrome)client).addExtension(new File(name));
				}catch(IOException | NullPointerException e1) {
					System.err.println("Error Loading Extension "+e.getChromeName());
					e1.printStackTrace();
				}
			}
			else if(client instanceof SeleniumFirefox) {
				try {
					String name = extractExtension(e.getFirefoxPath());
					((SeleniumFirefox)client).addExtension(new File(name));
				}catch(IOException | NullPointerException e1) {
					System.err.println("Error Loading Extension "+e.getFirefoxName());
					e1.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Copes a file from within the jar file into the current working directory
	 * @param jarPath path within the jar file, should be absolute. Relative paths are relative to ExtensionLoader class
	 * @return Name of the extracted file
	 * @throws IOException
	 */
	public static String extractExtension(String jarPath) throws IOException {
		InputStream in = ExtensionLoader.class.getResourceAsStream(jarPath);
		String extName = jarPath.lastIndexOf('/')>0?jarPath.substring(jarPath.lastIndexOf('/')+1):jarPath;
		Files.copy(in, new File(extName).toPath(), StandardCopyOption.REPLACE_EXISTING);
		return extName;
	}
	

}
