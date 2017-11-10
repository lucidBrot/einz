# JSON INTERFACE DOCUMENTATION

work in progress

***

The IP of the sender should never be needed, as the TCP connection already provides this.

The username of the client could also be stored, but shall be sent when useful as a matter of convenience, but should be checked for integrity by the Server.

## Ping / Pong

Request a simple answer from the other to make sure they're still there or to measure the delay.
`ping` expects an answer, while `pong` is the answer.

The **Server & Client** should both support sending both message types

##### Request

```JSON
{
  "messagetype":"ping"
}
```

##### Response

```JSON
{
  "messagetype":"pong"
}
```



## Register

Request to play on this server, or to spectate.
`username` : *String* 
`success` : *String* 
​    Can be *"true"*, *"false"* (or later be further extended. E.g. *banned* )
​    *"true"* means the client is allowed to stay on this server
`role` : *String*
​    *"player"* or *"spectator"* 

The **Client** sends this and Server only reacts to it

##### Request

```JSON
{
  "messagetype":"register",
  "username":"roger",
  "role":"player"
}
```

##### Response

With our current goals, this should always return *"true"* and is thus like an ACK. Also return which role was assigned in case the client requested both for some weird reason. If the client requests the same role multiple times, `success` will still be true if the client is still registered as this role and false if it has a different role or is unregistered.

If the client is not registered, `role` should have a return value of *"null"*.

```JSON
{
  "messagetype":"register response",
  "success":"true",
  "role":"spectator"
}
```

## Unregister

The **Client** requests to leave the game and close the connection.

##### Request

```Json
{
  "messagetype":"unregister",
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
  "messagetype":"unregistered",
  "username":"that random dude who we didn't want",
  "kicked":true
}

```

## SendRules

Informs the Client which rules will be used. The rules themselves might have to be implemented client-side as well.
`ruleset` is a (not specifically sorted) JSONObject of rules. Every rule contains a `rulename` JSONObject and further details specific to the rule.

The **Server** sends this to the Client. The Client responds with the response once it's ready.

##### Request

```Json
{
  "messagetype":"send rules",
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

The username is as always only specified for convenience, not security.

```Json
{
  "messagetype":"rules initialized",
  "invalid rules":["instantWinOnCardXPlayed", "';DROP DATABASE usernames"],
  "username":"roger"
}
```

## StartGame

Inform the clients that the game is about to start and they are allowed to request cards. This will be sent after [**sendRules**](#sendrules).

The **Server** sends this.

##### Request

```json
{
  "messagetype":"start game"
}
```

##### Response

No response from the clients is needed, as their next step will be to demand cards.

## DrawXCards

Request *x* new cards.
`X` : *int*

The **Client** sends this request. The Server checks whether the Client is allowed to draw this many cards and hands back the appropriate amount of cards later. The response contains how many these will be.
`will give` : *int*, can be 0

The username is included for convenience, but the Server should still check whether it fits to the IP.

##### Request

```json
{
  "messagetype":"draw x cards",
  "x":3
  "username":"some russian hacker"
}
```

##### Response

```Json
{
  "messagetype":"draw x cards response",
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
  "messagetype":"whose turn",
  "username":"Donald Trump"
}
```

## PlayCard

The **Client** can request to play a card. If `dry-run` is false, the Server will play the card if it is valid and otherwise return a failure message. So the client should wait to handle the response before doing anything else.
If `dry-run` is true, the Server will only answer whether the move is valid but not play the card.

As always, `username` is only for convenience and has to be validated by the server for security reasons. If `username` does not fit to the IP of the client, the Server should answer with [**InvalidUsername**](#invalidusername).

`card` : *JSONObject*

> The specifics of the card are not yet defined (10.11.2017)
> If the card is invalid, the Server should answer with [**InvalidCardPlayed**](#invalidcardplayed).

##### Request

```Json
{
  "messagetype":"play card",
  "dry-run":true
  "username":"roger",
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
  "messagetype":"play card response",
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
  "messagetype":"card played",
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
  "messagetype":"invalid card played",
  "reason":"bad coding skills and an inexistent card"
}
```

## InvalidUsername

Sent by the **Server** if the username is not registered or does not correspond to the IP. This is not supposed to actually happen in normal execution.

`reason`

> *"not registered"*  or *"not matching IP"*

```Json
{
  "messagetype":"invalid username",
  "reason":"not registered"
}
```

