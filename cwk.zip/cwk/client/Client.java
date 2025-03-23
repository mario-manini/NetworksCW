import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        // Check if the client was given the correct number of arguments
        if (!(args.length == 1 || args.length == 2)) {
            System.err.println("Usage: java Client vote <option> or java Client list");
            System.exit(1);
        }


		String voteOption = null;
		String usage = args[0];

		if (args.length == 2){ //If there are two arguments, make the vote option the second one, unless the first argument is list, then return an error
        	voteOption = args[1];
			if (usage.equals("list")){
				System.err.println("Usage: java Client vote <option> or java Client list");
            	System.exit(1);
			}
		}
		

        // Check if the vote option is valid
        try (
            Socket socket = new Socket("localhost", 7777);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
			
			if (usage.equals("vote")){
            	out.println(voteOption); //If in voting mode, send vote to server
			}
			else if (usage.equals("list")){ //If in list mode, send 'usage list' to the server, since usage list can never be an option itself (it has a space)
			    out.println("usage list");
			}
			else{
				System.err.println("Invalid usage!");
            	System.exit(1);
			}
			String serverResponse = null; //While there are still lines from the server, read them and print them to the client
			while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse); 
            }

        } catch (IOException error) {
            System.err.println("Error connecting to server: " + error.getMessage());
            System.exit(1);
        }
    }
}
