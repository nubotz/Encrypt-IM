import java.util.*;
import java.io.*;
import java.security.*;

public class Create_user {
	
	public static void main (String args[]) {
		Scanner input = new Scanner(System.in);
        System.out.println("Please make sure the user name only contain alphabetic characters");
        System.out.print("Enter user name: ");
		//assume admin dont input invalid / already existed user name
        String userName = input.next();
		
        
        Console console = System.console();
        char passArray[] = console.readPassword("password: ");
        String userPassword = String.valueOf(passArray);
        
    
        try{
            String salt = UUID.randomUUID().toString();
            
            // hash the pw with salt
            String hashedsaltedPW = toSHA1((userPassword+salt).getBytes());
            
            // write into the file
            PrintWriter write = new PrintWriter(new BufferedWriter(new FileWriter("password.txt",true)));
            write.println(userName + ";" + hashedsaltedPW + ";" + salt);
            write.close();
			
			
			////////////////////from java Lesson: Generating and Verifying Signatures
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			keyGen.initialize(1024, random);
			
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			
			
			/* Get a Signature Object */
			Signature dsa = Signature.getInstance("SHA1withDSA", "SUN"); 
			
			/* Initialize the Signature Object */
			dsa.initSign(priv);
			
			
			/* Supply the Signature Object the Data to Be Signed */
			FileInputStream fis = new FileInputStream("password.txt");
			BufferedInputStream bufin = new BufferedInputStream(fis);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bufin.read(buffer)) >= 0) {
				dsa.update(buffer, 0, len);
			};
			bufin.close();
			
			/* Generate the Signature */
			byte[] realSig = dsa.sign();
			
			/* save the signature in a file named as "sig" */
			FileOutputStream sigfos = new FileOutputStream("sig");
			sigfos.write(realSig);
			sigfos.close();
			
			/* save the public key in a file named as "suepk" */
			byte[] key = pub.getEncoded();
			FileOutputStream keyfos = new FileOutputStream("suepk");
			keyfos.write(key);
			keyfos.close();
			
			////////////////////from java Lesson: Generating and Verifying Signatures

        }
        catch (Exception e){
            System.out.println("Exception ");
			e.printStackTrace();
        }
        
        System.out.println("new user: "+ userName + " is created");
     
	}
	
	
	public static String toSHA1(byte[] convertme) {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA-1");
		}catch(Exception e) {
			e.printStackTrace();
		} 
		return byteArrayToHexString(md.digest(convertme));
	}
	
	public static String byteArrayToHexString(byte[] b) {
	  String result = "";
	  for (int i=0; i < b.length; i++) {
		result +=
			  Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
	  }
	  return result;
	}
}