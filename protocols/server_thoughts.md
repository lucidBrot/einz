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

## Messaging

Implement a Class that contains a Dictionary which maps messagetypes to ParserObjects. Every ParserObject might offer multiple functions, but those are the same for every messagetype. E.g. in other context parseBody and parseHeader.

This parserObject's method will then return some function which shall be called. This function is chosen by using some other class which also uses a dictionary to map the parsed message to an activity.

Both of those classes may take the dictionary/mapping as parameter or define it as abstract, so that the server and the client will have to initialize this themselves. Each once only.

Advantage: if something must be changed, it can be changed in the Parser or the Action, which is only at one place in the code.