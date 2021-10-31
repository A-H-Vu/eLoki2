package script.action;

public enum ActionCompatibility {
	/**
	 * Action is compatible with the client
	 */
	Ok, 
	/**
	 * Action is incompatible but this incompatibility can be ignored and should not affect the script
	 */
	Ignore, 
	/**
	 * Action is incompatible and will break the script
	 */
	Incompatible

}
