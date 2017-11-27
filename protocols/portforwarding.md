## How to use multiple emulators and connect them

Every emulator is behind its own virtual router. In order to connect them, you have to forward some ports.
This file is a collection of the relevant information to achieve this.

https://developer.android.com/studio/run/emulator-networking.html#calling :

> ## Interconnecting emulator instances
>
> ------
>
> To allow one emulator instance to communicate with another, you must set up the necessary network redirection as illustrated below.
>
> Assume that your environment is
>
> - A is your development machine
> - B is your first emulator instance, running on A
> - C is your second emulator instance, also running on A
>
> and you want to run a server on B, to which C will connect, here is how you could set it up:
>
> 1. Set up the server on B, listening to `10.0.2.15:<serverPort>`
> 2. On the B console, set up a redirection from `A:localhost:<localPort>` to `B:10.0.2.15:<serverPort>`
> 3. On C, have the client connect to `10.0.2.2:<localPort>`
>
> For example, if you wanted to run an HTTP server, you can select `<serverPort>` as 80 and `<localPort>` as 8080:
>
> - B listens on 10.0.2.15:80
> - On the B console, issue `redir add tcp:8080:80`
> - C connects to 10.0.2.2:8080



> #### Setting up redirection through the Emulator Console
>
> Each emulator instance provides a control console the you can connect to, to issue commands that are specific to that instance. You can use the`redir` console command to set up redirection as needed for an emulator instance.
>
> First, determine the console port number for the target emulator instance. For example, the console port number for the first emulator instance launched is 5554. Next, connect to the console of the target emulator instance, specifying its console port number, as follows:
>
> ```
> telnet localhost 5554
> ```
>
> Once connected, use the `redir` command to work with redirection. To add a redirection, use:
>
> ```
> add <protocol>:<host-port>:<guest-port>
> ```
>
> where `<protocol>` is either `tcp` or `udp`, and `<host-port>` and `<guest-port>` set the mapping between your own machine and the emulated system, respectively.
>
> For example, the following command sets up a redirection that handles all incoming TCP connections to your host (development) machine on 127.0.0.1:5000 and will pass them through to the emulated system on 10.0.2.15:6000:
>
> ```
> redir add tcp:5000:6000
> ```
>
> To delete a redirection, you can use the `redir del` command. To list all redirection for a specific instance, you can use `redir list`. For more information about these and other console commands, see [Using the Emulator Console](https://developer.android.com/studio/run/emulator-console.html).
>
> Note that port numbers are restricted by your local environment. This typically means that you cannot use host port numbers under 1024 without special administrator privileges. Also, you won't be able to set up a redirection for a host port that is already in use by another process on your machine. In that case, `redir` generates an error message to that effect

The emulators port number is visible in its window top bar.

![emulator_portforwarding](N:\Files\Schule\XCubby\studium\DS\dontsync\einz\protocols\emulator_portforwarding.jpg)

(On the left the server, on the right the client. The client did not need any redirection)

Here, I set the server up to listen on port 8080 on its own IP (10.2.15)
The client connects to my desktop PC which is always 10.0.2.2 from the emulator.

My desktop pc redirects incoming tcp traffic on 8080 to the server emulator, because I set this up in telnet.

Note: Telnet requires authentification. Copy the token from the file it tells you.

```bash
$ telnet localhost 5554
Trying 127.0.0.1...
Connected to localhost.
Escape character is '^]'.
Android Console: Authentication required
Android Console: type 'auth <auth_token>' to authenticate
Android Console: you can find your <auth_token> in
'C:\Users\Eric\.emulator_console_auth_token'
OK
auth 9/DO11I8NEPn1Yqv
Android Console: type 'help' for a list of commands
OK
redir list
no active redirections
OK
redir add tcp:8080:8080
OK

```

The redirections are automatically undone when you stop the emulator. You could also use `redir del tcp:8080`.