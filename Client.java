/*
* Project: Post it Board
* Authors: Dylan Clarry && Aayush Sheth
* A multithreaded server/client app that allows multiple users to post, pin, and delete post it notes on a board
*/

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.awt.event.ActionEvent;

public class Client {

	private JFrame frame;
	private JTextField ipField;
	private JTextField tfPort;
	private JTextField tfXpin;
	private JTextField tfYpin;
	private JTextField tfXpost;
	private JTextField tfYpost;
	private JTextField tfWidthPost;
	private JTextField tfHeightPost;
	private JTextField tfColourPost;
	private JTextField tfXget;
	private JTextField tfYget;
	private JTextField tfColourGet;
	private JTextField tfMessageGet;
	static JButton btnConnect = new JButton("Connect");
	static JButton btnDisconnect = new JButton("Disconnect");
	static Socket socket = null;
	// static PrintWriter output = null;
	// static BufferedReader input = null;
	static JTextArea taOutput = new JTextArea();
	private JTextField tfXcord;
	private JTextField tfYcord;
	static JTextArea taMessage = new JTextArea();
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Client window = new Client();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Client() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setResizable(false);
		frame.setTitle("Post it Board");
		frame.setBounds(100, 100, 640, 593);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblIpAddess = new JLabel("IP Addess:");
		lblIpAddess.setBounds(10, 15, 89, 14);
		frame.getContentPane().add(lblIpAddess);
		
		
		//Connect
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				taOutput.setText("");
				String ip = ipField.getText().replaceAll("\\s+", "");
				int port = -1;
				//If bad inputs occur then a 501 error is sent
				if(!tfPort.getText().replaceAll("\\s+", "").equals("")) {
					port = Integer.parseInt(tfPort.getText());
				}

