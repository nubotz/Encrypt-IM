import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Iterator;

public class ChatServer {
	public static void main (String[ ] args) throws IOException {

		final ServerSocket ss;
		
		final Map<String, Socket> userMap = new HashMap<String, Socket>();
		try {
			ss = new ServerSocket (33333);
			new Thread(){
				int id=0;
				public void run(){
					while(true){
						try{
							Socket socket = ss.accept();
							String idString = "id" + id++;
							userMap.put(idString ,socket);
							new Thread(new MyRunnable(socket, userMap)).start();
							new PrintWriter (socket.getOutputStream (),true).println(idString);
						}catch (Exception e) {
							System.out.println ("Accept failed: 33333");
						}
					}
				}
			}.start();
		}
		catch (IOException e) {
			System.out.println ("Could not listen on port: 33333");
			System.exit (1);
		}
	}
	
}
		class MyRunnable implements Runnable {
		Socket socket = null;
		Map<String, Socket> userMap = null;
			public MyRunnable(Socket socket, Map<String, Socket> userMap){
				this.socket = socket;
				this.userMap = userMap;
			}
			public void run(){
			try{
			   BufferedReader in = new BufferedReader (new InputStreamReader (socket.getInputStream ( )));
			   String clientInput;
			   while (true) {
					clientInput = in.readLine ( );
					if (clientInput == null)
						break;
					//process input and pipe to target user
					//format: (sender); (receiver); (unencrypted message)
					String[] parts = clientInput.split(";;",3);
					new PrintWriter (userMap.get(parts[1]).getOutputStream ( ),true).println(parts[0]+": "+parts[2]);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			}
		}