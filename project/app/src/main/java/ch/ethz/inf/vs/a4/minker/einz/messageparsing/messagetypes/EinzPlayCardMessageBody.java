package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import ch.ethz.inf.vs.a4.minker.einz.Card;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

public class EinzPlayCardMessageBody extends EinzMessageBody{

    /*
    {
      "header":{
        "messagegroup":"playcard",
        "messagetype":"PlayCard"
      },
      "body":{
        "dry-run":"true",
        "card":{
          "color":"green",
          "num":"1337"
        }
      }
    }
     */

    private String dryrun;
    private Card card;

    public String getDryrun() {
        return dryrun;
    }

    public Card getCard() {
        return card;
    }

}