				if(port == -1 || ip.equals("")) {
					taOutput.setText("501: bad connection");
				} else {
					try {
						//Connection is made if inputs are good
						socket = new Socket(ip, port);
						BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
						taOutput.setText("connected");
						btnConnect.setEnabled(false);
						System.out.println(input.readLine());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		btnConnect.setBounds(401, 11, 93, 23);
		frame.getContentPane().add(btnConnect);

		
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				taOutput.setText("");
				try {
					//Close the connection
					//output.close();
					//input.close();
					socket.close();
					btnConnect.setEnabled(true);
				} catch (IOException e) {
					taOutput.setText("disconnected");
				}
			}
		});
		btnDisconnect.setBounds(504, 11, 111, 23);
		frame.getContentPane().add(btnDisconnect);
		
		JButton btnPost = new JButton("POST");
		btnPost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

					// POST
					if(taMessage.getText().equals("") || tfXpost.getText().equals("") || tfYpost.getText().equals("") || tfWidthPost.getText().equals("") || tfHeightPost.getText().equals("")) {
						taOutput.setText("Invalid Input");
					}
					
					else {
						String x = tfXpost.getText().replaceAll("\\s+", "");
						String y = tfYpost.getText().replaceAll("\\s+", "");
						String width = tfWidthPost.getText().replaceAll("\\s+", "");
						String height = tfHeightPost.getText().replaceAll("\\s+", "");
						String colour = tfColourPost.getText().replaceAll("\\s+", "").equals("") ? "null" : "" + tfColourPost.getText().replaceAll("\\s+", "");
						String msg = taMessage.getText();

						String cmd = "POST " + x + " " + y + " " + width + " " + height + " " + colour + "%^@@^%" + msg;
						output.println(cmd);
						try {
							String cmdInput = input.readLine();

							cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");

							taOutput.setText(cmdInput);
						} catch(IOException e) {
							System.out.println("error:" + e);
						}
					}
				} catch(Exception e) {

				}

				
			}
		});
		btnPost.setBounds(504, 94, 83, 79);
		frame.getContentPane().add(btnPost);

		JButton btnGet = new JButton("GET");
		btnGet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

					// GET
					taOutput.setText("");
					try {
						//Parse to integer from the fields and run a get function 
						String x = tfXget.getText().replaceAll("\\s+", "").equals("") ? "-1" : "" + Integer.parseInt(tfXget.getText().replaceAll("\\s+", ""));
						String y = tfYget.getText().replaceAll("\\s+", "").equals("") ? "-1" : "" + Integer.parseInt(tfYget.getText().replaceAll("\\s+", ""));
						String colour = tfColourGet.getText().equals("") ? "null" : tfColourGet.getText();
						String refersTo = tfMessageGet.getText().equals("") ? "null" : tfMessageGet.getText();

						taOutput.setText(x + " " + y);

						String cmd = "GET colour=" + colour + " contains=|" + x + "|" + y + "| refersTo="  + refersTo + "";
						output.println(cmd);
						try {
							String cmdInput = input.readLine();

							cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");

							System.out.println(cmdInput);
							taOutput.setText(cmdInput);
						} catch(IOException e) {
							System.out.println("error:" + e);
						}
					} catch(Exception e) {
						taOutput.setText("error: " + e);
					}
				} catch(Exception e) {

				}
			}
		});
		btnGet.setBounds(504, 227, 83, 23);
		frame.getContentPane().add(btnGet);

		JButton btnPin = new JButton("PIN");
		btnPin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);


					taOutput.setText("");
					// PIN
					if(tfXpin.getText().equals("") || tfYpin.getText().equals("")) {
						taOutput.setText("Invalid Input");
					}
					

					try {
						String x = "" + Integer.parseInt(tfXpin.getText().replaceAll("\\s+", ""));
						String y = "" + Integer.parseInt(tfYpin.getText().replaceAll("\\s+", ""));

						String cmd = "PIN " + x + "," + y;
						output.println(cmd);
						try {
							String cmdInput = input.readLine();

							cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");

							taOutput.setText(cmdInput);
						} catch(IOException e) {
							System.out.println("error:" + e);
						}
					} catch(Exception e) {
						System.out.println("error: " + e);
						taOutput.setText("error: non-numeric values are not valid x/y coordinates");
					}
				} catch(Exception e) {
					
				}
			}
		});
		btnPin.setBounds(339, 328, 83, 23);
		frame.getContentPane().add(btnPin);

		JButton btnUnpin = new JButton("UNPIN");
		btnUnpin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

					// UNPIN
					taOutput.setText("");
					try {
						//unpin matching notes
						String x = "" + Integer.parseInt(tfXpin.getText().replaceAll("\\s+", ""));
						String y = "" + Integer.parseInt(tfYpin.getText().replaceAll("\\s+", ""));

						String cmd = "UNPIN " + x + "," + y;
						output.println(cmd);
						try {
							String cmdInput = input.readLine();

							cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");

							taOutput.setText(cmdInput);
						} catch(IOException e) {
							System.out.println("error:" + e);
						}
					} catch(Exception e) {
						taOutput.setText("error: " + e);
					}
				} catch(Exception e) {
					
				}	
			}
		});
		btnUnpin.setBounds(434, 328, 83, 23);
		frame.getContentPane().add(btnUnpin);

		JButton btnClear = new JButton("CLEAR");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

					// CLEAR
					//Clear all notes that arent pinned
					taOutput.setText("");
					output.println("CLEAR");
					try {
						String cmdInput = input.readLine();

						cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");

						taOutput.setText(cmdInput);
					} catch(IOException e) {
						System.out.println("error:" + e);
					}
				} catch(Exception e) {
					
				}
			}
		});

		//Get the coordinates of all pinned notes
		JButton btnGetPins = new JButton("Get Pins");
		btnGetPins.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

					output.println("GET PINS");
					try {
						String cmdInput = input.readLine();
						cmdInput = cmdInput.replaceAll("%\\^\\|\\|\\^\\%", "\n");
						taOutput.setText(cmdInput);
					} catch(IOException e) {
						System.out.println("error:" + e);
					}
				} catch(Exception e) {
					
				}
			}
		});

		btnClear.setBounds(526, 328, 89, 23);
		frame.getContentPane().add(btnClear);

		JLabel lblEnterDataHere = new JLabel("Enter Post Message Here:");
		lblEnterDataHere.setBounds(313, 68, 251, 14);
		lblEnterDataHere.setVerticalAlignment(SwingConstants.BOTTOM);
		frame.getContentPane().add(lblEnterDataHere);

		ipField = new JTextField();
		ipField.setBounds(75, 12, 167, 20);
		frame.getContentPane().add(ipField);
		ipField.setColumns(10);

		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(252, 15, 31, 14);
		frame.getContentPane().add(lblPort);

		tfPort = new JTextField();
		tfPort.setBounds(295, 12, 96, 20);
		frame.getContentPane().add(tfPort);
		tfPort.setColumns(10);
		taMessage.setLineWrap(true);

		
		taMessage.setBounds(313, 93, 167, 80);
		frame.getContentPane().add(taMessage);

		JLabel lblServerOutput = new JLabel("Server Output:");
		lblServerOutput.setBounds(10, 381, 116, 14);
		frame.getContentPane().add(lblServerOutput);
		taOutput.setWrapStyleWord(true);
		taOutput.setLineWrap(true);

		
		taOutput.setEditable(false);
		taOutput.setBounds(10, 406, 605, 147);
		frame.getContentPane().add(taOutput);

		JLabel lblXpin = new JLabel("X Coordinate:");
		lblXpin.setBounds(10, 308, 116, 14);
		frame.getContentPane().add(lblXpin);

		tfXpin = new JTextField();
		tfXpin.setBounds(10, 329, 96, 20);
		frame.getContentPane().add(tfXpin);
		tfXpin.setColumns(10);

		JLabel lblYpin = new JLabel("Y Coordinate:");
		lblYpin.setBounds(162, 308, 93, 14);
		frame.getContentPane().add(lblYpin);

		tfYpin = new JTextField();
		tfYpin.setBounds(162, 329, 96, 20);
		frame.getContentPane().add(tfYpin);
		tfYpin.setColumns(10);

		tfXpost = new JTextField();
		tfXpost.setBounds(10, 94, 68, 23);
		frame.getContentPane().add(tfXpost);
		tfXpost.setColumns(10);

		JLabel lblXpost = new JLabel("X Coordinate:");
		lblXpost.setBounds(10, 68, 89, 14);
		frame.getContentPane().add(lblXpost);

		JLabel lblYpost = new JLabel("Y Coordinate:");
		lblYpost.setBounds(97, 68, 83, 14);
		frame.getContentPane().add(lblYpost);

		tfYpost = new JTextField();
		tfYpost.setBounds(97, 94, 68, 23);
		frame.getContentPane().add(tfYpost);
		tfYpost.setColumns(10);

		JLabel lblWidthPost = new JLabel("Width:");
		lblWidthPost.setBounds(10, 128, 48, 14);
		frame.getContentPane().add(lblWidthPost);

		tfWidthPost = new JTextField();
		tfWidthPost.setBounds(10, 153, 68, 20);
		frame.getContentPane().add(tfWidthPost);
		tfWidthPost.setColumns(10);

		JLabel lblHeightPost = new JLabel("Height:");
		lblHeightPost.setBounds(97, 128, 48, 14);
		frame.getContentPane().add(lblHeightPost);

		tfHeightPost = new JTextField();
		tfHeightPost.setBounds(97, 153, 68, 20);
		frame.getContentPane().add(tfHeightPost);
		tfHeightPost.setColumns(10);

		JLabel lblColourPost = new JLabel("Colour:");
		lblColourPost.setBounds(203, 68, 48, 14);
		frame.getContentPane().add(lblColourPost);

		tfColourPost = new JTextField();
		tfColourPost.setBounds(200, 94, 83, 79);
		frame.getContentPane().add(tfColourPost);
		tfColourPost.setColumns(10);

		JLabel lblXget = new JLabel("X Coordinate:");
		lblXget.setBounds(10, 199, 89, 14);
		frame.getContentPane().add(lblXget);

		tfXget = new JTextField();
		tfXget.setBounds(10, 230, 68, 20);
		frame.getContentPane().add(tfXget);
		tfXget.setColumns(10);

		JLabel lblYget = new JLabel("Y Coordinate:");
		lblYget.setBounds(97, 199, 93, 14);
		frame.getContentPane().add(lblYget);

		tfYget = new JTextField();
		tfYget.setColumns(10);
		tfYget.setBounds(97, 230, 68, 20);
		frame.getContentPane().add(tfYget);

		JLabel lblColourGet = new JLabel("Colour:");
		lblColourGet.setBounds(200, 199, 48, 14);
		frame.getContentPane().add(lblColourGet);

		tfColourGet = new JTextField();
		tfColourGet.setColumns(10);
		tfColourGet.setBounds(200, 228, 83, 20);
		frame.getContentPane().add(tfColourGet);

		JLabel lblRefersto = new JLabel("Refers To:");
		lblRefersto.setVerticalAlignment(SwingConstants.BOTTOM);
		lblRefersto.setBounds(313, 202, 167, 14);
		frame.getContentPane().add(lblRefersto);

		tfMessageGet = new JTextField();
		tfMessageGet.setColumns(10);
		tfMessageGet.setBounds(313, 228, 167, 20);
		frame.getContentPane().add(tfMessageGet);
		
		btnGetPins.setBounds(504, 270, 83, 23);
		frame.getContentPane().add(btnGetPins);
	}
}
