# JSON INTERFACE DOCUMENTATION

How the Einz Server and Client are supposed to communicate.

Work in progress, not yet approved by team lead. Do not implement yet.

***

The IP of the sender should never be needed, as the TCP connection already provides this.

The username of the client will be stored serverside for convenience, no need to retransmit every time.

Every String is case-sensitive!

***

**STILL TO SPECIFY** : [`card`](#drawxcards)  , [`style`](#showmessage)  , [`state`](#getstate) 
**TODO** 

* Change all booleans to strings to provide extensibility.

* Possibly change the messages such that the request and response have the same messagetypes. Possibly not. I'd rather leave the *messagetypes* the same and use some identifier *messagegroup* that is the same for messages that belong conceptually together. This has advantages in that changes would only have to be done in one class in the code.

* message set-up so that they all have the same interface on the top-level. This has the advantage that we only need the *header* for finding out which parserobject to return and only the body for within the action.

## General Form of Message

The message consists of a *header* and a *body*, providing a consistent top-level interface over all kinds of messages. The `messagegroup`  is the same for messages that conceptually belong together. This simplifies the implementation of maintainable code because only one implementation of the Parser would have to be changed if we change the communication.
If the body is changed, we only have to modify the Parser in the code. If the header is changed, we only have to modify the ParserFactory.

All Strings and Booleans should be stored as String to allow further extensibility and should be handled like Strings in the code. Same for Int.

The header must always contain `messagegroup` and `messagetype`. The body may vary.
`messagegroup` is camelCase, `messagetype` is PascalCase, to make it easier to visually distinguish them.

  ```json
  {
    "header":{
      "messagegroup":"groupname",
      "messagetype":"typename"
    },
    "body":{
      "message-specifics":"some yeys",
      "more":["some", "array"]
    }
  }
  ```

## Table of Contents

**specified messagegroups**

* ping

  > [Ping](#ping / pong), Pong


* registration

  > [Register](#register), RegisterResponse, [UnregisterRequest](#unregister), UnregisterResponse


* rules

  > [SendRules](#sendrules), [RulesInitialized](#sendrules)


* startGame

  > [StartGame](#startgame)


* draw

  > [DrawXCards](#drawxcards), [HandOutCard](#handoutcard), ([DrawCard](#drawcard))


* stateinfo

  > [WhoseTurn](#whoseturn), [GetState](#getstate), GetStateResponse


* playcard

  > [PlayCard](#playcard), PlayCardResponse, [CardPlayed](#cardplayed), [InvalidCardPlayed](#invalidcardplayed)


* toast

  > [ShowMessage](#showmessage), [SendMessage](#sendmessage), [BroadcastMessage](#broadcastmessage)


* playerFinished

  > [PlayerFinished](#playerfinished)


* endGame

  > [GameOver](#gameover)
## Ping / Pong

Request a simple answer from the other to make sure they're still there or to measure the delay.
`ping` expects an answer, while `pong` is the answer.

The **Server & Client** should both support sending both message types

##### Request

```JSON
{
  "header":{
    "messagegroup":"ping",
    "messagetype":"Ping"
  },
  "body":{
    
  }
}
```

##### Response

```JSON
{
  "header":{
    "messagegroup":"ping",
    "messagetype":"Pong"
  },
  "body":{
    
  }
}
```



## Register

Request to play on this server, or to spectate.
`username` : *String* 

> should be unique. If it is not, `success` will be *false* and `reason` will be *"not unique"*

`success` : *String* 

> Can be *"true"*, *"false"* (or later be further extended. E.g. *banned* )
> *"true"* means the client is allowed to stay on this server

`role` : *String*

> *"player"* or *"spectator"* 

The **Client** sends this and Server only reacts to it

##### Request

```JSON
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"Register"
  },
  "body":{
    "username":"roger",
    "role":"player"
  }
}
```

##### Response

With our current goals, this should always return *"true"* and is thus like an ACK. Also return which role was assigned in case the client requested both for some weird reason. If the client requests the same role multiple times, `success` will still be true if the client is still registered as this role and false if it has a different role or is unregistered.

If the client is not registered, `role` should have a return value of *"null"*.

`reason` : *String* 

> if not `success` , can be one of the following options
>
> + *"not unique"* if the same username was already registered by a different IP
> + "already registered" if the same IP already has registered a username
> + *"empty"* if the username is the empty string. This is reserved for the server

```JSON
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"RegisterResponse"
  },
  "body":{
    "success":"true",
    "role":"spectator",
    "reason":"this can be anything if success was true"
  }
}
```

## Unregister

The **Client** requests to leave the game and close the connection.

##### Request

```Json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UnregisterRequest"
  },
  "body":{
    "username":"roger"
  }
}
```

We don't need to wait for the Server to respond. The Server will probably handle these leaves similarly to connection timeouts.

However, the **Server** might decide to kick a player. For these purposes, the Response exists.
Also, the other Clients need to know about leaves.

##### Response

`kicked` : *String*

> whether the client was kicked or asked to leave. *"false"* in the latter case, *"true"* if kicked.

```Json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UnregisterResponse"
  },
  "body":{
    "username":"that random dude who we didn't want",
    "kicked":"true"
  }
}

```

## <a name="sendrules"></a>SendRules / RulesInitialized

Informs the Client which rules will be used. The rules themselves might have to be implemented client-side as well.
`ruleset` is a (not specifically sorted) JSONObject of rules. Every rule contains a `rulename` JSONObject and further details specific to the rule.

The **Server** sends this to the Client. The Client responds with the response once it's ready.

##### Request

```Json
{
  "header":{
    "messagegroup":"rules",
    "messagetype":"SendRules"
  },
  "body":{
    "ruleset":{
        "startWithXCards":{
          "x":"7"
        },
       "instantWinOnCardXPlayed":{
          "cardcolor":"green",
          "cardnum":"3"
       }
    }
  }
}
```

Note that the example rules here were spontaneously written and might not be specified.

##### Response

Once a client has initialized based on the ruleset, it should inform the server that it's ready.
If rules were unknown or not completely specified, they are specified for debug purposes, though the server should never use rules that the clients don't know.
If everything is as it's supposed to be, this is simply a signal for the server that this client is ready to receive [**StartGame**](#startgame).
`invalid rules` : *JSONArray*

> Unsorted list of which rules were invalid or unknown

```Json
{
  "header":{
    "messagegroup":"rules",
    "messagetype":"RulesInitialized"
  },
  "body":{
    "invalid rules":["instantWinOnCardXPlayed", "';DROP DATABASE usernames"] 
  }
}
```

## StartGame

Inform the clients that the game is about to start and they are allowed to request cards. This will be sent after [**sendRules**](#sendrules).

The **Server** sends this.

##### Request

```json
{
  "header":{
    "messagegroup":"startGame",
    "messagetype":"StartGame"
  },
  "body":{
    
  }
}
```

##### Response

No response from the clients is needed, as their next step will be to demand cards.

## DrawXCards

Request *x* new cards.
`X` : *int*

The **Client** sends this request. The Server checks whether the Client is allowed to draw this many cards and hands back the appropriate amount of cards later using [**HandOutCard**](#handoutcard) `x` times . The response contains how many these will be.
`will give` : *int*, can be 0

##### Request

```json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"DrawXCards"
  },
  "body":{
    "x":"3"
  }
}
```

##### Response

```Json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"DrawXCardsResponse"
  },
  "body":{
    "will give":"2"
  }
}
```

## DrawCard

Just use  [**DrawXCards**](#drawxcards) with `x="1"`

## WhoseTurn

The **Server** informs each Client whenever somebodys' turn starts. The Client can thus find out whether it's its turn.

##### Request

```Json
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"WhoseTurn"
  },
  "body":{
    "username":"Donald Trump"
  }
}
```

## PlayCard

The **Client** can request to play a card. If `dry-run` is false, the Server will play the card if it is valid and otherwise return a failure message. So the client should wait to handle the response before doing anything else.
If `dry-run` is true, the Server will only answer whether the move is valid but not play the card.

`card` : *JSONObject*

> The specifics of the card are not yet defined (10.11.2017)
> If the card is invalid, the Server should answer with [**InvalidCardPlayed**](#invalidcardplayed).

##### Request

```Json
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
```

Note that the contents of card are yet to be specified as of 11.11.2017.

##### Response

As always the booleans are also stored as strings to allow easier extensibility. When coding, you have to make sure anyways that the input is of the correct type.

`play valid` : *boolean*

> true if the card can be played. If `dry-run` is *false*, then this means the card will be played and the server will soon send [**CardPlayed**](#cardplayed) to all clients.

`dry-run` : *boolean*

> the same value as the received `dry-run`

`your turn` : *boolean*

> Whether the play is not only valid but it's also the clients turn

```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlaycardResponse"
  },
  "body":{
    "dry-run":"true",
    "play valid":"true",
    "your turn":"true"
  }
}
```

## CardPlayed

Usually broadcasted from the **Server** to all Clients if a valid play was made.

`card` : *JSONObject* 

> To be specified

`player` : *String* 

> Who played the card

```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"CardPlayed"
  },
  "body":{
    "card":{
      "some info":"to be specified",
      "color":"green"
    }
    "player":"владимир путин"
  }
}
```

## InvalidCardPlayed

Response from the **Server** if a [**PlayCard**](#playcard) request was invalid.
`reason` : *String* 

> Not yet defined, for debugging purposes.

```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"InvalidCardPlayed"
  },
  "body":{
    "reason":"bad coding skills and an inexistent card"
  }
}
```

## GetState

The **Client** requests to know its own canonical state. This involves their current hand and the already played cards, also called the stack.

##### Request

```Json
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"GetState"
  },
  "body":{
    
  }
}
```

##### Response

`hand` : Array of *JSONObjects*

> A List of Cards that the Client should have in their hand.

`stack` : Array of *JSONObjects*

> A List of the already played cards, the card at index 0 being the oldest card on the stack

```Json
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"GetStateResponse"
  },
  "body":{
    "hand":[
      {"color":"blue", "value":"7"},
      {"color":"black", "special":"play Vitas"}
    ],
    "stack":[
      {"color":"blue", "num":"3"},
      {"color":"blue", "num":"4"},
      {"color":"red", "num":"4"}
    ]
  }
}
```

## HandOutCard

The **Server** hands out 1 card to a client.

`card` : *JSONObject* 

> The card that is handed to the client

```Json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"HandOutCard"
  },
  "body":{
    "card":{
      "color":"green",
      "num":"twelve"
    }
  }
}
```

Note that the format of the card is yet to be defined (10.11.2017)

## ShowMessage

Send some `message` that should be displayed to the clientside user.

`style` : *JSONObject* 

> The client might ignore this. If the UI supports multiple textcolors or background colors, this will specify a style

`from`: *"String"*

> From which user it is. empty string if it is from the server

```json
{
  "header":{
    "messagegroup":"toast",
    "messagetype":"ShowMessage"
  },
  "body":{
    "messagetype":"ShowMessage",
    "message":"сука блиать",
    "style":{"some":"JSONOBJECT"}
  }
}
```

## SendMessage

The **Client** requests the server to send [**ShowMessage**](#showmessage) to `target` username

```Json
{
  "header":{
    "messagegroup":"toast",
    "messagetype":"SendMessage"
  },
  "body":{
    "messagetype":"SendMessage",
    "target":"roger",
    "message":"my message"
  }
}
```

## BroadcastMessage

The **Client** requests the server to send [**ShowMessage**](#showmessage) to all clients, including itself.

```json
{
  "header":{
    "messagegroup":"toast",
    "messagetype":"BroadcastMessage"
  },
  "body":{
    "message":"my message"
  }
}
```

## PlayerFinished

The **Server** informs the clients that one Player has finished the game. E.g. by having played all cards. Optionally, depending on rules, there is `more` information included or `points`. If not, they should be *null*.

```Json
{
  "header":{
    "messagegroup":"playerFinished",
    "messagetype":"PlayerFinished"
  },
  "body":{
    "username":"roger",
    "points":"1",
    "more":{
      "a":"b"
    }
  }
}
```

## GameOver

The **Server** informs the clients that the game is over and they can show the after-game UI. E.g. displaying points.

```Json
{
  "header":{
    "messagegroup":"endGame",
    "messagetype":"GameOver"
  }
  "body":{}
}
```
