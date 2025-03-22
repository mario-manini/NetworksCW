import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {

    private static final ConcurrentHashMap<String, Integer> voteCounts = new ConcurrentHashMap<>(); //Vote options and tally are stored on hash map

    public static void main(String[] args) {

		if (args.length < 2){
			System.err.println("Error: Server requires at least two voting options."); //At least two voting options are required
            System.exit(1);
		}

        // Initialize vote counts for each option passed as command line arguments
        for (int option = 0; option < args.length; option++) {
            voteCounts.put(args[option], 0);
        }

        ExecutorService executorService = Executors.newFixedThreadPool(30); //Create a fixed thread pool of 30 threads 

        try (ServerSocket serverSocket = new ServerSocket(7777)) {
            System.out.println("Server is running on port " + 7777);

            while (true) {
                Socket clientSocket = serverSocket.accept(); 
                executorService.submit(new ClientHandler(clientSocket)); //Client Requests handled by the executor
            }
        } catch (IOException e) {
            System.err.println("Error while starting the server: " + e.getMessage());
        }
    }


    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true)
            ) {

				
				String optionsList = String.join(",", voteCounts.keySet()); //Send list of options to the client

				String recievedInput = input.readLine();
				String voteOption = null;

				if (recievedInput.equals("usage list")){
					for (String option : Server.voteCounts.keySet()) {
                    	Integer count = Server.voteCounts.get(option); 
                    	output.println("'"+option+"'"+ " has "+count+" vote(s).");
						}
						logRequest(clientSocket.getInetAddress().toString(), "list");

				}
				else{
                	voteOption = recievedInput;

					if (voteOption != null && voteCounts.containsKey(voteOption)) { //Checks for a valid vote

						synchronized (voteCounts) {
							voteCounts.put(voteOption, voteCounts.get(voteOption) + 1);
							// Increment vote count safely
						}

						// Log the valid request
						logRequest(clientSocket.getInetAddress().toString(), "vote");

						output.println("Vote for " + voteOption + " registered successfully.");
					} else {
						output.println("Invalid vote option. Please vote for a valid option.");
					}
				}

            } catch (IOException error) {
                System.err.println("Error processing client connection: " + error.getMessage());
            } finally {
                try {
                    clientSocket.close();  // Close the client connection
                } catch (IOException error) {
                    System.err.println("Error closing client socket: " + error.getMessage());
                }
            }
        }

        // Log the client request to the log file
		
        private synchronized void logRequest(String clientIP, String request) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt", true))) {
                String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss"));
                String clientIpWithoutSlash = clientIP.startsWith("/") ? clientIP.substring(1) : clientIP;

                String logEntry = currentDate + "|" + currentTime + "|" + clientIpWithoutSlash + "|" + request;
                writer.write(logEntry);
                writer.newLine();
            } catch (IOException error) {
                System.err.println("Error writing to log file: " + error.getMessage());
            }
        }
    }
}
