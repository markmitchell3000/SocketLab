/**
 * @author Mark Mitchell
 * Manages Inventory.
 */
public class ThneedStore
{
  public enum BUY_SELL{BUY, SELL}
  private int thneeds;
  private float dollarBalance = 1000.0f;
  private float time;

  public ThneedStore(){

  }

  synchronized public void buySell(ServerWorker worker, BUY_SELL typeExchange,
                                   int quantity, float unitPrice ){
    if((typeExchange == BUY_SELL.BUY && (unitPrice*quantity>dollarBalance))
            ||(typeExchange==BUY_SELL.SELL&&(quantity>thneeds))){
      //broadcast to worker that request failed
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
      ServerMaster.sM.broadcast(time + ": inventory=" + thneeds + " : treasury=" + dollarBalance);
    }
  }

  private void fixTime(){
    double blah = System.currentTimeMillis();
  }
}
