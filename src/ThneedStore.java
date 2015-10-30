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

  public ThneedStore(){
    tS = this;
    startTime = System.currentTimeMillis();
  }

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
      float time = fixTime();
      ServerMaster.sM.broadcast(time + ": inventory=" + thneeds + " : treasury=" + dollarBalance);
    }
  }

  private float fixTime(){
    long elapsedTime = System.currentTimeMillis()-startTime;
    return elapsedTime/1000;
  }
}
