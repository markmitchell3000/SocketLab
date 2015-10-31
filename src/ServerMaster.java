import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;

/**
 * ServerMaster creates a ServerWorker for each client connections.
 * This class initializes the ThneedStore and broadcasts messages to
 * the ServerWorkers.  ServerWorkers are held in a linked list.
 */
public class ServerMaster
{
  private enum MODIFY_TYPE{CLEAR, ADD}
  private ServerSocket serverSocket;
  private LinkedList<ServerWorker> allConnections = new LinkedList<ServerWorker>();
  private LinkedList<ServerWorker> removeList = new LinkedList<ServerWorker>();
  public static ServerMaster sM;

  /**
   * Constructor assigns a port number to the ServerMaster, then attempts to
   * open a socket and wait for a connection.
   * @param portNumber Integer for the port used.
   */
  public ServerMaster(int portNumber)
  {
    sM = this;
    try
    {
      serverSocket = new ServerSocket(portNumber);
    }
    catch (IOException e)
    {
      System.err.println("Server error: Opening socket failed.");
      e.printStackTrace();
      System.exit(-1);
    }

    new ThneedStore();
    waitForConnection(portNumber);
  }

  /**
   * Listens to the port and waits for a connection, creates a ThreadWorker
   * to handle a client should they connect.  Then goes back to listening to the port.
   * @param port Integer for the port to listen to.
   */
  public void waitForConnection(int port)
  {
    String host = "";
    try
    {
      host = InetAddress.getLocalHost().getHostName();
    }
    catch (UnknownHostException e)
    {
      e.printStackTrace();
    }
    while (true)
    {
      System.out.println("ServerMaster("+host+"): waiting for Connection on port: "+port);
      try
      {
        Socket client = serverSocket.accept();
        ServerWorker worker = new ServerWorker(client);
        worker.start();
        System.out.println("ServerMaster: *********** new Connection");
        allConnections.add(worker);
        worker.send("ServerMaster says hello!");
      }
      catch (IOException e)
      {
        System.err.println("Server error: Failed to connect to client.");
        e.printStackTrace();
      }

    }
  }

  private void cleanConnectionList()
  {
    for(ServerWorker w: removeList)
    {
      allConnections.remove(w);
    }
    removeList = new LinkedList<ServerWorker>();
  }

  synchronized private void accessRemoveList(ServerWorker w, MODIFY_TYPE mt)
  {
    switch (mt)
    {
      case CLEAR:
        cleanConnectionList();
      case ADD:
        removeList.add(w);
    }
  }

  /**
   * When client disconnects the ServerWorker should be removed from the list.
   * This can occur even while a message is being broadcast because these are
   * added to a list of workers that need to be removed and will be by the next
   * broadcast.
   * @param w
   */
  public void removeWorker(ServerWorker w)
  {
    accessRemoveList(w, MODIFY_TYPE.ADD);
  }

  /**
   * Called by the ThneedStore to broadcast the latest inventory and
   * treasury balance.
   * @param s String for the message being broadcast
   */
  public void broadcast(String s)
  {
    int i=0;
    accessRemoveList(null, MODIFY_TYPE.CLEAR);
    for (ServerWorker workers : allConnections)
    {
      workers.send(s);
      System.out.println(i);
      i++;
    }
  }

  /**
   * Main can be run from the command line setting up a single instance of the server
   * and assigning it a port number.
   * @param args
   */
  public static void main(String args[])
  {
    //Valid port numbers are Port numbers are 1024 through 65535.
    //  ports under 1024 are reserved for system services http, ftp, etc.
    int port = 5555; //default
    if (args.length > 0)
    try
    {
      port = Integer.parseInt(args[0]);
      if (port < 1) throw new Exception();
    }
    catch (Exception e)
    {
      System.out.println("Usage: ServerMaster portNumber");
      System.exit(0);
    }
    
    new ServerMaster(port);
  }
}
