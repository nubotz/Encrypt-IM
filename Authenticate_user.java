import java.util.*;
import java.io.*;
import java.security.*;
import java.security.spec.*;

public class Authenticate_user {
	
	public static void main (String args[]) {
		
	}
	
	public boolean login(String id, String pw){
	try{
		
		
			////////////////////from java Lesson: Generating and Verifying Signatures
            /* Input and Convert the Encoded Public Key Bytes */
			FileInputStream keyfis = new FileInputStream("suepk");
			byte[] encKey = new byte[keyfis.available()];  
			keyfis.read(encKey);

			keyfis.close();
			
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encKey);
			KeyFactory keyFactory = KeyFactory.getInstance("DSA", "SUN");
			PublicKey pubKey = keyFactory.generatePublic(pubKeySpec);

			/* Input the Signature Bytes */
			FileInputStream sigfis = new FileInputStream("sig");
			byte[] sigToVerify = new byte[sigfis.available()]; 
			sigfis.read(sigToVerify);
			sigfis.close();

			/* Initialize the Signature Object for Verification */
			Signature sig = Signature.getInstance("SHA1withDSA", "SUN");
			sig.initVerify(pubKey);

			/* Supply the Signature Object With the Data to be Verified */
			FileInputStream datafis = new FileInputStream("password.txt");
			BufferedInputStream bufin = new BufferedInputStream(datafis);

			byte[] buffer = new byte[1024];
			int len;
			while (bufin.available() != 0) {
				len = bufin.read(buffer);
				sig.update(buffer, 0, len);
			};

			bufin.close();

			/* Verify the Signature */
			boolean verifies = sig.verify(sigToVerify);
			////////////////////from java Lesson: Generating and Verifying Signatures
			
			if (!verifies){
				System.out.println("someone modified password.txt, contact your admin.");
				System.exit(-1);
			}
			
			//////////verified that no one changed txt
			
			//accept user login
			//Scanner input = new Scanner(System.in);
        
			//System.out.print("enter username: ");
			String tryUserName = id;
			String tryUserPW = pw;
			
			//Console console = System.console();
			//char passArray[] = console.readPassword("password: ");
			//String tryUserPW = String.valueOf(passArray);
			
			//open password.txt
            Scanner sc = new Scanner(new FileReader("password.txt"));
			
			String userName, userPassword, salt;
			boolean foundMatchedUserName = false;
			
            while (sc.hasNext()) {
                // uname;password;randomUUID
                String user = sc.next();
                // split to 3 item
                String[] parts = user.split(";");
                userName = parts[0];
                userPassword = parts[1];
                salt = parts[2];
                
				if (tryUserName.equals(userName)){
					foundMatchedUserName = true;
					String hashedsaltedTryPW = toSHA1((tryUserPW+salt).getBytes());
					if (hashedsaltedTryPW.equals(userPassword)){
						System.out.println("you are authenticated. Good!!");
						return true;
					}else{
						//wrong password
						//System.out.println("Wrong user name or pw");
					}
					break;
				}
            }//end while
			
			if (!foundMatchedUserName){
				//wrong user name
				//System.out.println("Wrong user name / pw");
				return false;
			}
			
			return false;
        }
        catch (Exception e){
			e.printStackTrace();
        }
	return false;
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

