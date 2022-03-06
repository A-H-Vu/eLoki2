package script.mod;

import script.Script;

/**
 * Very basic interface for functions that modify parts of the script
 * @author Allen
 *
 */
public interface ScriptMod {
	
	public Script modify(Script script);
}
