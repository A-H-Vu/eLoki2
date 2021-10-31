package clients;

/**
 * Base class for all clients
 * 
 * @author Allen
 *
 */
public abstract class Client {
	
	/**
	 * The client name, in general it should follow the format of `Library-Browser`
	 * or just Library if the library name if it does not support a particular browser.
	 * Additional dashes can be added i.e. Library-Browser-X-X if necessary to classify the client further
	 */
	private String name;
	
	/**
	 * 
	 * @param name see {@link#getName()} for details
	 */
	protected Client(String name) {
		this.name = name;
	}
	
	/**
	 * The client name, in general it should follow the format of `Library-Browser`
	 * or just Library if the library name if it does not support a particular browser.
	 * Additional dashes can be added i.e. Library-Browser-X-X if necessary to classify the client further
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Initialization function, called first before any non-configuration method to
	 * initialize the webdriver and the corresponding browser.
	 */
	public abstract void init();

}
