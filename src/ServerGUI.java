import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ServerGUI extends JFrame
{
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JTextArea ServerTextArea;
	private JButton startButton;
	private JButton stopButton;
	private int port = 8090;

	ArrayList<PrintWriter> clientOutputStreams;
	ArrayList<String> onlineUsers;
	ClientGUI chatGUI = new ClientGUI();

	public class ClientHandler implements Runnable {
		private BufferedReader reader;
		private Socket sock;
		private PrintWriter client;

		public ClientHandler(Socket clientSocket, PrintWriter user) {
			client = user;
			try {
				sock = clientSocket;
				//InputStreamReader isReader = new InputStreamReader(sock.getInputStream());
				ObjectInputStream input = new ObjectInputStream(sock.getInputStream());
				//reader = new BufferedReader(isReader);
				reader = (BufferedReader) input.readObject();
			} catch (Exception ex) {
				ServerTextArea.append("Error beginning StreamReader. \n");
			}

		}

		public void run() {
			String message;
			String username = ClientGUI.username;

			try {
				while (sock.isConnected() && !sock.isClosed()) {
					message = reader.readLine();

					ServerTextArea.append("Received: " + message + "\n");

					if (message.equals(username)) {
						if (onlineUsers.contains(username)) {
							tellEveryone("DENY \n");
							runServerWithGUI();
						} else {
							tellEveryone(("ACK \n"));
							addUser(username);
						}

					} else
						tellEveryone(username + ": " + message);

				}

			} catch (Exception ex) {
				ServerTextArea.append("Lost connection. \n");
				ex.printStackTrace();
				clientOutputStreams.remove(client);
			}
		}
	}

	public ServerGUI() {
		initComponents();
	}

	private void initComponents() {

		jScrollPane1 = new JScrollPane();
		ServerTextArea = new JTextArea();
		startButton = new JButton();
		stopButton = new JButton();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("House Server");

		ServerTextArea.setColumns(20);
		ServerTextArea.setEditable(false);
		ServerTextArea.setLineWrap(true);
		ServerTextArea.setRows(5);
		ServerTextArea.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		jScrollPane1.setViewportView(ServerTextArea);

		startButton.setText("Start");
		startButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startButtonActionPerformed();
			}
		});

		stopButton.setText("Stop");
		stopButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				stopButtonActionPerformed();
			}
		});

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup()
								.addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 179,
										javax.swing.GroupLayout.PREFERRED_SIZE)
								.addGap(18, 18, 18)
								.addComponent(stopButton, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
				.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
				.addGroup(layout.createSequentialGroup().addContainerGap()
						.addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 229,
								javax.swing.GroupLayout.PREFERRED_SIZE)
						.addGap(18, 18, 18)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
								.addComponent(startButton).addComponent(stopButton))
						.addContainerGap(19, Short.MAX_VALUE)));

		pack();
	}

	public void startButtonActionPerformed() {
		Thread starter = new Thread(new ServerStart());
		starter.start();

		ServerTextArea.append("Server started. \n");

	}

	private void stopButtonActionPerformed() {
		tellEveryone("Server is stopping and all users will be disconnected.\n");
		ServerTextArea.append("Server stopping... \n");

	}

	public void runServerWithGUI() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				new ServerGUI().setVisible(true);
			}
		});
	}

	public class ServerStart implements Runnable {

		public void run() {
			clientOutputStreams = new ArrayList<PrintWriter>();
			onlineUsers = new ArrayList<String>();

			try {
				ServerSocket serverSock = new ServerSocket(port);

				while (true) {
					Socket clientSock = serverSock.accept();
					PrintWriter writer = new PrintWriter(clientSock.getOutputStream());
					clientOutputStreams.add(writer);

					Thread listener = new Thread(new ClientHandler(clientSock, writer));
					listener.start();
					ServerTextArea.append("Got a connection. \n");
				}
			} catch (Exception ex) {
				ServerTextArea.append("Error making a connection. \n");
			}

		}
	}

	public void addUser(String data) {
		String name = ClientGUI.username;
		ServerTextArea.append("Before " + name + " added. \n");

		onlineUsers.add(name);
		ServerTextArea.append("After " + name + " added. \n");

		tellEveryone(name + " is online.\n");
	}

	public void tellEveryone(String message) {

		Iterator<PrintWriter> it = clientOutputStreams.iterator();

		while (it.hasNext()) {
			try {
				PrintWriter writer = (PrintWriter) it.next();
				writer.println(message);
				ServerTextArea.append("Sending: " + message + "\n");
				writer.flush();
				ServerTextArea.setCaretPosition(ServerTextArea.getDocument().getLength());

			} catch (Exception ex) {
				ServerTextArea.append("Error telling everyone. \n");
			}
		}
	}
}
