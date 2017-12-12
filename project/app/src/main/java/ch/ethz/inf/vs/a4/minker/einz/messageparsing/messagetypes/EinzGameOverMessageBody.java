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

    /**
     * This is a convenience constructor.
     * @param ignoreThisBoolean ignore this, it is only there to allow multiple constructors taking a hashmap
     */
    public EinzGameOverMessageBody (HashMap<String, Integer> points, boolean ignoreThisBoolean) {
        HashMap<String, String> myPoints = new HashMap<>();
        for(String k : points.keySet()){
            myPoints.put(k, String.valueOf(points.get(k)));
        }
        this.points = myPoints;
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
		"messagegroup": "endgame",
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
