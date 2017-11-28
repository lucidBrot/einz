package ch.ethz.inf.vs.a4.minker.einz;

import android.util.Log;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.InvalidResourceFormatException;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParser;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzParserFactory;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzPlayCardResponseMessageBody;
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
                        "      \"messagegroup\": \"draw\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzDrawParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "      \"messagegroup\": \"stateinfo\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzStateInfoParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "      \"messagegroup\": \"playcard\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzPlayCardParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "      \"messagegroup\": \"furtheractions\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzFurtherActionsParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "      \"messagegroup\": \"toast\",\n" +
                        "      \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzToastParser\"\n" +
                        "    },\n" +
                        "\t{\n" +
                        "\t  \"messagegroup\": \"endGame\",\n" +
                        "\t  \"mapstoparser\": \"class ch.ethz.inf.vs.a4.minker.einz.messageparsing.parsertypes.EinzEndGameParser\"\n" +
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
    public void parseRegister() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"Register\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"roger\",\n" +
                "    \"role\":\"player\",\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseRegisterSuccess() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"RegisterSuccess\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"roger\",\n" +
                "    \"role\":\"spectator\",\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseUpdateLobbyList() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        /*String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"UpdateLobbyList\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"lobbylist\":[\n" +
                "      {\"roger\":\"player\"},\n" +
                "      {\"chris\":\"player\"},\n" +
                "      {\"table\":\"spectator\"}\n" +
                "    ],\n" +
                "    \"admin\":\"roger\"\n" +
                "  }\n" +
                "}";
                */
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"UpdateLobbyList\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"lobbylist\":[\n" +
                "      {\"username\":\"chris\", \"role\":\"player\"},\n" +
                "      {\"username\":\"roger\", \"role\":\"player\"},\n" +
                "      {\"username\":\"table\", \"role\":\"spectator\"}\n" +
                "    ],\n" +
                "    \"admin\":\"roger\"\n" +
                "  }" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseRegisterFailure() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"RegisterFailure\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"role\":\"player\",\n" +
                "    \"username\":\"server\",\n" +
                "    \"reason\":\"invalid\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseUnregisterRequest() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"UnregisterRequest\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"roger\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseUnregisterResponse() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"UnregisterResponse\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"that random dude who we didn't want\",\n" +
                "    \"reason\":\"kicked\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseKick() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"Kick\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"that random dude who we didn't want\",\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parseKickFailure() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"registration\",\n" +
                "    \"messagetype\":\"KickFailure\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"the user that you wanted to kick\",\n" +
                "    \"reason\":\"why you failed\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseSpecifyRules() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"startgame\",\n" +
                "    \"messagetype\":\"SpecifyRules\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"ruleset\":{\n" +
                "        \"startWithXCards\":{\n" +
                "          \"x\":\"7\"\n" +
                "        },\n" +
                "       \"instantWinOnCardXPlayed\":{\n" +
                "          \"cardcolor\":\"green\",\n" +
                "          \"cardnum\":\"3\"\n" +
                "       },\n" +
                "      \"exodia\":{},\n" +
                "      \"handicap\":{\"arr\":[{\"chris\":\"100\"},{\"roger\":\"-10\"}]}\n" +
                "\t}\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseStartGame() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"startgame\",\n" +
                "    \"messagetype\":\"StartGame\"\n" +
                "  },\n" +
                "\t\"body\":{}\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseInitGame() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"startgame\",\n" +
                "    \"messagetype\":\"InitGame\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"ruleset\":{\n" +
                "        \"startWithXCards\":{\n" +
                "          \"x\":\"7\"\n" +
                "        },\n" +
                "       \"instantWinOnCardXPlayed\":{\n" +
                "          \"cardcolor\":\"green\",\n" +
                "          \"cardnum\":\"3\"\n" +
                "       },\n" +
                "      \"exodia\":{},\n" +
                "      \"handicap\":{\"arr\":[{\"chris\":\"100\"},{\"roger\":\"-10\"}]}\n" +
                "\t},\n" +
                "    \"turn-order\":[\n" +
                "      \"sisisilvia\",\n" +
                "      \"faeglas\",\n" +
                "      \"baclemen\"\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseDrawCards() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"draw\",\n" +
                "    \"messagetype\":\"DrawCards\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }
    @Test
    public void parseDrawCardsSuccess() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"draw\",\n" +
                "    \"messagetype\":\"DrawCardsSuccess\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"cards\":[\n" +
                "      {\"ID\":\"cardID1\",\"origin\":\"talon\"},\n" +
                "      {\"ID\":\"cardID3\",\"origin\":\"talon\"},\n" +
                "      {\"ID\":\"cardID1\",\"origin\":\"talon\"}\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        parser_test(msg); // #cardtag this test still fails.
    }

    @Test
    public void parseDrawCardsFailure() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"draw\",\n" +
                "    \"messagetype\":\"DrawCardsFailure\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"reason\":\"it's nothing. I'm fine bae.\"" +
                "  }\n" +
                "}";
        parser_test(msg);
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


    @Test
    public void parseSendState() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"stateinfo\",\n" +
                "    \"messagetype\":\"SendState\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"globalstate\":{\n" +
                "      \"numcardsinhand\":{\n" +
                "        \"Eric\":\"3\",\n" +
                "        \"Rip\":\"100\",\n" +
                "        \"Ric\":\"2\"\n" +
                "      },\n" +
                "      \"stack\":[\n" +
                "        {\"ID\":\"cardID01\", \"origin\":\"~talon\"},\n" +
                "        {\"ID\":\"cardID1337\", \"origin\":\"Rip\"}\n" +
                "      ],\n" +
                "      \"whoseturn\":\"Ric\",\n" +
                "      \"drawxcardsmin\":\"2\"\n" +
                "    },\n" +
                "    \"playerstate\":{\n" +
                "      \"hand\":[\n" +
                "        {\"ID\":\"cardID03\", \"origin\":\"Eric\"}\n" +
                "      ],\n" +
                "      \"possibleactions\":\n" +
                "        [\n" +
                "        \"leaveGame\", \"drawCards\", \"playCard\"\n" +
                "        ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parsePlayCard() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"playcard\",\n" +
                "    \"messagetype\":\"PlayCard\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"card\":{\n" +
                "      \"ID\":\"cardID1337\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parsePlayCardResponse() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"playcard\",\n" +
                "    \"messagetype\":\"PlayCardResponse\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "\t\"success\":\"true\"\n" +
                "  }\n" +
                "}";
        //parser_test(msg);
    }

    @Test
    public void parsePlayCustomAction() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"furtheractions\",\n" +
                "    \"messagetype\":\"CustomAction\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"custom parameter of the rule\":\" a custom JSONObject\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parseCustomActionResponse() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"furtheractions\",\n" +
                "    \"messagetype\":\"CustomActionResponse\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"custom parameter of the rule\":\" a custom JSONObject\"\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parseFinishTurn() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"furtheractions\",\n" +
                "    \"messagetype\":\"FinishTurn\"\n" +
                "  },\n" +
                "  \"body\":{}\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parseShowToast() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"toast\",\n" +
                "    \"messagetype\":\"ShowToast\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"toast\":\"сука блиать\",\n" +
                "    \"from\":\"josua\",\n" +
                "    \"style\":{\"some\":\"JSONOBJECT\"}\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parsePlayerFinished() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "  \"header\":{\n" +
                "    \"messagegroup\":\"endGame\",\n" +
                "    \"messagetype\":\"PlayerFinished\"\n" +
                "  },\n" +
                "  \"body\":{\n" +
                "    \"username\":\"roger\",\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

    @Test
    public void parseGamerOver() throws JSONException, InvalidResourceFormatException, ClassNotFoundException {
        String msg = "{\n" +
                "\t\"header\": {\n" +
                "\t\t\"messagegroup\": \"endGame\",\n" +
                "\t\t\"messagetype\": \"GameOver\"\n" +
                "\t},\n" +
                "  \"body\": {\n" +
                "    \"points\":{\n" +
                "      \"roger\":\"17\",\n" +
                "      \"chris\":\"3\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        parser_test(msg);
    }

}