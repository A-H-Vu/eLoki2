package clients;

/**
 * Base class for all clients
 * 
 * @author Allen
 *
 */
public abstract class Client {

	private String name;

	protected Client(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
