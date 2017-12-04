# Initialization

As it turns out, there are difficulties that can cause the first message sent over our tcp socket connection to be lost.

(Any time I mention `RegisterSuccess` below, it could also be `RegisterFailure` )

Read the [Conclusion](#conclusion) first! This whole file turned out to be unneccesary.



## Current Implementation

The Server-side view:

1. Client connects to my serverSocket
2. I spawn a `socket` that is for only this connection
3. I spawn a new `EinzServerClientHandlerThread` that will be responsible for all incoming connections on this `socket` 
4. I run some initial code in this thread, e.g. setting up the parsers, and then am ready to receive messages.
5. Client sends me a `Register` message and I receive it
6. I respond with `RegisterSuccess`

The Client-side view:

1. User enters IP and port of the server
2. I spawn a new `EinzClientConnection` thread which might initialize
3. I connect to the serverSocket
4. I send a `RegisterMessage` 
5. I receive a `RegisterSuccess`

## The Problem with the current implementation

I will refer to serverside numbers with S# and cliendside numbers with C#. Above, you can see that we have only one synchronization point: S1 and C3 are more or less at the same time. Anything before them happens before, anything after happens after.

So as C4 happens, the server can be in any state between (inclusive) S1 and S4. If the server is still initializing (S4), the socket apparently drops the packet that the client sent at C4 seamlessly. After some long time, an Exception on the socket might happen. (TODO: handle this!) But otherwise, the client does not know what happened and is waiting for a `RegisterSuccess` message, while the server is waiting for a `Register` message. We're stuck.



## The easy fix

This has already been applied - maybe temporarily.

If the client were to wait until the server was ready, this problem would not occur. the server takes on average 200ms to initialize. So let the client just sleep for 500ms and everything should be fine *unless the server is really slow*.

The problem should not occur in opposite direction, because as of now, the client does not need to initialize something after creating the connection, because it is supposed to only have one of those and more importantly, it knows so in advance whereas the server has to react to any new client.

## Proposed clean solution

This solution assumes that the client does not crash after the first registration message has been sent and can still send a new one later. This probably requires the socket to restart if it fails after the initiation of the connection and before the receiving of a `RegisterSuccess` message.

The idea is simple: The server tells the client when it is ready. Whenever the client receives a ready packet, it will send a register packet. The server should return RegisterSuccess/RegisterFailure again exactly as before if it receives the same registration twice.

### Proposed solution X: In case the server initializes early

1. The client initiates a connection
2. The server spawns a new `EinzServerClientHandlerThread` 
3. The server finishes initialization
4. **The server sends a `ready` packet**
5. The client sends a `Register` message
6. The client receives the `ready` packet and sends a `Register` message
7. The server receives the `Register` message and sends a `RegisterSuccess`
8. The server receives the second `Register` message and sends a `RegisterSuccess`
9. The client receives the `RegisterSuccess` 

Here, X5 and X6 could be interchanged, as well as X7 and X8. X9 could happen anytime after X7, but this is not making a difference for my argument.

### Proposed solution Y: In case the server initializes late

1. The client initiates a connection
2. The server spawns a new `EinzServerClientHandlerThread` 
3. The client sends a `Register` message
4. The message gets lost because the server is not yet ready
5. The client might experience a socket timeout which has to be handled
6. The server finishes initialization
7. **The server sends a `ready` packet**
8. The client receives the `ready` packet and sends `Register` again.
9. The server receives the message and sends `RegisterSuccess`
10. The client receives the `RegisterSuccess`

### Proposed solution Z: in case the server intializes at the same time as the client sends its `Register` packet

1. The client initiates a connection
2. The server spawns a new `EinzServerclientHandlerThread`
3. The client sends a `Register` message
4. **The server finishes initialization and sends a `ready` packet**
5. The client receives the `ready` packet
6. (The server receives the `Register` packet)
7. The client sends another `Register` message as reaction to the `ready`
8. The server receives the `Register` packet
9. The client receives `RegisterSuccess`
10. The client receives `RegisterSuccess` again

Here, Z9 might happen before Z8, and Z6 might happen after Z7, but this does not matter as the swapped events are not on the same side (server/client).

## Analysis of the proposed solution

This solves the problem with just two more messages at worst.

Changes to be made:

* The server needs to send `ready` when it finishes initialization
* The client needs to send `Register` instantly when it finishes initialization
* The client needs to parse `ready` and react to it by sending `Register` again
* The server needs to make sure that it responds with the same result if the exact same registration has already happened. Alternatively, the server could in this case also not respond at all, because we can assume that the response arrived reliably at the client or will soon. (*Which is better?*)
* The client needs to handle the case where the first `Register` message times out within the socket.

If for some reason, the client were not ready when it received the `ready` packet from the server, it will still send a register message. If the server was not ready when the client sent its first register, it will still receive a register message. And if the messages crossed in the network, the registration will still succeed. Thus, the proposed solution is correct.

If the client decided not to register itself, it shouldn't be sending messages to the server anyways. So assuming a correct client, it will never be registered if it doesn't send `Register`.

* If the server went down during this process, the client would time out after the second `Register` timing out. TODO: make sure this timeout works. (But it probably causes a IOException, unless the server only goes down after having received the register message)
  But this works the same way as when the server stops unexpectedly later in the game, and will be handled later by constantly sending `are you still there?` messages anyways.
  (This makes the argument that we only need a constant number of messages kinda irrelevant)
* If the client went down during this process, we should also be able to rely on our `are you still there?` packets that we intend to implement.

## Conclusion

**Actually, all of the above is based on wrong premises**.

I have now tested the old version (at tag bug1/initmsg) again and it turns out that it does not always work in the hotspot router model either. I originally believed that the server did not buffer messages during initialization. This is wrong: making the server sleep for 30 seconds during initialization does not make a difference at all.

My original belief was that the registration process was consistently working in emulators and in a scenario where one device was also the router, but not if both were connected via a router. This belief was based on insufficient testing - actually, the registration also fails sometimes in the hotspot scenario.

The actual problem was that the client tried to write to the output buffer before that was set up, thus losing the message before it even entered the network. That making the client slower helped makes also sense in this case. Why it consistently worked in the emulators could have to do with the fact that setting up the connection on localhost is so fast that the other thread which sends the registration message is very unlikely to be faster.

See the code of EinzClient and EinzClientConnection for this, but basically the flow of the program was like this:

1. start connecting in a background thread
2. send the registration message

If 1 completes fast enough, 2 works. It was intended that 2 would spin if the connection was not yet established, but this was a one-line bug in bug1/initmsg. In the next commit, 5b2c455, I fixed this 'accidentally' in the belief that the case where the output buffer was null had never occurred yet.

**So we don't actually need this initialization protocol.**

The bug seems to be completely fixed now at tag fix1/initmsg

***

<sub>Eric Mink - 01.12.2017</sub>
