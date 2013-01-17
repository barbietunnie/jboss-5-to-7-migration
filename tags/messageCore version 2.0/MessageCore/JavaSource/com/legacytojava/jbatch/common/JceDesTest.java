package com.legacytojava.jbatch.common;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

class JceDesTest {

	public static void main(String[] args) {
		
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("DES");
		    SecretKey desKey = keygen.generateKey();
		    
			Cipher desCipher;
		
		    // Create the cipher 
		    desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		    
		    // Initialize the cipher for encryption
		    desCipher.init(Cipher.ENCRYPT_MODE, desKey);
		
		    // Our clear text
		    byte[] cleartext = "This is just an example".getBytes();
		    cleartext = "00000123".getBytes();
		
		    // Encrypt the clear text
		    byte[] ciphertext = desCipher.doFinal(cleartext);
		    for (int i=0; i<ciphertext.length; i++) {
		    	byte b = ciphertext[i];
		    	System.out.println(b);
		    }
		    System.out.println("Encrypted text: " + new String(ciphertext));
		
		    // Initialize the same cipher for decryption
		    desCipher.init(Cipher.DECRYPT_MODE, desKey);
		
		    // Decrypt the cipher text
		    byte[] cleartext1 = desCipher.doFinal(ciphertext);
		    
		    System.out.println("Decrypted text: " + new String(cleartext1));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}