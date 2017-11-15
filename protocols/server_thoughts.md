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

## Concurrency and Multithreading

Immer neuen state senden egal ob erfolg oder nicht, weil serverside immer was passiert ist.

Writelock & readlock
Wenn nur reads ->nacher neuen state senden obwohl alter requestet ist i.o. weil gleich.
Wenn writes von einer späteren message aber vor diesem getstate -> möchte zwischenzustand auch senden, also writes nur erlauben wenn schon state in History gespeichert. (History limitieren auf #players*2 states, mehr kann ein spieler eh nicht verpassen bis er wieder dran ist) Counter bei serverinternem stateobject um sie zu unterscheiden.
-> Client UI kann ablauf wie gewohnt darstellen, einfach schneller

WriteState nur möglich wenn eigene stack version == neuste version + 1

Für UI: bei jeder karte im stack mitschicken woher sie kommt für animation
-> könnte auch neuesten state senden und UI arbeitet stash ab. Aber was wenn man aufnehmen würde vom stash?