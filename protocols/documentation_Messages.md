# JSON INTERFACE DOCUMENTATION

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

Request to play on this server.
`username` : *String* 
`success` : *String* 
​    Can be *"true"*, *"false"* (or later be further extended. E.g. *banned* )
​    *"true"* means the client is allowed to stay on this server

The **Client** sends this and Server only reacts to it

##### Request

```JSON
{
  "messagetype":"register",
  "username":"roger"
}
```

##### Response

With our current goals, this should always return *"true"* and is thus like an ACK.

```JSON
{
  "messagetype":"response register",
  "success":"true"
}
```

## StartGame

Inform the clients that the game is about to start and they are allowed to request cards.

The **Server** sends this.

##### Request

```json
{
  "messagetype":"start game"
}
```

#####Response

No response from the clients is needed, as their next step will be to demand cards.

## DrawXCards

Request *x* new cards.
`X` : *int*

The **Client** sends this request. The Server checks whether the Client is allowed to draw this many cards and hands back the appropriate amount of cards later. The response contains how many these will be.
`will give` : *int*, can be 0

##### Request

```json
{
  "messagetype":"draw x cards",
  "x":3
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

Just use  [**DrawXCards**](#DrawXCards) with `x=1`