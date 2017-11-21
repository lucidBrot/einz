package ch.ethz.inf.vs.a4.minker.einz.server;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

public class Debug {

    /**
     * For debug purposes only, should not have side effects at all.
     * Prints the class of the given object as JSON
     * @param o
     */
    public static void debug_printJSONRepresentationOf(Object o){
        //<debug>
        JSONObject container = new JSONObject();
        try {
            container.put("your Object:", o);
            Log.d("ESCH/DEBUG", "printJSONRepresentationOF() : "+ container.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // D/DEBUG: {"test":"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser"}
        //</debug>
    }
}
