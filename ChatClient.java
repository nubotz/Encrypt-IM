import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;


public class ChatClient {
public static final String ALGORITHM = "RSA";
  //public static final String PRIVATE_KEY_FILE = "./keys/private.key";
  public static final String PRIVATE_KEY_PATH = "./keys/private";
    //public static final String PUBLIC_KEY_FILE = "./keys/public.key";
	public static final String PUBLIC_KEY_PATH = "./keys/public/";
	public static final int ID_LENGTH = 3;
	public static String myID = "nobody";
	

	public static void main (String[ ] args) throws IOException {

		Socket echoSocket = null;
		PrintWriter out = null;
		final BufferedReader in;
		
final Base64.Decoder decoder = Base64.getDecoder();
final Base64.Encoder encoder = Base64.getEncoder();

		try {
			echoSocket = new Socket ("127.0.0.1",33333);
			out = new PrintWriter (echoSocket.getOutputStream ( ),true);
			in = new BufferedReader (new InputStreamReader (echoSocket.getInputStream ()));
			
			
			Scanner input = new Scanner(System.in);
			System.out.print("enter username: ");
			String tryUserName = input.nextLine();
			Console console = System.console();
			char passArray[] = console.readPassword("password: ");
			String tryUserPW = String.valueOf(passArray);
			
			
			Authenticate_user au = new Authenticate_user();
			boolean authen = au.login(tryUserName, tryUserPW);
			if (!authen){
			System.out.println("Wrong user name or pw");
			System.exit(0);
			System.out.println("after exit");
			}
			
			myID = tryUserName;
			out.println("myIDis::"+myID);

			
			//myID = in.readLine ( );
			//System.out.println("myID is "+ myID);
			
			if (!areKeysPresent()) {
        // Method generates a pair of keys using the RSA algorithm and stores it
        // in their respective files
		System.out.println("generating key");
        generateKey();
		System.out.println("key generated");
      }else{
		System.out.println("key are present");
	  }
			
			
			//this thread is used to keep listening server reply
			new Thread(){
				public void run(){
					try{
						while(true){						
						String message = in.readLine();
							if (message == null){
								System.out.println("read is null, break now");
								break;
							}
							//split message first
							String parts[] = message.split(";;",2);
							
							// Decrypt the cipher text using the private key.
							 ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_PATH + myID +"/private_" + myID + ".key"));
							  final PrivateKey privateKey = (PrivateKey) inputStream.readObject();
							  final String plainText = decrypt(decoder.decode(parts[1]), privateKey);
							System.out.println(parts[0]+": " + plainText);
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
			//////////////////////////////////////
		

		//write message to server
		BufferedReader stdIn = new BufferedReader (new InputStreamReader (System.in));
		String targetID, userInput;
		System.out.println("send to who?");
		while ((targetID = stdIn.readLine ( )) != null) {
		//check if have targetID's public key
		File targetPublicKey = new File(PUBLIC_KEY_PATH + targetID+".key");

    if (!targetPublicKey.exists()) {
		//if not exist, get it from target
		out.println (myID+";;"+targetID);
		System.out.println ("needPubKey");
		System.out.println("send to who?");
    }else{
			System.out.println("input message:");
		userInput = stdIn.readLine ( );
		
		
		final String originalText = userInput;
      ObjectInputStream inputStream = null;

      // Encrypt the string using the public key
      inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_PATH + targetID+".key"));
      final PublicKey publicKey = (PublicKey) inputStream.readObject();
      final byte[] cipherText = encrypt(originalText, publicKey);

      
      // Printing the Original, Encrypted and Decrypted Text
	  String encodedText = encoder.encodeToString(cipherText);
      System.out.println("Encrypted: " + encodedText);
		out.println (myID+";;"+targetID);
		out.println (encodedText);
	  System.out.println("send to who?");
	}
		
		
		
		}
out.close();
		//in.close ( );
		stdIn.close ( );
		echoSocket.close ( );
		}catch (Exception e) {
				System.out.println ("Exception");
				e.printStackTrace();
				System.exit (1);
		}
	}
	
	  public static byte[] encrypt(String text, PublicKey key) {
    byte[] cipherText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);
      // encrypt the plain text using the public key
      cipher.init(Cipher.ENCRYPT_MODE, key);
      cipherText = cipher.doFinal(text.getBytes());
    } catch (Exception e) {
      e.printStackTrace();
    }
    return cipherText;
  }
  
  
    public static String decrypt(byte[] text, PrivateKey key) {
    byte[] dectyptedText = null;
    try {
      // get an RSA cipher object and print the provider
      final Cipher cipher = Cipher.getInstance(ALGORITHM);

      // decrypt the text using the private key
      cipher.init(Cipher.DECRYPT_MODE, key);
      dectyptedText = cipher.doFinal(text);

    } catch (Exception ex) {
      ex.printStackTrace();
    }

    return new String(dectyptedText);
  }
  
    public static void generateKey() {
    try {
      final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
      keyGen.initialize(1024);
      final KeyPair key = keyGen.generateKeyPair();

      File privateKeyFile = new File(PRIVATE_KEY_PATH + myID +"/private_" + myID + ".key");
      File publicKeyFile = new File(PUBLIC_KEY_PATH + myID + ".key");

      // Create files to store public and private key
      if (privateKeyFile.getParentFile() != null) {
        privateKeyFile.getParentFile().mkdirs();
      }
      privateKeyFile.createNewFile();

      if (publicKeyFile.getParentFile() != null) {
        publicKeyFile.getParentFile().mkdirs();
      }
      publicKeyFile.createNewFile();

      // Saving the Public key in a file
      ObjectOutputStream publicKeyOS = new ObjectOutputStream(
          new FileOutputStream(publicKeyFile));
      publicKeyOS.writeObject(key.getPublic());
      publicKeyOS.close();

      // Saving the Private key in a file
      ObjectOutputStream privateKeyOS = new ObjectOutputStream(
          new FileOutputStream(privateKeyFile));
      privateKeyOS.writeObject(key.getPrivate());
      privateKeyOS.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * The method checks if the pair of public and private key has been generated.
   * 
   * @return flag indicating if the pair of keys were generated.
   */
  public static boolean areKeysPresent() {

    File privateKey = new File(PRIVATE_KEY_PATH + myID +"/private_" + myID + ".key");
    File publicKey = new File(PUBLIC_KEY_PATH + myID + ".key");

    if (privateKey.exists() && publicKey.exists()) {
      return true;
    }
    return false;
  }

}