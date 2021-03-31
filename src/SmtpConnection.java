
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class SmtpConnection
{
	private static final int SMTP_PORT = 25;
	
	private static final String MAIL_HOST	= "umail.iwate-pu.ac.jp";
	private static final String MAIL_FROM	= "hashi@iwate-pu.ac.jp";
	private static final String RCPT_TO		= "p031s015@s.iwate-pu.ac.jp";
	private static final String[] DATA_LINES		= {
		"To: " + RCPT_TO,
		"Subject: ...",
		"",
		"...",
	};
	
	public static void main(String args[])
	{
		SmtpConnection smtpConnection = null;
		try {
			smtpConnection = new SmtpConnection(MAIL_HOST);
			smtpConnection.sendMail(MAIL_FROM, RCPT_TO, DATA_LINES);
		}
		catch (UnknownHostException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		finally {
			try {
				smtpConnection.close();
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private Socket smtpSocket = null;
	private BufferedReader in = null;
	private PrintWriter out = null;
	
	SmtpConnection(String mailHost)
		throws	UnknownHostException,
				IOException
	{
		this.smtpSocket = new Socket(
			InetAddress.getByName(mailHost),
			SMTP_PORT
		);
		this.in = new BufferedReader(
			new InputStreamReader(
				this.smtpSocket.getInputStream()
			)
		);
		boolean autoFlush = true;
		this.out = new PrintWriter(
			new OutputStreamWriter(
				this.smtpSocket.getOutputStream()
			),
			autoFlush
		);
	}
	
	void sendMail(String from, String to, String[] dataLines)
		throws	IOException
	{
		String receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		String localHostName = InetAddress.getLocalHost().getHostName();
		System.out.println("HELO " + localHostName);
		this.out.println("HELO " + localHostName);
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		System.out.println("MAIL From:<" + from + ">");
		this.out.println("MAIL From:<" + from + ">");
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		System.out.println("RCPT TO:<" + to + ">");
		this.out.println("RCPT TO:<" + to + ">");
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		System.out.println("DATA");
		this.out.println("DATA");
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		for (String line: dataLines) {
			System.out.println(line);
			this.out.println(line);
		}
		
		System.out.println(".");
		this.out.println(".");
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
		
		System.out.println("QUIT");
		this.out.println("QUIT");
		receivedMessage = this.in.readLine();
		System.out.println(receivedMessage);
	}
	
	void close()
		throws	IOException
	{
		try {
			if (this.smtpSocket != null) {
				this.smtpSocket.close();
			}
			if (this.in != null) {
				this.in.close();
			}
			if (this.out != null) {
				this.out.close();
			}
		}
		finally {
			this.smtpSocket = null;
			this.in = null;
			this.out = null;
		}
	}
}
