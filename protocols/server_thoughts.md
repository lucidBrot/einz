# Serverthoughts

Thoughts, considerations and ideas that came up during the development of the server.
***
## Threads vs Services
If the server runs in a thread, it would be paused when the Screen is turned off. if it runs in a service, it would not run in parallel, unless in a thread itself.
So we will run it in a thread and be ok with it being possibly paused if the user turns off the device screen.

***
## Callback to update UI
The UI has to be updated from the main thread. The UI thread can be explicitely used by runOnUiThread, but this is only possible from within an Activity. So I implemented `ServerActivityCallbackInterface` to allow updating the list of connected clients.
[See this Link on SO](https://stackoverflow.com/questions/5161951/android-only-the-original-thread-that-created-a-view-hierarchy-can-touch-its-vi#comment11782071_5162096)

***
## Sources
[Example TCP Server](https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server)

## Message Parsing

Implement a Class that contains a Dictionary which maps messagetypes to ParserObjects. Every ParserObject might offer multiple functions, but those are the same for every messagetype. E.g. in other context parseBody and parseHeader.

This parserObject's method will then return some function which shall be called. This function is chosen by using some other class which also uses a dictionary to map the parsed message to an activity.

Both of those classes may take the dictionary/mapping as parameter or define it as abstract, so that the server and the client will have to initialize this themselves. Each once only.

Advantage: if something must be changed, it can be changed in the Parser or the Action, which is only at one place in the code.

## Connectivity timeouts

We want to notice

* if a client stops responding or loses internet connection
* if a client is still connected but did not play in time

For the latter case, we need a system-state specific timout counter. For the former, we need to either be periodically sending keepalive packets to test network connection or to ack on every message receive.

Periodic keepalive packets have the advantage of being independent of the rest of the game. ACKs have the advantage of only generating additional traffic when neccessary: We don't neccessarily care about a client being not connected as long as we have nothing to tell him. This would mean that we need to send ACKs on every message received that will be followed by a client-side action, e.g. after receiving a new state, before the player chooses what to do (because user interaction means delay).

To make implementation easier and cleaner, we would probably send an ACK after every incoming message though. For this, the sending of any non-ACK message would trigger a countdown timer within which the ACK needs to come in. These ACKs would probably not need to specify for which message they are, because TCP gives us ordering.

On the other hand, periodic keepalive packets would remain quite simple and detached from the rest of the server unless a client times out. This could be implemented in a single thread, while the ACKs would need interaction with the Messagepassing. I prefer the approach with keepalive packets, so we can completely hide and decouple them from the rest of the server.
They would have to be sent quite often though.

We could also just be happy with the timeouts the gamestate hands out, but then we wouldn't know if it's the player or the network which is slow.

My preferred action would be to have server- and clientside one additional thread which just sends a keepalive packet every 400 ms. Assuming a slow network connection of 1000ms ping, we can set the timeout to (network delay + network delay on way back + 400 ms if we have just recently received the last keepalive packet) = 2400 ms until we decide that the client is not responding.
We could also send the keepalive packet as response to a previous keepalive packet. Then we would estimate a maximum of 2*p for the way and no period time, because the peer should react almost instantly, as networking is way slower than calculating. (here, p stands for the maximum ping time to the server that we will allow) This way, we can tell after 2 seconds that a client has disconnected (for p=1000ms), instead of after the timeout until which a card would have had to be played (30 seconds or so). Also, we know whether it is the client or the player who is not reacting.

**Implementation**
Instead of calling the Message handling action, we capture `keepalive` packets directly after the call to `socket.readLine()` . If it is a keepalive packet, we directly respond. It is not worth handling this in a new thread, as responding is probably faster than creating a new thread, and a new thread might introduce new concurrency problems.
The keepalive message should be registered in the ParserFactory and ActionFactory, for consistency. This would allow the Serverlogic to register an alternative Action for keepalive packets if neccessary.

```json
{
  "header":{
    "messagegroup":"networking",
    "messagetype":"keepalive"
  },
  "body":{  }
}
```

We don't use `socket.setSoTimeout()` because that would constantly require incoming messages and otherwise close the socket. Doing that would work, but might entangle the code for the timeout more with the rest of the codebase than neccessary. Instead, running our own timer only between keepalive packets would be completely independent of the gamelogic messaging and the registration messaging. But: it would generate more traffic than neccessary.
If we reset the timeout timer also on any other traffic, we still fulfill our purpose of noticing whether the other is still there. So here's what we want to do:

**on any message received**

1. reset the `keepaliveInTimer` back to maximum (<span style="color:blue">KEEPALIVE_TIMEOUT</span>)
2. If it is a `keepalive` packet, capture it and do not pass it on to the gamelogic. Just parse it and probably do nothing.
   If it is a different packet, let the program flow as it did until now.

**on any message sent**

1. reset the `keepaliveOutTimer` back to <span style="color:blue">KEEPALIVE_TIMEOUT</span>

**when the `keepaliveInTimer` hits 0** 

1. The other party has lost connection. 
2. If we should do something, call the callbackFunction specified at the start of the connection.
   * Serverside: Inform all clients that this client has lost connection
   * Clientside: Change the UI such that the user knows they have lost connection.
3. Close the socket and associated resources (buffers or the like)

**when the `keepaliveOutTimer` hits 0**

1. We have not sent a message in quite some time (in <span style="color:blue">KEEPALIVE_TIMEOUT</span> ms to be exact), so it's time to send a `keepalive` packet.

**on shutting down**

1. Stop all timers

**on starting up**

1. Start all timers. Possibly with an additional <span style="color:blue">KEEPALIVE_INITIAL_BONUS</span> time to the timer to make sure we don't think we lost connection before we even started.

The <span style="color:blue">KEEPALIVE_TIMEOUT</span> should be larger or equal to the maximal ping we want to support. Setting it to 1000ms should be good enough: We would notice after a second that somebody had disconnected. We don't need to calculate the time it takes the packet to reach us into this, because even if the packet is still on its way, the peer should send the next if the timer ran out. This way, we only need to allow  <span style="color:blue">KEEPALIVE_TIMEOUT</span> between packets.

It could happen that the time a packet takes to reach us is larger than  <span style="color:blue">KEEPALIVE_TIMEOUT</span>. In this case, the first timer will already run out. That's fine.
Similarly, if the network is suddenly really slow, we will notice.

## Concurrency and Multithreading

Immer neuen state senden egal ob erfolg oder nicht, weil serverside immer was passiert ist.

Writelock & readlock
Wenn nur reads ->nacher neuen state senden obwohl alter requestet ist i.o. weil gleich.
Wenn writes von einer späteren message aber vor diesem getstate -> möchte zwischenzustand auch senden, also writes nur erlauben wenn schon state in History gespeichert. (History limitieren auf #players*2 states, mehr kann ein spieler eh nicht verpassen bis er wieder dran ist) Counter bei serverinternem stateobject um sie zu unterscheiden.
-> Client UI kann ablauf wie gewohnt darstellen, einfach schneller

WriteState nur möglich wenn eigene stack version == neuste version + 1

Für UI: bei jeder karte im stack mitschicken woher sie kommt für animation
-> könnte auch neuesten state senden und UI arbeitet stash ab. Aber was wenn man aufnehmen würde vom stash?