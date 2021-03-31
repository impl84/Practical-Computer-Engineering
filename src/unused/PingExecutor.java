
package unused;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PingExecutor
{
	public static void main(String[] args)
	{
		PingExecutor ping = new PingExecutor();
		try {
			ping.execute();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public PingExecutor()
	{
	}
	
	public void execute()
		throws	IOException
	{
		ProcessBuilder builder = new ProcessBuilder("ping", "-n","10", "www.google.com");
		Process process = builder.start();
			// IOException
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		
		String line = reader.readLine();
			// IOException
		while (line != null) {
			System.out.println(line);
			line = reader.readLine();
				// IOException
		}
		reader.close();
			// IOException
	}
}
