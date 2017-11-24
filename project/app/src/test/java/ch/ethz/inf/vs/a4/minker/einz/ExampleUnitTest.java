package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParserFactory;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzStateInfoParser;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }
   @Test
    public void parseGetStateOld() throws Exception {
        JSONObject expectedJSON = new JSONObject("{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"stateinfo\",\n" +
                "    \"messagetype\":\"GetState\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \n" +
                "  }\n" +
                "}\n" +
                "\n");
        String expected = expectedJSON.toString();
        EinzStateInfoParser einzStateInfoParser = new EinzStateInfoParser();
        EinzMessage einzMessage = einzStateInfoParser.parse(expectedJSON);
        JSONObject actualJSON = einzMessage.toJSON();
        String actual = actualJSON.toString();
        System.out.println(expected);
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    public void parser_test(String msg) throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        EinzParserFactory einzParserFactory = new EinzParserFactory();

        JSONObject jsonObject = new JSONObject(
                "{\n" +
                        "  \"parsermappings\": [\n" +
                        "    {\n" +
                        "      \"messagegroup\": \"registration\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzRegistrationParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "      \"messagegroup\": \"startgame\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzStartGameParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "\t  \"messagegroup\": \"stateinfo\",\n" +
                        "\t  \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzStateInfoParser\"\n" +
                        "\t}\n" +
                        "  ]\n" +
                        "}"
        );
        einzParserFactory.loadMappingsFromJson(jsonObject);

        EinzParser einzParser = einzParserFactory.generateEinzParser(msg);
        JSONObject expectedJSON = new JSONObject(msg);
        String expected = expectedJSON.toString();
        EinzMessage einzMessage = einzParser.parse(expectedJSON);
        JSONObject actualJSON = einzMessage.toJSON();
        String actual = actualJSON.toString();
        System.out.println(expected);
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void parseGetState() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"stateinfo\",\n" +
                "    \"messagetype\":\"GetState\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \n" +
                "  }\n" +
                "}\n" +
                "\n";
        parser_test(msg);
    }
}