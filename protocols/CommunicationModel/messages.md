# JSON MESSAGING INTERFACE DOCUMENTATION

How the Einz Server and Client are supposed to communicate.

Work in progress, not yet approved by team lead. Do not implement yet.

See the handwritten notes pdf for a messy state diagram until somebody creates a beautiful one.

***

The IP of the sender should never be needed, as the TCP connection already provides this.

The username of the client will be stored serverside for convenience, no need to retransmit every time.

Every String is case-sensitive!

The first client to connect will be **admin**

***

**STILL TO SPECIFY** (IF EVER USED) : [`style`](#showtoast), [`rules`](#rules)

## General Form of Message

The message consists of a *header* and a *body*, providing a consistent top-level interface over all kinds of messages. The `messagegroup`  is the same for messages that conceptually belong together. This simplifies the implementation of maintainable code because only one implementation of the Parser would have to be changed if we change the communication.
If the body is changed, we only have to modify the Parser in the code. If the header is changed, we only have to modify the ParserFactory.

All Strings and Booleans should be stored as String to allow further extensibility and should be handled like Strings in the code. Same for Int.

The header must always contain `messagegroup` and `messagetype`. The body may vary.
`messagegroup` is camelCase, `messagetype` is PascalCase, to make it easier to visually distinguish them.

Within the program, messagegroup and messagetype might be null if the mapping was not registered.

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

* networking

  > [Ping](#ping--pong), Pong, [Keepalive](#keepalive)


* registration

  > [Register](#register), [RegisterSuccess](#registersuccess), [RegisterFailure](#registerfailure), [UpdateLobbyList](#updatelobbylist), [UnregisterRequest](#unregister), [UnregisterResponse](#unregisterresponse), [Kick](#kick), [KickFailure](#kickfailure) 


* startgame

  > [SpecifyRules](#specifyRules), [StartGame](#startgame), [InitGame](#initgame)


* draw

  > [DrawCards](#drawcards), [DrawCardsResponse](#drawcardsresponse)


* stateinfo

  > [SendState](#sendstate), [GetState](#getstate) 


* playcard

  > [PlayCard](#playcard), [PlayCardResponse](#playcardresponse)

* furtheractions

  > All actions that a user can perform, except for the otherwise specified (e.g. playCard, DrawCard)
  >
  > [CustomAction](#customaction), [CustomActionResponse](#customactionresponse), [FinishTurn](#finishturn)
  >


* toast

  > [ShowToast](#showtoast)


* endgame

  > [PlayerFinished](#playerfinished), [GameOver](#gameover)




**specified content** 

+ [state](#state)
+ [card](#card)
+ [possible actions](#possibleactions)

## Ping / Pong

Request a simple answer from the other to make sure they're still there or to measure the delay.
`ping` expects an answer, while `pong` is the answer.

The **Server & Client** should both support sending both message types

##### Ping

```JSON
{
  "header":{
    "messagegroup":"networking",
    "messagetype":"Ping"
  },
  "body":{
    
  }
}
```

##### Pong

```JSON
{
  "header":{
    "messagegroup":"networking",
    "messagetype":"Pong"
  },
  "body":{
    
  }
}
```

## Keepalive

Similar to ping, but respond to receiving the keepalive packet by sending it back.

Implemented by **server** and **client**.

If no packet is received within some timeout, the connection is considered broken.

The reason for this packet is that we cannot know whether a client disconnected unless we send to it. In the worst case, this would mean that we patiently wait some 30 seconds for a client to play (gamelogic, if implemented at all), and only once we decide that this was too long and tell it that we disconnected it, we notice. To circumvent this, we send a keepalive packet back and forth and can thus make use of the `socket.setTimeout`, which only reacts to data packets, not to the tcp-internal keepalive packets.

Implementing this packet is easily uncoupled from the rest of the packets and is thus a TODO for later.

```json
{
  "header":{
    "messagegroup":"networking",
    "messagetype":"KeepAlive"
  },
  "body":{
  }
}
```

***

## Register

Request to play on this server, or to spectate.
`username` : *String* 

> should be unique. If it is not `reason` in [RegisterFailure](#registerfailure) will be *"not unique"*
>
> should NOT be the empty string *""* or *"server"*

`role` : *String*

> *"player"* or *"spectator"* 

`playerSeating` : JSONObject

> Specifications are free to Chris. This field is used to communicate to other devices what the own orientation is. We assume the devices sit around the spectator, thus allowing us to calculate a positioning of playernames on the spectator which mirrors the real seating.
>
> This Object will be sent by the server to everybody in InitGame

The **Client** sends this and Server only reacts to it. The Server's reaction is either RegisterSuccess or RegisterFailure, followed by an information to all players with the [updated lobby list](#updatelobbylist).

```JSON
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"Register"
  },
  "body":{
    "username":"roger",
    "role":"player",
    "playerSeating":{}
  }
}
```

##RegisterSuccess

If the client requests the same role multiple times, the request will still succeed. If the client requests different roles, one is chosen arbitrarily by the server. Therefore, the packet sent from the server contains the role the client was given.

```json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"RegisterSuccess"
  },
  "body":{
    "username":"roger",
    "role":"spectator",
  }
}
```

## UpdateLobbyList

The **server** broadcasts the new List of Players and Spectators to the clients whenever a new one connects or leaves. The first client thus only receives a list with itself and knows that it is admin.

It is not important for the client to know spectators and the admin, but it might be useful for the UI if we want to show that.

`lobbylist` : *JSONArray of JSONObjects*

> An unordered list of players and spectators, consisting of their username and their role

`admin` : *String*

> The username of the admin

`playerSeating`:JSONObject

> As specified in the [register messsage](#register) by chris

```json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UpdateLobbyList"
  },
  "body":{
    "lobbylist":[
      {"username":"roger", "role":"player",     "playerSeating":{}},
      {"username":"chris", "role":"player",      "playerSeating":{}},
      {"username":"table", "role":"spectator",      "playerSeating":{}}
    ],
    "admin":"roger",
  }
}
```

## RegisterFailure

With our current goals, this should most often return *"true"* and is thus like an ACK. Also return which role was assigned in case the client requested both for some weird reason. If the client requests the same role multiple times, `success` will still be true if the client is still registered as this role and false if it has a different role or is unregistered.

If the client has not been registered. This may be because of an invalid username as specified under [Register](#register) or for some other reason. This message specifies also what role the user requested and what username he provided.

`reason` : *String* 

> can be one of the following options
>
> + *"not unique"* if the same username was already registered
> + *"already registered"* if the same connection already has registered a username
> + *"invalid"* if the username is the empty string or *"server"*. Or if the username contains invalid characters. One invalid character is the Tilde, which is reserved to identify non-username-strings
> + *"lobby full"* if the server decided to fixate the number of players or spectators and the game has not yet started (otherwise, the server wouldn't react at all).
> + *"game already in progress"*

```JSON
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"RegisterFailure"
  },
  "body":{
    "role":"player",
    "username":"server",
    "reason":"invalid"
  }
}
```

## UnregisterRequest

The **Client** requests to leave the game and close the connection.

`username`: *String*

> The user who wants to leave. If this is not the username of the sender, this will fail silently.

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

We don't need to wait for the Server to respond. The Server logic will probably handle these leaves similarly to connection timeouts.

However, the **Server** might decide to [kick](#kick) a player. For these purposes, [the Response](#unregisterplayer) exists.
Also, the other Clients need to know about leaves.

##UnregisterResponse

Sent by the **server** to all clients, including the one who was unregistered. After sending this message, the server can stop responding to this client. This might happen even after the game has started and will then be treated similarly to a client losing connection.

`reason` : *String*

> whether the client was kicked or asked to leave. *"kicked"* in the first case, *"disconnected"* if it asked to leave, *"timeout"* if the client suddenly stopped responding and was thus kicked by the server. *"server"* if the server is turning off or other internal reasons.

```Json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UnregisterResponse"
  },
  "body":{
    "username":"that random dude who we didn't want",
    "reason":"kicked"
  }
}
```

## Kick

The player who started the server, from now on referred to as admin, may decide to kick a player or spectator from the lobby or the running game. Doing so is essentially the same as when sending [UnregisterRequest](#unregisterRequest) , but only allowed for the admin. The admin is informed about the outcome, either by a broadcast of [UnregisterResponses](#unregisterresponse) to all clients or by a [KickFailure](#kickfailure) to only the admin.

This request might also be sent during the game.

```json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"Kick"
  },
  "body":{
    "username":"that random dude who we didn't want",
  }
}
```

## KickFailure

The **server** informs the admin that [kicking](#kick) did not work. If the client who requested the kick was not the admin, it informs that client that it is not allowed to kick.

`reason` : *String*

> * *"not allowed"* : you are not allowed to kick people
> * "not found" : this player or spectator is not registered
> * *"invalid"* : this username is invalid

```json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"KickFailure"
  },
  "body":{
    "username":"the user that you wanted to kick",
    "reason":"why you failed"
  }
}
```

***

## SpecifyRules

The **admin client** informs the server what rules it chose. The rule is only passed by identifier, because both the client and the server already need to know all rules that can be used.

Since every rule might have dynamic parameters, they are all stored as JSONObject where only their name is guaranteed to be available.

Some rules refer to cards, some rules should be applied globally.
For the `cardRules`, there is a list of rules with their parameters for all cards. For the `globalRules`, every rule is only listed once.

The rule *must* have `id` and `parameters` . For any card not having rules applied to it specifically, and  with no entry provided, it will be assumed that it is not supposed to be in the deck. Otherwise, there must be a `number` in the card's object to specify how often the card should be in the deck. (The deck will be re-shuffled once it is empty)

> See also [rule](#rules)

```json
{
  "header":{
    "messagegroup":"startgame",
    "messagetype":"SpecifyRules"
  },
  "body":{
    "cardRules":{
      "someCardID":{
        "rulelist":[
          {"id":"instantWinOnCardPlayed", "parameters":{}},
          {"id":"chooseColorCard", "parameters":{"param1":"lulz",
                                                "lolcat":"foobar"}}
          ],
        "number":"3"
    },
      "otherCardID":{
          "number":"7",
          "rulelist":[
          {"id":"instantWinOnCardPlayed", "parameters":{}}
        ]
      }
    },
    "globalRules":[
        {"id":"startWithXCards","parameters":{"x":"7"},
        {"id":"exodia","parameters":{}},
        {"id":"handicap","parameters":{"arr":[{"chris":"100"},{"roger":"-10"}]}}
	]
  }
}
```

## StartGame

The **admin client** informs the server that it should start the game and stop listening to new connections.

```json
{
  "header":{
    "messagegroup":"startgame",
    "messagetype":"StartGame"
  },
	"body":{}
}
```

## InitGame

Informs the Client which rules will be used (by identifier), 
`ruleset` is a (not specifically sorted) JSONObject of rules. Every rule contains a `rulename` JSONObject and further details specific to the rule.

Also sends all player names, ordered by turn-order

The **Server** sends this to the Client. No response from the Client required.

`turn-order` : *JSONArray of usernames as Strings* 

`ruleset` : *JSONObject containing the body of [SpecifyRules](#specifyrules) *

> See also [rule](#rules)

```Json
{
  "header":{
    "messagegroup":"startgame",
    "messagetype":"InitGame"
  },
  "body":{
    "ruleset":{
      "cardRules":{
        "someCardID":[
          {"id":"instantWinOnCardPlayed", "parameters":{}},
          {"id":"chooseColorCard", "parameters":{"param1":"lulz",
                                                "lolcat":"foobar"}}
        ],
        "otherCardID":[
          {"id":"instantWinOnCardPlayed", "parameters":{}}
        ]
      },
      "globalRules":[
          {"id":"startWithXCards","parameters":{"x":"7"},
          {"id":"exodia","parameters":{}},
          {"id":"handicap","parameters":{"arr":[{"chris":"100"},{"roger":"-10"}]}}
      ]
    },
    "turn-order":[
      "sisisilvia",
      "faeglas",
      "baclemen"
    ]
  }
}
```

Note that the example rules here were spontaneously written and might not be specified.

***

## DrawCards

Request to draw new cards. The Server will return as many cards as the minimum of cards drawable that is not 0.

The **Client** sends this request. The Server checks whether the Client is allowed to draw this many cards and hands back the appropriate amount of cards later using [**DrawCardsResponse**](#drawcardsresponse).

```json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"DrawCards"
  },
  "body":{
  }
}
```



## DrawCardsResponse

`cards` : *JSONArray of JSONObjects*

> The drawn cards, setup so that the UI will know where they came from in order to animate if it wants to
>
> ```json
> {
>   "ID":"cardID1337",
>   "origin":"stack"
> }
> ```
>
> `origin` can be 
>
> + *~stack* if the card had previously been played and was now drawn from the stack
> + *~talon* if the card had not been drawn yet and was now drawn from the talon
> + some username if the card was in a users hand
> + *~unspecified* or simply not set if not specified
>
> Not that the tilde is not allowed within usernames, so this is clear specification
>
> The [card](#card) does not have to be unique.

The **server** sends this request and will follow up with a complete [sendState](#sendstate) 

```Json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"DrawCardsSuccess"
  },
  "body":{
    "cards":[
      {"ID":"cardID1","origin":"talon"},
      {"ID":"cardID3","origin":"talon"},
      {"ID":"cardID1","origin":"talon"}
    ]
  }
}
```

If the drawing failed for some reason, the response will instead be

```json
{
  "header":{
    "messagegroup":"draw",
    "messagetype":"DrawCardsFailure"
  },
  "body":{
    "reason":"unspecified"
  }
}
```

where `reason` can be *"unspecified" or *"not allowed"*, *"game not running"* ... (has yet to be used. Usage will show what reasons we will have. Until then, it will always be *"unspecified"*)

***

## PlayCard

The **Client** can request to play a card. The Server will play the card if it is valid and return a success/failure message. After that, it will send you the new state and if success to everybody else as well.

`card` : *JSONObject*

> We use a JSONObject for the card to make this more easily extensible.
>
> See [Card](#card) for more info!
>
> ```json
> {
>   "ID":"cardID1337",
>   "origin":"xxx1337baclemenxXx"
> }
> ```



```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlayCard"
  },
  "body":{
    "card":{
      "ID":"cardID1337",
      "origin":"~stack"
    },
     "playParameters":{
    	"wishColorRule":{"wishForColor":"blue"},
    	"ruleDank":{"xXx":"1337"}
    }
  }
}
```

`playParameters` is a list of *JSONObjects*  which represent settings specific to this card ID when played. Exampli gratuita, a player might play a card that allows them to wish for a color. It is easiest when that selection is sent with the playCard Request.

This field will usually be ignored, unless a rule uses it. To use it, you can call `message.getPlayParameters("wishForColors")` to get the *String* associated with *"wishForColors"* or `message.getPlayParameters()` to get the whole *JSONObject* list. 
Example:

```java
wishedColor = CardColor.valueOf(played.getPlayParameter("wishColorRule", "wishedColor")); 
// I added this as alternative idea (Eric, 10.12.2017)
        // Idee: wenn die Karte gespielt wird, muss die UI sowieso wissed dass der user eine farbe auswählen muss. Also user direkt farbe auswählen lassen.
        //      Danach die karte clientside mit diesem parameter setzen.
        //      Wenn server die karte erhält wird diese regel getriggert und die liest den parameter aus.
```

Make sure to use a rule name coherent with our rules.

## PlayCardResponse

This is sent by the **server** to specify whether a play was valid and has been played, or not.

`success` : *String*

> *"true"* or *"false"*

```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlayCardResponse"
  },
  "body":{
	"success":"true"
  }
}
```

***

## GetState

The **Client** requests to know its own canonical [state](#state). This involves their current hand and the already played cards, also called the stack, as well as the number of cards the other players are holding.

The [response](#sendstate) will usually also be sent without being requested - e.g. after a player finished his turn.

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

##SendState

The **server** sends this after being asked via [GetState](#getstate) or when appropriate, i.e. some player did something or the state changed for some other reason.

See the specification of [state](#state) for information about the formatting.

The states will be empty if there was a GetState request while not appropriate - e.g. the game not yet running.

See [Card](#card) for more info!

```Json
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"SendState"
  },
  "body":{
    "globalstate":{
      "numcardsinhand":[
        {"name":"Eric", "handSize":"3"},
        {"name":"Rip","handSize":"100"},
        {"name":"Ric","handSize":"2"}
      ],
      "stack":[
        {"ID":"cardID01", "origin":"~talon"},
        {"ID":"cardID1337", "origin":"Rip"}
      ],
      "whoseturn":"Ric",
      "drawxcardsmin":"2"
    },
    "playerstate":{
      "hand":[
        {"ID":"cardID03", "origin":"Eric"}
      ],
      "possibleactions":
        [
          {"actionName":"leaveGame","parameters":{}},
          {"actionName":"drawCards", "parameters":{}},
          {"actionName":"playCard", "parameters":{"playableCards":["cardID1", "cardID1337"]}}
        ]
    }
  }
}
```

***

## ShowToast

Mostly for debugging.

Send some `toast` that should be displayed to the clientside user.

`style` : *JSONObject* 

> The client might ignore this. If the UI supports multiple textcolors or background colors, this will specify a style. How exactly is yet to be specified. The client should always support this being empty or null.

`from`: *"String"*

> From which user it is. empty string if it is from the server

```json
{
  "header":{
    "messagegroup":"toast",
    "messagetype":"ShowToast"
  },
  "body":{
    "toast":"сука блиать",
    "from":"josua",
    "style":{"some":"JSONOBJECT"}
  }
}
```

***

## PlayerFinished

The **Server** informs the clients that one Player has finished the game. E.g. by having played all cards. 

After this, the server will remove the player from the turn order list and let it spectate until [end of game](#endgame)

```Json
{
  "header":{
    "messagegroup":"endgame",
    "messagetype":"PlayerFinished"
  },
  "body":{
    "username":"roger",
  }
}
```

## GameOver

The **Server** informs the clients that the game is over and they can show the after-game UI. E.g. displaying points. Per default, the Client will display the points next to the user in a Ranking list, sorted based on the points and the specified rules.

points: *JSONObject of Players and points*

```Json
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
```

***

## State

The state is defined as containing the global state and the personal player state.
See [Card](#card) for more info!

`numcardsinhand`  : *String* to *String* JSONObject

> Global State: how many cards which player has

`stack` : *JSONArray of [cards](#card)*

> Global State: ordered List of cards. Can contain x cards | x ∈ [0,∞[

`whoseturn` : *String* 

> Global State: the username whose turn it is

`drawxcardsmin` : *String* 

> Global State: the user whose turn it is will draw at least this number of cards if he decides to draw



`hand` : *JSONArray of [cards](#cards)*

> Player State: This player's hand cards.
>
> The cards contain origin, though not really needed because the client can usually figure out where the new handcards came from by looking at [PlayCardResponse](#playcardresponse)

`possibleactions` :*JSONArray of JSONObjects that represent possible player actions*

> Player State: Unordered. What [actions](#possibleactions) this player can choose from.

```json
{
  "globalstate":{
    "numcardsinhand":[
        {"name":"Eric", "handSize":"3"},
        {"name":"Rip","handSize":"100"},
        {"name":"Ric","handSize":"2"}
      ],
    "stack":[
      {"ID":"cardID01", "origin":"~talon"},
      {"ID":"cardID1337", "origin":"Rip"}
    ],
    "whoseturn":"Ric",
    "drawxcardsmin":"2"
  },
  "playerstate":{
    "hand":[
      {"ID":"cardID03", "origin":"Eric"}
    ],
  "possibleactions":
        [
          {"actionName":"leaveGame","parameters":{}},
          {"actionName":"drawCards", "parameters":{}},
          {"actionName":"playCard", "parameters":{"playableCards":["cardID1", "cardID1337"]}}
        ]
  }
}
```

## Card

The drawn cards, setup so that the UI will know where they came from in order to animate if it wants to

```json
{
  "ID":"cardID1337",
  "origin":"stack"
}
```

`origin` can be 

- *~stack* if the card had previously been played and was now drawn from the stack
- *~talon* if the card had not been drawn yet and was now drawn from the talon
- some username if the card was in a users hand
- *~unspecified* or simply not set if not specified

Not that the tilde is not allowed within usernames, so this is clear specification

`ID` is an identifier to find the corresponding card in the resources / local memory.

The [card](#card) does not have to be unique, there may be multiple copies of the same card in play.

## PossibleActions

Action-IDs the client can choose from and should support:

+ "leaveGame"

  > Inform the server that we want to disconnect
  > [UnregisterRequest](#unregisterrequest)

+ "drawCards"

  > Inform the server that we want to draw cards
  > [DrawCards](#drawcards)

+ "kickPlayer" (username)

  > Inform the server that we want to kick a player
  > [Kick](#kick)
  >
  > The parameters do not contain the players he can kick, because if he can kick, he can kick all.

+ "playCard" (cardID)

  > Inform the server which card we would like to play
  > [PlayCard](#PlayCard)
  >
  > The parameters is a JSONArray of Card-IDs Strings


Possibly in the future supported: "transferServer"

Every possible Action has the option to provide parameters custom to that action. E.g. the playCard action needs to know what cards can be played:

```json
{"actionName":"playCard","parameters":{"playableCards":["red_3","green_rev"]}}
```



## FurtherActions

Depending on the rules, we need additional messages. E.g. when the user plays a card that allows them to choose a colour, they will get a state where something like "choose color" is a  (custom) possible action. To reply to the server, the rule must handle sending/receiving custom messages, i.e. [CustomAction](#customaction) on the client side and [CustomActionResponse](#customactionresponse) to respond from the server.



`ruleName` identifies the Rule to the method that decides what to do when receiving this message - i.e. which rule to execute.

See also [Custom Rule Actions](#custom-rule-actions)

### CustomAction

sent by the **client**

```json
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"CustomAction"
  },
  "body":{
    "ruleName":"TheRule27",
    "custom parameter of the rule":{ a custom JSONObject}
  }
}
```

### CustomActionResponse

sent by the **server**

```json
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"CustomActionResponse"
  },
  "body":{
    "ruleName":"TheRule27",
    "success":"true",
    "custom parameter of the rule":{ a custom JSONObject}
  }
}
```

This content will be as follows if it is not appropriate to respond to it e.g. because the game is not running:

```json
"body":{
  "success":"false"
}
```





### FinishTurn

sent by the **client**

is ignored by the server if the game is not running.

```json
{
  "header":{
    "messagegroup":"furtheractions",
    "messagetype":"FinishTurn"
  },
  "body":{}
}
```



## Rules

All we know as of 16.11.2017 is that Rules should have an identifier String.

It would probably make sense to include some compatibility notes for use with other rules.

State of 05.12.2017: There are two kind of rules.

* Those that apply to a specific card (or multiple)
* Those that apply globally

A rule is only specified by its identifier and maybe some parameters, already provided as a jsonObject. The parameters are specific to the rule and only parsed to a JSON Object

```json
{
  "id":"never gonna give you up, never gonna let you down",
  "parameters":{
    "never":"gonna run around",
    "and desert":"you"
  }
}
```



## Custom Rule Actions

List here which rules require which parameter format.

`wishColorRule`:`{"wishForColor":"blue"}`