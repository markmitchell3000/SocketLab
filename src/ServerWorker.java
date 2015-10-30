import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerWorker extends Thread
{
  private Socket client;
  private PrintWriter clientWriter;
  private BufferedReader clientReader;

  public ServerWorker(Socket client)
  {
    this.client = client;

    try
    {
      //          PrintWriter(OutputStream out, boolean autoFlushOutputBuffer)
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
  
  //Called by ServerMaster
  public void send(String msg)
  {
    System.out.println("ServerWorker.send(" + msg + ")");
    clientWriter.println(msg);
  }

  private void readUserInput(String[] words){
    if(words.length==3)
    {
      int quantity;
      float price = 0.00f;
      try
      {
        quantity = Integer.parseInt(words[1]);
      }
      catch (NumberFormatException e){
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
          else{
            send("Third Argument not a valid price");
            return;
          }
        }
        else{
          send("Third Argument not a valid price");
          return;
        }
      }
      catch (NumberFormatException e){
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
      //remove client from linked list
      //return;
    }
    else if(!words[0].equals("inventory:"))
    {
      send("Invalid Arguments");
    }
  }

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
          } else if (words.length >= 1)
          {
            readUserInput(words);
          }
        }
      } catch (IOException e)
      {
        e.printStackTrace();
      }
    }
  }

}
