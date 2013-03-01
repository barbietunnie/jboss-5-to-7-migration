package com.legacytojava.jbatch.common;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

class JcePbeTest {

	public static void main(String[] args) {
		
		try {
			PBEKeySpec pbeKeySpec;
		    PBEParameterSpec pbeParamSpec;
		    SecretKeyFactory keyFac;

		    // Salt
		    byte[] salt = {
		        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
		        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
		    };

		    // Iteration count
		    int count = 20;

		    // Create PBE parameter set
		    pbeParamSpec = new PBEParameterSpec(salt, count);

		    // Prompt user for encryption password.
		    // Collect user password as char array (using the
		    // "readPasswd" method from above), and convert
		    // it into a SecretKey object, using a PBE key
		    // factory.
		    pbeKeySpec = new PBEKeySpec("passw0rd".toCharArray());
		    keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		    // Create PBE Cipher
		    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");

		    // Initialize PBE Cipher with key and parameters
		    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		    // Our clear text
		    byte[] cleartext = "This is another example".getBytes();

		    // Encrypt the clear text
		    byte[] ciphertext = pbeCipher.doFinal(cleartext);
		    System.out.println("Encrypted text length: " + ciphertext.length + " " + new String(ciphertext));
		    
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		    byte[] cleartext2 = pbeCipher.doFinal(ciphertext);
		    System.out.println("Decrypted text: " + new String(cleartext2));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}