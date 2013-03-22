package jpa.util;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;

import org.apache.log4j.Logger;

public final class BlobUtil {
	static final Logger logger = Logger.getLogger(BlobUtil.class);
	private BlobUtil() {
		// utility class
	}

	public static byte[] objectToBytes(Object obj) throws IOException {
		if (obj == null) return null;
		// convert java object to a output stream
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream objos = new ObjectOutputStream(baos);
		// write the object to the output stream
		objos.writeObject(obj);
		objos.flush();
		objos.reset();
		objos.close();
		// get byte array
		byte[] baosarray = baos.toByteArray();
		baos.close();
		return baosarray;
	}

	public static byte[] beanToXmlBytes(Object obj) throws IOException {
		if (obj == null) return null;
		// convert java object to a output stream using XMLEncoder
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		XMLEncoder encoder = new XMLEncoder(baos);
		encoder.writeObject(obj);
		encoder.flush();
		encoder.close();
		// get byte array
		byte[] baosarray = baos.toByteArray();
		baos.close();
		return baosarray;
	}

	public static Object bytesToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null) return null;
		// wrap the bytes into an object input stream
		ObjectInputStream objis = new ObjectInputStream(new ByteArrayInputStream(bytes));
		// get object from the input stream
		Object obj = objis.readObject();
		objis.close();
		return obj;
	}

	public static Object xmlBytesToBean(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null) return null;
		// wrap the bytes into an XMLDecoder
		XMLDecoder decoder = new XMLDecoder(new ByteArrayInputStream(bytes));
		// get object from XMLDecoder
		Object obj = decoder.readObject();
		decoder.close();
		return obj;
	}
	
    /**
	 * Returns a copy of the object, or null if the object cannot be serialized.
	 * @throws IllegalArgumentException if the object cannot be serialized.
	 */
    public static Object deepCopy(Object orig) {
    	if (orig == null) return null;
    	if (!(orig instanceof java.io.Serializable)) {
    		throw new IllegalArgumentException("Input object must be Serializable");
    	}
        Object obj = null;
        try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(orig);
            out.flush();
            out.close();
            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        }
        catch(IOException e) {
        	logger.error("IOException caught", e);
        }
        catch(ClassNotFoundException cnfe) {
        	logger.error("ClassNotFoundException caught", cnfe);
        }
        return obj;
    }
    
    public static void main(String[] args) {
    	try {
    		Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
			logger.info("Calendar 1: " + cal.getTime());
			cal.roll(Calendar.MONTH, false);
			logger.info("Calendar 2: " + cal.getTime());
			cal.roll(Calendar.MONTH, false);
			logger.info("Calendar 3: " + cal.getTime());
    	}
    	catch (Exception e) {
    		logger.error("Exceeption", e);
    	}
    }
}
