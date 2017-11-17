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

  > [Register](#register), [RegisterSuccess](#registersuccess), [RegisterFailure](#registerfailure), [UpdateLobbyList](#updatelobbylist) [UnregisterRequest](#unregister), [UnregisterResponse](#unregisterresponse), [Kick](#kick)


* startgame

  > [SpecifyRules](#specifyRules), [StartGame](#startgame), [InitGame](#initgame)


* draw

  > [DrawCards](#drawcards), [DrawCardsResponse](#drawcardsresponse)


* stateinfo

  > [SendState](#sendstate), [GetState](#getstate) 


* playcard

  > [PlayCard](#playcard), [PlayCardResponse](#playcardresponse)


* toast

  > [ShowToast](#showtoast)


* endGame

  > [PlayerFinished](#playerfinished), [EndGame](#endgame)




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

If no keepalive packet is received within some timeout, the connection is considered broken.
`timeout` specified by the server in ms.

The reason for this packet is that we cannot know whether a client disconnected unless we send to it. In the worst case, this would mean that we patiently wait some 30 seconds for a client to play, and only once we decide that this was too long and tell it that we disconnected it, we notice. To circumvent this, we send a keepalive packet back and forth and can thus make use of the `socket.setTimeout`, which only reacts to data packets, not to the tcp-internal keepalive packets.

Implementing this packet is easily uncoupled from the rest of the packets and is thus a TODO for later.

```json
{
  "header":{
    "messagegroup":"networking",
    "messagetype":"keepalive"
  },
  "body":{
    "timeout":"1000"
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

The **server** broadcasts the new List of Players and Spectators to the clients whenever a new one connects. The first client thus only receives a list with itself and knows that it is admin.

It is not important for the client to know spectators and the admin, but it might be useful for the UI if we want to show that.

`lobbylist` : *JSONArray of JSONObjects*

> An unordered list of players and spectators, consisting of their username and their role

`admin` : *String*

> The username of the admin

```json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UpdateLobbyList"
  },
  "body":{
    "lobbylist":[
      {"roger":"player"},
      {"chris":"player"},
      {"table":"spectator"}
    ],
    "admin":"roger"
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

Sent by the **server** to all clients, including the one who was unregistered. After sending this message, the server can stop responding to this client.

`reason` : *String*

> whether the client was kicked or asked to leave. *"kicked"* in the first case, *"disconnected"* if it asked to leave, *"timeout"* if the client suddenly stopped responding and was thus kicked by the server.

```Json
{
  "header":{
    "messagegroup":"registration",
    "messagetype":"UnregisterResponse"
  },
  "body":{
    "username":"that random dude who we didn't want",
    "reason":"true"
  }
}
```

## Kick

The player who started the server, from now on referred to as admin, may decide to kick a player from the lobby or the running game. Doing so is essentially the same as when sending [UnregisterRequest](#unregisterRequest) , but only allowed for the admin.

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

***

## SpecifyRules

The **admin client** informs the server what rules it chose. The rule is only passed by identifier, because both the client and the server already need to know all rules that can be used.

Since every rule might have dynamic parameters, they are all stored as JSONObject where only their name is guaranteed to be available.

`ruleset` : *JSONOBject containing non-uniform JSONObjects*

> The identifier of the JSONObject is also the identifier of the [rule](#rule)

```json
{
  "header":{
    "messagegroup":"startgame",
    "messagetype":"SpecifyRules"
  },
  "body":{
    "ruleset":{
        "startWithXCards":{
          "x":"7"
        },
       "instantWinOnCardXPlayed":{
          "cardcolor":"green",
          "cardnum":"3"
       },
      "exodia":{},
      "handicap":{"arr":[{"chris":"100"},{"roger":"-10"}]}
	}
  }
}
```

## StartGame

The **admin client** informs the server that it should start the game and stop listening to new connections.

```json
{
  "header":{
    "messagegroup":"startgame",
    "messagetype":"startGame"
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

`ruleset` : *JSONOBject containing non-uniform JSONObjects*

> The identifier of the JSONObject is also the identifier of the [rule](#rule)

```Json
{
  "header":{
    "messagegroup":"startgame",
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
       },
      "exodia":{},
      "handicap":{"arr":[{"chris":"100"},{"roger":"-10"}]}
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

Request to draw new cards. The Server will return as many cards as the minimum of cards drawable that is not 0, or 0 as a sign of failure.

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

##DrawCardsResponse

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
    "messagetype":"DrawCardsResponse"
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

***

## PlayCard

The **Client** can request to play a card. The Server will play the card if it is valid and return a success/failure message. After that, it will send you the new state and if success to everybody else as well.

`card` : *JSONObject*

> Only the card ID is needed. Still, we use a JSONObject for the card to make this more easily extensible.
>
> ```json
> {
>   "ID":"cardID1337"
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
      "ID":"cardID1337"
    }
  }
}
```

## PlayCardResponse

This is sent by the **server** to specify whether a play was valid and has been played, or not.

`success` : *String*

> *"true"* or *"false"*

```Json
{
  "header":{
    "messagegroup":"playcard",
    "messagetype":"PlaycardResponse"
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

```Json
{
  "header":{
    "messagegroup":"stateinfo",
    "messagetype":"SendState"
  },
  "body":{
    "globalstate":{
      "numcardsinhand":{
        "Eric":"3",
        "Rip":"100",
        "Ric":"2"
      },
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
        "leaveGame", "drawCards", "playCard"
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

> The client might ignore this. If the UI supports multiple textcolors or background colors, this will specify a style

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
    "messagegroup":"endGame",
    "messagetype":"PlayerFinished"
  },
  "body":{
    "username":"roger",
  }
}
```

## EndGame

The **Server** informs the clients that the game is over and they can show the after-game UI. E.g. displaying points. Per default, the Client will display the points next to the user in a Ranking list, but `points` is not neccessary, depending on the ruleset.

`ranking` : *JSONArray of Players*

> The Players are of the form
>
> ```json
> {
>   "username":{
>     "points":"12"
>   }
> }
> ```

```Json
{
	"header": {
		"messagegroup": "endGame",
		"messagetype": "GameOver"
	},
	"body": {}
}
```

***

## State

The state is defined as containing the global state and the personal player state.

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

`possibleactions` :*JSONArray of Strings*

> Player State: Unordered. What [actions](#possibleactions) this player can choose from.

```json
{
  "globalstate":{
    "numcardsinhand":{
      "Eric":"3",
      "Rip":"100",
      "Ric":"2"
    },
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
      "leaveGame", "drawCards", "playCard"
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

+ "playCard" (cardID)

  > Inform the server which card we would like to play
  > [PlayCard](#PlayCard)



Possibly in the future supported: "transferServer"

## Rules

All we know as of 16.11.2017 is that Rules should have an identifier String.

It would probably make sense to include some compatibility notes for use with other rules.