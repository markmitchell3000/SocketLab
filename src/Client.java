import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * @author Mark Mitchell
 * Handles user orders for buying and selling thneeds, gets its information
 * about the inventory from broadcasts from the ServerMaster that
 * are then sent from the ServerWorkers.
 */
public class Client
{
  private Socket clientSocket;
  private PrintWriter write;
  private BufferedReader reader;
  private long startNanoSec;
  private Scanner keyboard;
  private ClientListener listener;

  private volatile int sneedsInStore;
  private volatile float treasury=0.00f;
  private boolean clientRunning = true;

  /**
   * Constructor for the client connects to a Server worker and initializes listeners.
   * @param host String for the hosts address - 127.0.0.1 if on the same machine
   * @param portNumber Integer for the port the client connects to.
   */
  public Client(String host, int portNumber)
  {
    startNanoSec = System.nanoTime();
    System.out.println("Starting Client: " + timeDiff());

    keyboard = new Scanner(System.in);

    while (!openConnection(host, portNumber))
    {
    }
    
    listener = new ClientListener();
    System.out.println("Client(): Starting listener = : " + listener);
    listener.start();

    listenToUserRequests();

    closeAll();

  }


  private boolean openConnection(String host, int portNumber)
  {

    try
    {
      clientSocket = new Socket(host, portNumber);
    }
    catch (UnknownHostException e)
    {
      System.err.println("Client Error: Unknown Host " + host);
      e.printStackTrace();
      return false;
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open connection to " + host
          + " on port " + portNumber);
      e.printStackTrace();
      return false;
    }

    try
    {
      write = new PrintWriter(clientSocket.getOutputStream(), true);
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open output stream");
      e.printStackTrace();
      return false;
    }
    try
    {
      reader = new BufferedReader(new InputStreamReader(
          clientSocket.getInputStream()));
    }
    catch (IOException e)
    {
      System.err.println("Client Error: Could not open input stream");
      e.printStackTrace();
      return false;
    }
    return true;

  }

  private void listenToUserRequests()
  {
    while (true)
    {
      System.out.println("Sneeds in Inventory = " + sneedsInStore);
      System.out.println("Enter Command (Buy: # | Sell: #):");
      String cmd = keyboard.nextLine();
      if (cmd == null) continue;
      if (cmd.length() < 1) continue;
      String[] words = cmd.split(" ");
      //char c = cmd.charAt(0);
      //if (c == 'q')break;
      String s = words[0];
      if(s.equals("quit:")){
        //closeAll();
        clientRunning = false;
        return;
      }
      else if(s.equals("inventory:")){
        String formattedString = String.format("%.02f", treasury);
        System.out.println("Number of Thneeds: "+sneedsInStore+" Treasury: "+formattedString);
      }

      write.println(cmd);
    }
  }

  /**
   * Closes the socket connection to the server.
   */
  public void closeAll()
  {
    System.out.println("Client.closeAll()");

    if (write != null) write.close();
    if (reader != null)
    {
      try
      {
        reader.close();
        clientSocket.close();
      }
      catch (IOException e)
      {
        System.err.println("Client Error: Could not close");
        e.printStackTrace();
      }
    }

  }

  private String timeDiff()
  {
    long namoSecDiff = System.nanoTime() - startNanoSec;
    double secDiff = (double) namoSecDiff / 1000000000.0;
    return String.format("%.6f", secDiff);

  }

  /**
   * Main for client runs from the client side and connects to a running server.
   * @param args Command line args should be a host address and a port.
   */
  public static void main(String[] args)
  {
    
    String host = null;
    int port = 0;
   
    try
    {
      host = args[0];
      port = Integer.parseInt(args[1]);
      if (port < 1) throw new Exception();
    }
    catch (Exception e)
    {
      System.out.println("Usage: Client hostname portNumber");
      System.exit(0);
    }
    new Client(host, port);

  }

  /**
   * Nested class set up to listen for messages sent from the ServerWorker.
   */
  class ClientListener extends Thread
  {
    /**
     * Thread that reads any messages from the ServerWorker
     */
    public void run()
    {
      System.out.println("ClientListener.run()");
      while (clientRunning)
      {
        read();
      }

    }

    private void read()
    {
      try
      {
        System.out.println("Client: listening to socket");
        String msg = reader.readLine();
        String[] s = msg.split(" ");
        if (msg.startsWith("Sneeds:"))
        {
          int idxOfNum = msg.indexOf(':') + 1;
          int n = Integer.parseInt(msg.substring(idxOfNum));
          sneedsInStore = n;
          System.out.println("Current Inventory of Sneeds (" + timeDiff()
              + ") = " + sneedsInStore);
        }
        else if (msg.startsWith("You just bought "))
        {
          System.out.println("Success: " + msg);
        }
        else if (msg.startsWith("Error"))
        {
          System.out.println("Failed: " + msg);
        }
        else if(msg.equals("Second Argument not an integer")
                ||msg.equals("Third Argument not a valid price")
                ||msg.equals("Invalid Arguments")
                ||msg.equals("Too many arguments!"))
        {
          System.out.println(msg);
        }
        else if(s[1].startsWith("inventory=")){
          String[] s1 = s[1].split("=");
          sneedsInStore = Integer.parseInt(s1[1]);
          String[] s2 = s[3].split("=");
          treasury = Float.parseFloat(s2[1]);
          System.out.println("Updated inventory and treasury");
          System.out.println("Sneeds in Inventory = " + sneedsInStore);
          String formattedString = String.format("%.02f", treasury);
          System.out.println("Treasury balance: $"+formattedString);
        }
        else
        {
          System.out.println("Unrecognized message from Server(" + timeDiff()
              + ") = " + msg);
        }

      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }

  }

}
