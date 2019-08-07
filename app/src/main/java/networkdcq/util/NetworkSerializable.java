package networkdcq.util;

/**
 * This interface allows a mean of multi-platform serialization
 */

public interface NetworkSerializable {
	
	
	/** Variable members separator  */
	public static char VARIABLE_MEMBER_SEPARATOR = '&';
	/** End of viariables flag */
	public static char VARIABLE_END_OF_VARIABLES = '%';
	
	/**
	 * Returns a String representation of the object, containing its variables, 
	 * separated with {@code VARIABLE_MEMBER_SEPARATOR}, and {@code VARIABLE_END_OF_VARIABLES}
	 * indicating the end of variable members.
	 */
    public String networkSerialize();

	/**
	 * Returns a new instance of the object, based on the received data, 
	 * according to {@link networkSerialize()} logic
	 */
    public Object networkDeserialize(String data);
}
