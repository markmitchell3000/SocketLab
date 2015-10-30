import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
  private Socket clientSocket;
  private PrintWriter write;
  private BufferedReader reader;
  private long startNanoSec;
  private Scanner keyboard;
  private ClientListener listener;

  private volatile int sneedsInStore;
  private volatile float treasury;
  private boolean clientRunning = true;

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

      char c = cmd.charAt(0);
      if (c == 'q') {
        clientRunning = false;
        return;
      }
      String[] words = cmd.split(" ");
      if(words.length>3){
        System.out.println("Too many arguments!");
      }
      else if(words.length>=1)
      {
        readUserInput(words);
      }

      write.println(cmd);
    }
  }

  private void readUserInput(String[] words){
    if(words[0].equals("buy:"))
    {

    }
    else if(words[0].equals("sell:"))
    {

    }
    else if(words[0].equals("quit:"))
    {
      clientRunning = false;
      return;
    }
    else if(words[0].equals("inventory:"))
    {
      System.out.println("Number of Thneeds: "+sneedsInStore+" Treasury: "+treasury);
    }
    else
    {
      System.out.println("Invalid Arguments");
    }
  }

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

  
  
  
  class ClientListener extends Thread
  {
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
        if (msg.startsWith("Sneeds:"))
        {
          int idxOfNum = msg.indexOf(':') + 1;
          int n = Integer.parseInt(msg.substring(idxOfNum));
          sneedsInStore = n;
          System.out.println("Current Invintory of Sneeds (" + timeDiff()
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
