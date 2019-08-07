package networkdcq.util;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

public class MemoryUtils {

	
	  /**
	   * Function that get the size of an object in bytes
	   * 
	   * @param 
	   * 	object
	   * @return 
	   * 	Size in bytes of the object or -1 if the 
	   * 	object is null or in case of an error
	   * @see 
	   * 	http://www.java2s.com/Code/Android/Development/Functionthatgetthesizeofanobject.htm
	   */
	  public static final int sizeOf(Object object) {
	    
	    if (object == null)
	      return -1;
	    
	    try {
		    // Special output stream use to write the content
		    // of an output stream to an internal byte array.
		    ByteArrayOutputStream byteArrayOutputStream =
		      new ByteArrayOutputStream();
		    
		    // Output stream that can write object
		    ObjectOutputStream objectOutputStream =
		      new ObjectOutputStream(byteArrayOutputStream);
		    
		    // Write object and close the output stream
		    objectOutputStream.writeObject(object);
		    objectOutputStream.flush();
		    objectOutputStream.close();
		    
		    // Get the byte array
		    byte[] byteArray = byteArrayOutputStream.toByteArray();
		    
		    // TODO can the toByteArray() method return a
		    // null array ?
		    return byteArray == null ? 0 : byteArray.length;
	    }
	    catch (Exception e) {
	    	Logger.e(e.getMessage());
	    	return -1;
	    }
	  }
	  
}
