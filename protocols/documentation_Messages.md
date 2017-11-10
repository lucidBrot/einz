# JSON INTERFACE DOCUMENTATION

How the Einz Server and Client are supposed to communicate.

***

The IP of the sender should never be needed, as the TCP connection already provides this.

The username of the client will be stored serverside for convenience, no need to retransmit every time.

Every String is case-sensitive!

***

**STILL TO SPECIFY** : [`card`](#drawxcards)  , [`style`](#showmessage)  , [`state`](#getstate) 

## Ping / Pong

Request a simple answer from the other to make sure they're still there or to measure the delay.
`ping` expects an answer, while `pong` is the answer.

The **Server & Client** should both support sending both message types

##### Request

```JSON
{
  "messagetype":"Ping"
}
```

##### Response

```JSON
{
  "messagetype":"Pong"
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
  "messagetype":"Register",
  "username":"roger",
  "role":"player"
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
  "messagetype":"RegisterResponse",
  "success":"true",
  "role":"spectator",
  "reason":"this can be anything if success was true"
}
```

## Unregister

The **Client** requests to leave the game and close the connection.

##### Request

```Json
{
  "messagetype":"Unregister",
  "username":"roger"
}
```

We don't need to wait for the Server to respond. The Server will probably handle these leaves similarly to connection timeouts.

However, the **Server** might decide to kick a player. For these purposes, the Response exists.
Also, the other Clients need to know about leaves.

##### Response

`kicked` : *boolean*

> whether the client was kicked or asked to leave. *false* in the latter case.

```Json
{
  "messagetype":"Unregistered",
  "username":"that random dude who we didn't want",
  "kicked":true
}

```

## <a name="sendrules"></a>SendRules / RulesInitialized

Informs the Client which rules will be used. The rules themselves might have to be implemented client-side as well.
`ruleset` is a (not specifically sorted) JSONObject of rules. Every rule contains a `rulename` JSONObject and further details specific to the rule.

The **Server** sends this to the Client. The Client responds with the response once it's ready.

##### Request

```Json
{
  "messagetype":"SendRules",
  "ruleset":{
    "startWithXCards":{
      "x":7
    },
    "instantWinOnCardXPlayed":{
      "cardcolor":"green",
      "cardnum":3
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
  "messagetype":"RulesInitialized",
  "invalid rules":["instantWinOnCardXPlayed", "';DROP DATABASE usernames"],
}
```

## StartGame

Inform the clients that the game is about to start and they are allowed to request cards. This will be sent after [**sendRules**](#sendrules).

The **Server** sends this.

##### Request

```json
{
  "messagetype":"StartGame"
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
  "messagetype":"DrawXCards",
  "x":3
}
```

##### Response

```Json
{
  "messagetype":"DrawXCardsResponse",
  "will give":2
}
```

## DrawCard

Just use  [**DrawXCards**](#drawxcards) with `x=1`

## WhoseTurn

The **Server** informs each Client whenever somebodys' turn starts. The Client can thus find out whether it's its turn.

##### Request

```Json
{
  "messagetype":"WhoseTurn",
  "username":"Donald Trump"
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
  "messagetype":"PlayCard",
  "dry-run":true
  "card":{
    "color":"green"
    "num":1337
  }
}
```

##### Response

`play valid` : *boolean*

> true if the card can be played. If `dry-run` is *false*, then this means the card will be played and the server will soon send [**CardPlayed**](#cardplayed) to all clients.

`dry-run` : *boolean*

> the same value as the received `dry-run`

`your turn` : *boolean*

> Whether the play is not only valid but it's also the clients turn

```Json
{
  "messagetype":"PlayCardResponse",
  "dry-run":true,
  "play valid":true,
  "your turn":true
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
  "messagetype":"CardPlayed",
  "card":{
    "some info":"to be specified",
    "color":"green"
  }
  "player":"владимир путин"
}
```

## InvalidCardPlayed

Response from the **Server** if a [**PlayCard**](#playcard) request was invalid.
`reason` : *String* 

> Not yet defined, for debugging purposes.

```Json
{
  "messagetype":"InvalidCardPlayed",
  "reason":"bad coding skills and an inexistent card"
}
```

## GetState

The **Client** requests to know its own canonical state. This involves their current hand and the already played cards, also called the stack.

The **Server** will then respond if the username matches the IP.

##### Request

```Json
{
  "messagetype":"GetState"
}
```

##### Response

`hand` : Array of *JSONObjects*

> A List of Cards that the Client should have in their hand.

`stack` : Array of *JSONObjects*

> A List of the already played cards, the card at index 0 being the oldest card on the stack

```Json
{
  "messagetype":"GetStateResponse",
  "hand":[
    {"color":"blue", "value":7},
    {"color":"black", "special":"play Vitas"}
  ],
  "stack":[
    {"color":"blue", "num":3},
    {"color":"blue", "num":4},
    {"color":"red", "num":4}
  ]
}
```

## HandOutCard

The **Server** hands out 1 card to a client.

`card` : *JSONObject* 

> The card that is handed to the client

```Json
{
  "messagetype":"HandOutCard",
  "card":{
    "color":"green",
    "num":"black"
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
  "messagetype":"ShowMessage",
  "message":"сука блиать",
  "style":{"some":"JSONOBJECT"}
}
```

## SendMessage

The **Client** requests the server to send [**ShowMessage**](#showmessage) to `target` username

```Json
{
  "messagetype":"SendMessage",
  "target":"roger",
  "message":"my message"
}
```

## BroadcastMessage

The **Client** requests the server to send [**ShowMessage**](#showmessage) to all clients, including itself.

```json
{
  "messagetype":"BroadcastMessage",
  "message":"my message"
}
```

## PlayerFinished

The **Server** informs the clients that one Player has finished the game. E.g. by having played all cards. Optionally, depending on rules, there is `more` information included or `points`. If not, they should be *null*.

```Json
{
  "messagetype":"PlayerFinished",
  "username":"roger",
  "points":1,
  "more":{
    "a":"b"
  }
}
```

## GameOver

The **Server** informs the clients that the game is over and they can show the after-game UI. E.g. displaying points.

```Json
{
  "messagetype":"GameOver"
}
```
