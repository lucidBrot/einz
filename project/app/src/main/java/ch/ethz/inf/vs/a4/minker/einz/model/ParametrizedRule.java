package ch.ethz.inf.vs.a4.minker.einz.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Josua on 11/30/17.
 */

public interface ParametrizedRule {

    /**
     * Initializes the Rule with the required parameters
     * @param parameter The parameter as JSONObject
     * @throws JSONException If the JSON does not contain the correct parameter
     */
    void setParameter(JSONObject parameter) throws JSONException;

    /**
     * Returns what arguments the rule is expecting. ParameterType can be TEXT or NUMBER. The Rule
     * defines a set of parameter it requires and defines if the input should be a NUMBER or a TEXT.
     * @return Mapping between a name and a input type.
     */
    Map<String, ParameterType> getParameterTypes();
    
}
