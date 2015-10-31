
/**
 * @author Mark Mitchell
 * Manages Inventory.
 */
public class ThneedStore
{
  public enum BUY_SELL{BUY, SELL}
  private int thneeds;
  private float dollarBalance = 1000.00f;
  private long startTime;
  public static ThneedStore tS;

  /**
   * Constructor gets the start time for when it is instantiated
   * and it sets up a static instance of the class.
   */
  public ThneedStore(){
    tS = this;
    startTime = System.currentTimeMillis();
  }

  /**
   * Buys and sells are both handled by this synchronized method.
   * @param worker ServerWorker that is making the request
   * @param typeExchange Enumerator that is either a buy or a sell.
   * @param quantity Integer number of Thneeds to be bought or sold.
   * @param unitPrice Float for the price per Thneed.
   */
  synchronized public void buySell(ServerWorker worker, BUY_SELL typeExchange,
                                   int quantity, float unitPrice )
  {
    if(typeExchange == BUY_SELL.BUY && (unitPrice*quantity>dollarBalance)){
      worker.send("Error Not Enough Money in the Treasury!");
    }
    else if(typeExchange==BUY_SELL.SELL&&(quantity>thneeds)){
      worker.send("Error Not Enough Thneeds to Sell!");
    }
    else{
      if(typeExchange == BUY_SELL.SELL){
        thneeds-=quantity;
        dollarBalance+=quantity*unitPrice;
      }
      else{
        thneeds+=quantity;
        dollarBalance-=quantity*unitPrice;
      }
      String formattedString = String.format("%.02f", dollarBalance);
      System.out.println(fixTime() + ": inventory=" + thneeds + " : treasury=" + formattedString);
      ServerMaster.sM.broadcast(fixTime() + ": inventory=" + thneeds + " : treasury=" + formattedString);
    }
  }

  private String fixTime()
  {
    float elapsedTime = (System.currentTimeMillis()-startTime)/1000;
    String formattedStr = String.format("%.02f", elapsedTime);
    return formattedStr;
  }

}
