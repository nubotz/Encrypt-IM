import java.io.*;
import java.net.*;

public class ChatClient {

	public static void main (String[ ] args) throws IOException {

		Socket echoSocket = null;
		PrintWriter out = null;
		final BufferedReader in;
		String myID = null;

		try {
			echoSocket = new Socket ("127.0.0.1",33333);
			out = new PrintWriter (echoSocket.getOutputStream ( ),true);
			in = new BufferedReader (new InputStreamReader (echoSocket.getInputStream ()));
					
			myID = in.readLine ( );
			System.out.println("myID is "+ myID);
			
			new Thread(){
				public void run(){
					try{
						while(true)
							if(in.readLine ( ) == null) break;
							System.out.println(in.readLine ( ));
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}.start();
		}
		catch (UnknownHostException e) {
			System.out.println ("Don't know about host.");
			System.exit (1);
		}

		catch (Exception e) {
			System.out.println ("Couldn't get I/O.");
			System.exit (1);
		}

		
		BufferedReader stdIn = new BufferedReader (new InputStreamReader (System.in));
		String userInput;
		while ((userInput = stdIn.readLine ( )) != null) {
			out.println (myID+";;"+userInput);
		}

		out.close ( );
		//in.close ( );
		stdIn.close ( );
		echoSocket.close ( );
	}

}