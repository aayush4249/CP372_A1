/*
* Project: Post it Board
* Authors: Dylan Clarry && Aayush Sheth
* A multithreaded server/client app that allows multiple users to post, pin, and delete post it notes on a board
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {

	//Array list to store all the notes
	public ArrayList<Note> notes = new ArrayList<Note>();
	public ArrayList<String> pins = new ArrayList<>();
	public static Board board;
	// public static BufferedReader in;
	// public static PrintWriter out;
	public static int boardWidth;
	public static int boardHeight;

	public static void main(String[] args) throws Exception {
		System.out.println("server running");

		// aaget port number from args
		int portNumber = Integer.parseInt(args[0]);

		// create new server socket
		ServerSocket serverSocket = new ServerSocket(portNumber);


		//Create a new board
		board = createBoard(args);
		

		try {
			while(true) {

				// creates new client request
				new ClientRequest(serverSocket.accept()).start();
			}
		} finally {

			// close socket when done
			serverSocket.close();
		}
	}

	/*
	*
	* create the board
	*
	*/
	public static Board createBoard(String[] cmds) {
		int len = cmds.length;

		String[] colours = new String[len - 3];
		int k = 0;

		// get non-default colours from arguments
		for(int i = len; i > 4; i--) {
			colours[k] = cmds[i - 1];
			k++;
		}

		// add default colour to valid colours list
		colours[colours.length - 1] = cmds[3];

		// create new board
		Board board = new Board(Integer.parseInt(cmds[1]), Integer.parseInt(cmds[2]), cmds[3], colours);

		// board width and height properties
		boardWidth = Integer.parseInt(cmds[1]);
		boardHeight = Integer.parseInt(cmds[2]);

		return board;
	}
	
	/*
	*
	* ClientRequest
	*
	*/
	private static class ClientRequest extends Thread {
		private Socket socket;
		private int clientCount;
		
		// constructor
		public ClientRequest(Socket socket) {
			this.socket = socket;
			this.clientCount = clientCount;
			//System.out.println("New connection with client " + this.clientCount + " at port " + this.socket.getLocalPort());
		}
		
		@Override
		public void run() {
			try {

				// process client requests
				processRequest();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch(Exception e) {
					//System.out.println("error: " + e);
				}
				//System.out.println("connection with client " + this.clientCount + " closed");
			}
		}

		public void processRequest() throws Exception {

			// server i/o readers/writers
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			out.println("connected");

			while(true) {

				// client request input
				String input = in.readLine();
				String[] cmdParts = input.split("\\s+");

				// client method request
				String method = cmdParts[0].toLowerCase();

				// switch case by method type to call appropriate parser
				switch(method) {
					case "post":
					parsePost(input);
					break;

					case "get":
					parseGet(input);
					break;

					case "pin":
					parsePin(input);
					break;

					case "unpin":
					parseUnpin(input);
					break;

					case "clear":
					board.clear();
					out.println("cleared all unpinned notes");
					break;

					default:
						//System.out.println("no valid command entered");
					break;
				}
			}
		}

		// parses a post request
		public void parsePost(String req) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);


				// split request by special indicator then by spaces
				String[] cmdParts = req.split("%\\^@@\\^%");
				String[] reqParts = cmdParts[0].split("\\s+");

				int x = Integer.parseInt(reqParts[1]);
				int y = Integer.parseInt(reqParts[2]);
				int width = Integer.parseInt(reqParts[3]);
				int height = Integer.parseInt(reqParts[4]);
				String colour = reqParts[5].equals("null") ? board.defaultColour.toLowerCase() : reqParts[5].toLowerCase();
				String msg = cmdParts[1];

				// checks if colour is a part of the boards valid colour list
				if(!board.containsColour(colour)) {

					// output error if colour is invalid
					out.println("503: " + colour + " is not in the boards valid colours%^||^%%^||^%list of valid colours:%^||^%" + board.stringValidColours());
				} else if( (x + width) > boardWidth || (y + height) > boardHeight ) {
					out.println("502: Invalid Dimensions");
				} else {

					// post note
					board.post(x, y, width, height, colour, msg);

					// send a post receipt back to the client
					String noteReceipt = "x: " + x + "%^||^%y: " + y + "%^||^%width: " + width + "%^||^%height: " + height + "%^||^%colour: " + colour + "%^||^%message: " + msg;
					out.println("note receipt:%^||^%========%^||^%" + noteReceipt);
				}
			} catch(Exception e) {
				
			}
		}

		// parses a get request
		public void parseGet(String req) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				String[] cmdParts = req.split("\\s+");

				// GET PINS request
				if(cmdParts[1].toLowerCase().equals("pins")) {
					String stringPins = board.getPins();
					System.out.println(stringPins);
					out.println(stringPins);
				} else {
					String[] colourReq = cmdParts[1].split("=");
					String[] containsReq = cmdParts[2].split("\\|");
					String[] refersToReq = req.split("refersTo=");

					int x;
					int y;
					String colour;
					String msg;

					// if colour or msg is null set to empty
					colour = colourReq[1].toLowerCase().equals("null") ? "" : colourReq[1].toLowerCase();
					msg = refersToReq[1].toLowerCase().equals("null") ? "" : refersToReq[1];

					// if x/y are empty
					if(containsReq[1].toLowerCase().equals("null")) {
						x = -1;
						y = -1;
					} else {
						x = Integer.parseInt(containsReq[1]);
						y = Integer.parseInt(containsReq[2]);
					}

					System.out.println(x + " " + y + " " + colour + " " + msg);

					// call boards get method
					out.println(board.get(x, y, colour, msg));			
				}
			} catch(Exception e) {
				
			}
		}

		// parses a pin request
		public void parsePin(String req) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				String[] reqParts = req.split("\\s+");
				String[] coorParts = reqParts[1].split(",");

				int x = Integer.parseInt(coorParts[0]);
				int y = Integer.parseInt(coorParts[1]);

				// call boards pin method
				board.pin(x, y);
				out.println("Pinned");
			} catch(Exception e) {
				
			}
		}

		// parses an unpin request
		public void parseUnpin(String req) {
			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				String[] reqParts = req.split("\\s+");
				String[] coorParts = reqParts[1].split(",");

				int x = Integer.parseInt(coorParts[0]);
				int y = Integer.parseInt(coorParts[1]);

				// call boards unpin method
				board.unpin(x, y);
				out.println("Unpinned");
			} catch(Exception e) {
				
			}
		}
	}

	/*
	*
	* Board
	*
	*/
	public static class Board extends Server {
		int width;
		int height;
		String defaultColour;
		String[] colours;
		
		// board constructor
		public Board(int width, int height, String defaultColour, String[] colours ) {
			this.width = width;
			this.height = height;
			this.defaultColour = defaultColour.toLowerCase();
			this.colours = colours;
		}

		// returns string of valid colours
		private String stringValidColours() {
			String validColours = "";
			for(int i = 0; i < this.colours.length; i++) {
				validColours += this.colours[i] + "%^||^%";
			}
			return validColours;
		}

		// returns boolean value if colour is in valid colours
		private boolean containsColour(String colour) {
			for(int i = 0; i < this.colours.length; i++) {
				if(colour.equals(this.colours[i])) {
					return true;
				}
			}
			return false;
		}

		//Clear Unpinned notes
		private void clear() {
			for (int i = 0; i < notes.size(); i++) {
				if (notes.get(i).pinned == false) {
					notes.remove(i);
					i--;
				}
			}
		}

		//Add a new note to array list
		private void post(int xCord, int yCord, int width, int height, String colour, String message) {
			Note n = new Note(xCord, yCord, width, height, colour, message);
			notes.add(n);
		}

		//Pin a note
		private void pin(int xCord, int yCord) {
			for (int i = 0; i < notes.size(); i++){
				//If statement to that checks each notes coordinates
				//Checks if requested coordinates are within the boundaries of any notes
				//If the note isn't already pinned then it gets pinned
				if((notes.get(i).xCord <= xCord && notes.get(i).xCord2 >= xCord && notes.get(i).yCord <= yCord && notes.get(i).yCord2 >= yCord)){
					if(notes.get(i).pinned == false){
						notes.get(i).pinned = true;
					}
				}
			}

			// add pins
			pins.add(Integer.toString(xCord) + "," + Integer.toString(yCord));
		}

		//Unpin a note
		private void unpin(int xCord, int yCord) {
			for (int i = 0; i < notes.size(); i++){
				//If statement to that checks each notes coordinates
				//Checks if requested coordinates are within the boundaries of any notes
				//If the note is already pinned then it gets unpinned
				if((notes.get(i).xCord <= xCord && notes.get(i).xCord2 >= xCord && notes.get(i).yCord <= yCord && notes.get(i).yCord2 >= yCord)){
					if(notes.get(i).pinned == true){
						notes.get(i).pinned = false;
					}
				}
			}
			
			String coords = Integer.toString(xCord)+","+Integer.toString(yCord);

			for (int i =0; i < pins.size(); i++){
				if(pins.get(i).equals(coords)){
					pins.remove(i);
					i--;
				}
			}
		}

		//Gets all pins
		private String getPins(){
			String stringPins = "";
			for (int i = 0; i < pins.size(); i++){
				stringPins += pins.get(i) + "%^||^%";
			}
			System.out.println("stringPins");
			return stringPins;
		}



		//Get function
		private String get(int xCord, int yCord, String colour, String message){

			System.out.println("this is the get functioningngngngg");
			
			//Get new arraylist matching the colours
			ArrayList<Note> copyList = new ArrayList<Note>(notes);

			// filter by colour
			if (!colour.equals("")){
				cList(copyList,colour);
			}

			// filter by coordinates
			if (xCord != -1){
				
				cordList(copyList, xCord, yCord);
			}

			// filter by message
			if (!message.equals("")){
				refersList(copyList, message);
			}

			String stringNote = "";
			for (int i =0; i < copyList.size();i++) {
				System.out.println(copyList.get(i).message);
				stringNote += copyList.get(i).message + "%^||^%";
			}
			return stringNote;
		}		
	}

		//Removes non matching colours from the arraylist
	public void cList(ArrayList<Note> copyList,String colour2){

			//Loop to find all notes with matching colour
		for(int i=0; i < copyList.size(); i ++){

					//If no match
			if(!(copyList.get(i).colour.equals(colour2))){

						//Remove the note if it doesnt belong
				copyList.remove(i);
				i--;
			}
		}	
	}

		//Removes all notes that dont have the coordinates
	public void cordList(ArrayList<Note> copyList, int xCord, int yCord){
		for(int i = 0; i < copyList.size(); i++ ) {

				//If statement to that checks each notes coordinates
				//Checks if requested coordinates are within the boundaries of any notes
				//Removes all that are not in boundary
			if(!(copyList.get(i).xCord <= xCord && copyList.get(i).xCord2 >= xCord && copyList.get(i).yCord <= yCord && copyList.get(i).yCord2 >= yCord)) {
				copyList.remove(i);
				i--;
			}
		}
	}

		//Removes all notes that dont have the required substring
	public void refersList(ArrayList<Note> copyList, String message){
		for (int i = 0; i < copyList.size(); i++){
				//Check if index exists for substring
			if(copyList.get(i).message.indexOf(message) == -1){
					copyList.remove(i);//Remove notes with no index of message
					i--;
				}
			}
		}
		/*
	*
	* Note
	*
	*/
	public static class Note {


		int width, height, xCord, yCord, xCord2,yCord2;
		String colour, message;
		boolean pinned = false;

		// note constructor
		public Note(int xCord, int yCord, int width, int height, String colour, String message){
			this.xCord = xCord;
			this.yCord = yCord;
			this.width = width;
			this.height = height;
			this.colour = colour;
			this.message = message;
			this.xCord2 = this.xCord + this.width;
			this.yCord2 = this .yCord + this.height;
			this.pinned = false;
		}
	}
}