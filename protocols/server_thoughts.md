## Serverthoughts
Thoughts, considerations and ideas that came up during the development of the server.
***
## Threads vs Services
If the server runs in a thread, it would be paused when the Screen is turned off. if it runs in a service, it would not run in parallel, unless in a thread itself.
So we will run it in a thread and be ok with it being possibly paused if the user turns off the device screen.


***
## Sources
[Example TCP Server](https://stackoverflow.com/questions/10131377/socket-programming-multiple-client-to-one-server)