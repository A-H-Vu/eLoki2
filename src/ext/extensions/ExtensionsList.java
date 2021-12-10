package ext.extensions;


/**
 * List of extensions that are packaged in the jar file that can be added to the browser
 * @author Allen
 *
 */
public enum ExtensionsList {
	IgnoreXFrame("Ignore-X-Frame-headers.crx","ignore_x_frame_options_header-1.6.9-an+fx.xpi");
	
	
	private String chromeName;
	private String firefoxName;
	ExtensionsList(String chromeName, String firefoxName){
		this.chromeName = chromeName;
		this.firefoxName = firefoxName;
	}
	
	public String getChromeName() {
		return chromeName;
	}
	
	public String getFirefoxName() {
		return firefoxName;
	}
	
	public String getChromePath() {
		return "/ext/extensions/chrome/"+chromeName;
	}
	
	public String getFirefoxPath() {
		return "/ext/extensions/firefox/"+firefoxName;
	}

}
