import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Mark Mitchell
 * ServerWorker sends and receives messages from the Client.
 * Buy and sell orders will be relayed to the ThneedStore.
 */
public class ServerWorker extends Thread
{
  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;

  /**
   * Constructor takes a socket and sets up the a PrintWriter and a BufferedReader.
   * @param client Socket for the newly connected client.
   */
  public ServerWorker(Socket client)
  {
    this.client = client;

    try
    {
      clientWriter = new PrintWriter(client.getOutputStream(), true);
    }
    catch (IOException e)
    {
      System.err.println("Server Worker: Could not open output stream");
      e.printStackTrace();
    }
    try
    {
      clientReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
      
    }
    catch (IOException e)
    {
      System.err.println("Server Worker: Could not open input stream");
      e.printStackTrace();
    }
  }

  /**
   * ServerMaster will use this to broadcast Inventory updates.
   * ThneedStore will use this to tell a client that their order
   * failed.
   * @param msg  String message to be broadcast.
   */
  public void send(String msg)
  {
    System.out.println("ServerWorker.send(" + msg + ")");
    clientWriter.println(msg);
  }

  private void readUserInput(String[] words)
  {
    if(words.length==3)
    {
      int quantity;
      float price = 0.00f;
      try
      {
        quantity = Integer.parseInt(words[1]);
      }
      catch (NumberFormatException e)
      {
        send("Second Argument not an integer");
        return;
      }
      try
      {
        int numLength = words[2].length();

        if(numLength>3)
        {
          if (words[2].charAt(words[2].length() - 3) == '.')
          {
            price = Float.parseFloat(words[2]);
          }
          else
          {
            send("Third Argument not a valid price");
            return;
          }
        }
        else
        {
          send("Third Argument not a valid price");
          return;
        }
      }
      catch (NumberFormatException e)
      {
        send("Third Argument not a valid price");
        return;
      }
      if (words[0].equals("buy:"))
      {
        ThneedStore.tS.buySell(this, ThneedStore.BUY_SELL.BUY, quantity, price);
      }
      else if (words[0].equals("sell:"))
      {
        ThneedStore.tS.buySell(this, ThneedStore.BUY_SELL.SELL, quantity, price);
      }
      else{
        send("Invalid Arguments");
      }
    }
    else if(words[0].equals("quit:"))
    {
      System.out.println("firing");
      ServerMaster.sM.removeWorker(this);
      return;
    }
    else if(!words[0].equals("inventory:"))
    {
      send("Invalid Arguments");
    }
  }

  /**
   * Needed for the thread that will listen to requests from the client.
   */
  public void run()
  {
    while(true)
    {
      try
      {
        String s = clientReader.readLine();
        if(s!=null)
        {
          String[] words = s.split(" ");
          if (words.length > 3)
          {
            send("Too many arguments!");
          }
          else if (words.length >= 1)
          {
            readUserInput(words);
          }
        }
        else
        {
          ServerMaster.sM.removeWorker(this);
          return;
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

}
