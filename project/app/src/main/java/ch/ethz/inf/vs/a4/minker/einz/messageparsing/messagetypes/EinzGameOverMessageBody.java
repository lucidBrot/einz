package ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;

/**
 * Created by silvia on 11/18/17.
 */

public class EinzGameOverMessageBody extends EinzMessageBody {

    private final HashMap<String, String> points;

    public EinzGameOverMessageBody(HashMap<String, String> points) {
        this.points = points;
    }

    public HashMap<String, String> getPoints() {
        return points;
    }


    @Override
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject pointsJSON = new JSONObject();
        HashMap<String, String> points = getPoints();
        for (Map.Entry<String, String> entry : points.entrySet()) {
            String name = entry.getKey();
            String number = entry.getValue();
            pointsJSON.put(name, number);
        }
        jsonObject.put("points", pointsJSON);
        return jsonObject;
    }
}
/*
{
	"header": {
		"messagegroup": "endGame",
		"messagetype": "GameOver"
	},
  "body": {
    "points":{
      "roger":"17",
      "chris":"3"
    }
  }
}
 */
