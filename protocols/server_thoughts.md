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