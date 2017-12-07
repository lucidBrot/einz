package ch.ethz.inf.vs.a4.minker.einz.client;

import android.content.Context;
import android.util.Log;
import ch.ethz.inf.vs.a4.minker.einz.Globals;
import ch.ethz.inf.vs.a4.minker.einz.UI.LobbyUIInterface;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.KeepaliveScheduler;
import ch.ethz.inf.vs.a4.minker.einz.keepalive.OnKeepaliveTimeoutCallback;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessage;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.EinzMessageHeader;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzKickMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzRegisterMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.messageparsing.messagetypes.EinzUnregisterRequestMessageBody;
import ch.ethz.inf.vs.a4.minker.einz.Debug;
import ch.ethz.inf.vs.a4.minker.einz.server.ServerActivityCallbackInterface;
import org.json.JSONException;

import static java.lang.Thread.sleep;

public class EinzClient implements Runnable {

    private final EinzClientConnection connection;
    private final boolean isHost; // true if admin
    private ClientActionCallbackInterface actionCallbackInterface; // handles the methods that an action might call (as reaction to incoming message)
    private ClientMessenger clientMessenger; // handles incoming messages
    private String serverIP;
    private int serverPort;
    private Context appContext;
    private Thread clientConnectionThread;
    /**
     * true if the client was shutdown (but apparently still exists)
     */
    private boolean dead;
    public KeepaliveScheduler keepaliveScheduler;

    public String getUsername() {
        return username;
    }

    private String username;
    private String role;
    private final LobbyUIInterface lobbyUI;

    /**
     * <img src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxMSEA8QEBIQFhAQEA8VEBAWFQ8PEBAVFRUWFhUVFRUYHSggGBomGxUVITEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OFRAQFy0dHR0tLS0rLS0tLS0tLS0uLS0rLS0tLS0tKys3LS0tLSstLSstLSstLSstMCsrKystKystLf/AABEIAMcA/gMBEQACEQEDEQH/xAAbAAACAwEBAQAAAAAAAAAAAAAAAQIEBQMGB//EADgQAAEDAgUCBQIEBgEFAQAAAAEAAhEDIQQFEjFBUWEGInGBkRMyFCNSoUJiscHR8PEzcoKy4Qf/xAAZAQEBAQEBAQAAAAAAAAAAAAAAAQIDBAX/xAAzEQEAAwABAwMDAgMHBQEAAAAAAQIRAxIhMQQTQVFhcSKBobHBBRQyQpHR8DNScoKSI//aAAwDAQACEQMRAD8A5WWmiQBQEIFBQABQOEBCCT3bQIQRQJAwEBCAQNAICUBKBSgLoBAkBKACBygSBygRKBhyA1IAFBKUC0oCEDQIlASgSAhAi1EGlFOEAgUoEgIQEIBAIBAQgNKAhA4KAQEIGgAEAgNKBQgUIEQiBFdSgUIBAQgEAgJQKUTRKKaAQEoEgEAgIQKEBpQMBA0AgSAUEXP+FRBuKYTpDgSBcBBMVB1QS1IglFNAIBAICUBqQIlASgEBCBQgSIaKcICEAEDlAkAgIQRjugEBZAWQVMyzGnRbqed9gLkpEIyMT4qZoBpiXuJsdmidyiqNPxFW0uA8zzeYswdYVwUMZntRwAk6huePhBQo4+o1xcHGTudpVxGnkmaVPrtAl2qxE2jqVJXHuG12GfM229xZQTEIiUIBFEoHKBIEgUIGgJQEoCUDlAakDQKEAgSAugV0BpQEIgLUAWorPzLHilpGkuc6bDgdflXB47MaVaq8ufcnYcNHQK5Kat5fkBialufZaiqasVMsb9rCQDYmbu7eiYao1snA2KYus3E4UtlZlVejWj7ZBvJncdFPlVn8c6AOJFgiPaeH8a97SXxoAaGk2JSUbQcFA5RTQJBFASgJQOEChBLSgTTM9jCzFt115eG3H07/AJoif9ThacggRdcDqszbJiPq6U4pvS9o8Vzf3nDlacwgEBKADkCIQVsZjqdMed3sFcFJmeUj2A5JAWZ0WMJmVKo7Qx0u3iCrWJlHDFkE9V1iERoUmganc/b3KqCq8mw2QcvpQLWCCs8dLqNQzMXhvqEtExyf7LOClissDBabBMWGaaRF4PuFldWcqaH1qbajnBkibn4hEl9LYBAAEAbKGJQiANQEFFGlAwEDhABA0A4oKoN7/bqPzxK80T+rv419y9azwx0d+T24/wDnvuff+g1G4v5CT69viVO8T/491rTjtStpiN5oisfaYiY3/XD1fbOx1Hp6BWP8vV4nWLfp932Y/VTprHbe0drTH7olxtH6naZ9Fn9XbPrOO0V4v/093ttadefXqj/ku1I2/r1ld+POns+T62LRzW6oiPpnjPjEoK6PKSBaUQOcGiXEAdTZFWcowzsSXCgQ4N3dcNHvymDMzH/88ruqF1SqACbBrXVPm62au0fAjKLRqY57pu8jUD7A2ViE0hkzKOotp6CRBMELQptwwnff9uqIlVwgJGk7DZBMNawcE88oRDgXzsEMZmLrQ4t2LvcrOtF9IAWsFpHP8IDd1+imEq+Y4caT6LMil4ZwzDWJcJLfsBs073JXK2x4ae2a8EdFIt9RIBWJ0wwVpDDkRIFFOUAgECKCJQACmQvVP1OUxNlE+iZCxa0TsTkgJh1T9TEokzMiCqg0oLRyeu5oNNhl2xNgO5CuDZyfwcwAOxZbWqdCPI3sBytRBr1WEoMptDKbQ1g2aAAAqa6PciOT+yaPP+KabalBzDq1C7SHFgB4JINx2UmVh4LLM5pkGnXfLmOgAkAm+7H2n0KRKrFaoxzqwYYc0A6f1MP8Te02K1CKxbJiSLWMEg/CBuqCIbM/7sqKNWz7yHEb7lZxU2gm7v7LQ6zINrx8KTKSw82xJMtplxdsYiB7rEzqsNtCq0yQ6et1MVdo5hUbAg+XYSf6KTCN3Ka2IqODnNIZNyfLPopkD0rXICFUOUUSgEDCAJQRLkCAQS0ogQKUVawuAq1PtaY6nyhXCWpQ8Ond7x6N/wAq9I1MDlNJhkCT1deFchJasohB1kgJlSyokKqghicWGNc6NhYdTwqsPM+I8S5jCWNa95jyG4AjlFfMcbgTUD3tpllRoLnsIMETuzqs4MzD491Oox4J1Nib/wAPLfhI0ehzPOmfUwz2D8qpSMt2LHAwR7K6L7Kpc1pBhhB0WGoStIqvpgHc6uSbkqaajXrgNcZHlG0wT6KTJEqFWtUqQ1h0zvF3Ef2Uab+T+FKrhOnS3lzt/WE8JiWPySg0lgeajwPM4WYz179lnriRXwWSUqdwNTv1G5QaICgkAqiQQEIohAIFKAAQMBB0pUnOMNBJTBepZLUduWj3la6TFyhkjB97i48gWAVio7sdhqZiWCPcrXSLtPM6RsKjO14SUlYbWbwQoQ6teiugKI5B1yEgwnOhEdKIkSgp4k6qjREsAcSeJ4Ua8Kr8MA7UC6+8mQrBrH8X4Oq6iKmHJFSidQDf4xyD7JJD5NiqBIdVa1wYT5jBIa47iVkGXYd1SWj+EyPf/hCG7l2PcxzsPV+6mNTHHkG8d01HbGZkLudcxa0N9EkxkAGo6Xar7HZIget8I4qlhm3w5qVdR/MLhEeh2UtMx4WJa2ZZ7XrDT5abP0gkn5WJ2VZlOgB1PbYfCRXE13AWhMIhkoqMoHqQIoDSgaAKC/gsuJhz7D9PJW4qNqhSawQ0QP6q4ny6HEACSjWsHMMfqOlrvLueJKakqDa4PpcepTqCc8HspMoKOKe2zXn5t8JEmNXA564Wdf8AZXRvYPMGPG9+nRJVeI5HypAjVdAAF3H4Co5AEDnuiIF5FgEXHMnqiQg/si4z8XkdF1KowU2gVB5wBAJ6+qhjwmEyf8LUqtf+qmQf1MkwqMnxdUAxVOq0+V1NgtxpcQ6/usSrhVxdAmGBwaD5dRkmRcmFdhJhrYFoc0aSxxtsCIWmZbVKlAAt/Rc2nUBAIJNQMoFCIYCA0opygUoFKI0Mrwmrzu2B8o6rVYVslwFyVrUZebZwKbYZd3J3ARHmnYp9Qy9xjok+FZuGxb6Zf9YtP5jnAB2zeAueukO9DMwSQGw6QbmQ4doUiyzVpYdxcQ0iDBLgd7cKzLMQx8vzV7sQ+k+nDQTB0uaW35J3UiVl6Bgt3v6ro5yi8kEPaSOD3CurD0OT5oR5XGW/0USXopBgjkI0WyJiDmztZFcHA+qIhMX4RQ91rbEBBjZphGVd7O2aebXhaR4PxPlWmnpuXNB67lzb/ErFoHjnM8+m+8Qsq9n4e1FoBiABHUevVWZZmG80KKkglAQFkQAop6lERJVUEohIoQdKLdTg0clIRuVKgpt2sBAXRXnM0zJwETZPAw2vc8yTYcKJi6x0CeSkyMbOG/SqkMIeHhpLzBEkS4DouVnSOzvg2sOzW6pH8o9UhZtrapOMguknjkt7LcMas1m6gTPIPwmQmuAJDhJEE8brSRC7UA0xxCNIYSpf/eESIexyaqTTvuDHspKtAKjjUbOyg5AKjm5khw5Isg467tHI3UHjfHWNqUn4eqw/lUqodUA3naD2glBdxbm1WNqtuHtaQNy4dk1Hla/hZ2tznFrQ8yLEkA9lnFa+W4YUmad9rkAILJKISBgoGCgEQLMhqwoVBCBEoOuAqQ+exSAZ7mgDdDd5F+FseUqVnPdfZRF+hQt7KwOr2gXmY42RVfEMD5kXH2nhvUhYmF0YJrLOc3ixHokQa0S9p+0d+4KqSTqhiCqiUSRbZNVN9W0CU01PBk9ENes8Ov8AKQeqkrDbCK51DCDiW8qo5vsqKGIePMTZwO3QIPK+JGNfQrM31i3Weqzaewh4YaadOkKgMsbZZrAvY2rqcT8BVlUIRQAiCEU0DCBwgFECQEqoQAhDHLEnTDwdpluxNlmPI8ziMdrJjqbchdDAx+mD/wAqDocyjf4VB+MBshiD8RaCbW/qiO9KtI/spg70cRHPtygtU8TN91R3p1d+nCiw5CvJ9Nyhi5l1cz/KeVYV6bKcRpcDwbFB6JpkKKidoKDi53HPARFaPMZn+09lBieJKmgB8mZiOvZSbYrEwzPqeeptwFI7otOrRtG0ei1qK5KiEqCUDBRQgcoBAIhwilKBQgUIK+LENcYkxYIPKV7OJmCTsiuFSpuQqONME3IBREcTUc0dLiOUlVwVfLE3j1lEQw2JIMOnUOvdDs7DGb9fhDFjC406bmD2QXMPiS4nfa3RQWKQB3+FVhqYV4EDhBt4F8Ed0HrKB8rfRRSqPDQS4gAcoMllc1X6qZIYGkO6kjt/dY1qHSvi2U2y5wAHUp1RCMDNsfh64gO8wPQx7KTbUlmlwEAGQ0QOh7rcMySqEophUMIGiBAIBFMIhFFJAwUAUHGs2QR1RdeazHKKgdqb5m3m4keykyMqq0iQ4Qehmy1Ejj9ThN00/olzgLTCg7AFki09P8IIPqkSTF91cTEWCwJnhRFqjEXTVhfwLoagtMqXF5P7hCFx+Pa2A4gSLE2TVW8F4iotIbq80iIu35U0ezq+JKQb5XXAFocEmVZWOz6gQS6oSZENEx3BCzNlhi4nM2Oqh1B72gAzBLdXSy52accRU13c4n1KzjKFOSYbt14W6xKSuNXWGASqGEU5QCBoFCIcIpwgJUCVIBCIRRQCiHKKg9nyoMDxDgHObqAFvuG0hS05Cw88cHHNum65e41hGiQZaR6HdbreflEK9QzMiQItdXqRxc9xnm3NpV6wmazaCYuOydUGa60Kbr6iB73+FmZMdn1CYGqO26dau1F75nUfYEKe4EcM5wOoz7yszyGNnA5EG0KWJ1fdWezTG2hrXTPfUvPT1PVz24s8RE7+Vztr1GOoBjaMmfq0mVAI21EgD9lrh9THL7nbOi01/wBPkxGt4Wd9SoyoHBwwzqtPS0nWYaQ3bfzQY5Xjn+0+K1a3pPab9M78ff8Ag10yyjlVWm9tI0n63RpYWuD3T+kc7L119Tw2pN4vE1jzO9h2OV1g8MdTe1zo0sIOp3SBz7LXHz8V6Tet4mseZ3x+WZ1r4XJaxeykabmucYBc1zW9zsscn9o+m4+K3JF4mKxvbynTMzjkzDEVRTe14OsNc1o/M34B5Xe3PE8M8lJjM2Jnx+6Z3xBtIkuDQ4xqJEXAG5I7LpPJWtYm0xG5+NlEmYZ50Q1x+pOix88WMddipPPxx1baP0+ft+TE24OoWGoKbywbv0u0+s9FifVcMX9ubx1fTe65JNwdQsNQU3mmJ8+k6bb36Kz6nhjk9ubx1fTe6ZPlGhRc86WNc5x4AJK3y8tOKvVe0RH3IjVvGZa6mKdn6nUnPqNIgshxabdLD5Xj9P66nLa/eMi0Vifr2iWprmKraDzohrjrnRAJ1xYx1XrtzcderbRHT5+35Zw8Rh3sOl7XNMTBBEjqE4ufj5Y6uO0Wj7ExMeXNdUJFEoglFwkAUEURCrTDhDgCFJjVZmJyZpuwx23C5zxtaw8ZlrmOIceOFi04qo/At7rPUJnDA7jbbqpEhtpuB3kfurojVwpdstxZMdcNhCFm0rELQprKysUaCsQmvS5dSbWwjaIfTbUp4io/S9wphzXsaJBNrFuy+febcHqp5ZrM1tWI7RuTEz5j7tR3hLO9LjRYxwcKOHpUy5s6XObqJ0nkXiey6+hretb2tGddptnzk55SZatWu0PqVBUZ58uDGw4ag9tOm0tI3BkFfO4+G80rxzSf08uz27Zszv4a6oGAxDfp4ZpeA/8AD42lrJ/6TnuGgk7tET8rpzel5J5OW0U2vVS2f90RHfCLR2WMDWZSGHpPqs1N/FfmBweyj9RmlnmH817bSnPw8nP7vJx0mKz0dpjJt0zs9vx2+6RMRmumDIpMwZfUYQ3GvJc12tjRoZPm27+6xz76jk9T7dJiZ469pjJ3Z+CO0Rs/LLwEMxdIuczS2uwueCHMjUDOrovqepi3L6G8UrOzSYiMyfH0YjtYsBiQzEteY0fUcHdCx0h37ErXqeCeX0k0jzkZ+Y7x/JInLNV2OpNbVa1wnCtLMKf16qf0nEf+Uv8AdfJr6Pntbjtav/WneT7ZPVH8P0/s6dUd/t4Sy+pSb9FxqUyPw7muc+o/6jXFrpY2nMBsxuFPVU57zyVilt64nIrGTETH6pt538Fc7OGEqNNKmX1KY0UHtD2vdTrss78s07h4JMTGxXbm47157RSkz1XicmIms94/VFvic+N8pHhTyh404hmprHVKQDHOOlshwJaXcSAvd/aFLdXDydM2rS2zEd/iYic+0s1+YaIrs/LpGrTLjgq1Iv1SwPLyQ0u6d182eLk/XyxxzERy1tmd+nM3G98RvwVKqxjaNM1aer8Pi6etrpbTfUdLZcNrSJ7rV+Lk5L8vL7c51UnJjvMRHft/RO0ZDLxrC1tNhqtfp1w1p1tpyRbWN53jhfW9NaL3veOOa7nee0z+32Yn4jVRexAUCKBIBAIEgSAKhrLzbCF0vHAuP8LjyVbiWMaa5htpoSk2mg6fT6IOtKgXEAXWojU1p0cvA+656cLrFE1bYwAQAtZH0TQWDoEyDQGp0mpAK4JQhqVGoWuDmmHNMg2MH0Kzelb1mtu8SRLrisY+pp1unTOkANa0TvAaAFy4PS8XBvRGb57zMz+8rMzLgu6AqgCgaAQNVAooRAqGiiEEUAgYat04r3/wxuOPLz8fFnXaI36iFm1ZrOTGS3S9bx1VnYJRrYjyAFa1m05DN7xSNkoSKzO58FrxXN+eznXp6mub1BCxaNh0h5qu0tc5p3BXlltKk0nUREN3uPRarWbRMx8MWtFZiJ+XVlMukjhpJ7Ac/urWk23PiN/ZLXrTOqc2cj8lTOogN9FIjW57NnCYYM/7o+F3rXGJlZWw1EEKghAkU0BCAQCBoGAgEAgSBogKKAhgQNAkAgm3Yz1C9XD0+zfq3zHj93zvUdf954ujN6bef/VIj/1su3JWtomfjo7fXs8/Fe1Jiu5PuZbPHeN7fb+pMERYfa5XgiK9M53mtv6p6qbck3ibTlb0z+CFMSfleX09YvyZMdsn+T3ervbj4oms99rH8Y/mGDf0KnBWLdex4rLXqr2p7fTObaI/bukQI4sL8ELvelfbmK1jYje+xP5eSnJeOWJvacm2RMZNZj6Z5if6vOlzT+JqaWuLXM0zJAkkGy8HD0xx8l5rEzHTm/eZev1E2nm4uOLTWLdW59sdX0Gw9waATRpv0ifIS4Ax/vK9F+Gk1taK5tazn0mZebi5+SLUrNty9q79YiN7uzhBxFMMENpOAcAdR+3c8yusxFbc3HWsREVnv8/H83KJtevp+W15mbXjY+Pn4+y5hsE1ltLfK2QY8xOm5J9SVuOLjraePojIru/O5unu8k8deb3J214jO2Z1ZmfhbIAt/L0Mz6rV6cNK9M543xO7m+fBx8nqOS03rv8AizzGZE5medwReIG2/O0yp7dOv2unt07vzubp73J7fv8AXO9WdPxnVmZ+O+m1ologXbPfZWtadVKdMfqru/O4XvyRTk5euf03yI+M3HFfOfYNUCAQCBIGUDQCBIGECQCBoBDTCAQJABAwSNit05b03pnNceX0/HyzE3ruDUet1fd5Orq6p1P7vxdHR0x0/Qaj1T3uTYt1d4T+68PTNOmMnz+wWItaJ2J7us8dbV6Zjt/sQKRaa+PlbUrbNjc7/uTnmCL7bK35uSaTXq7OdfTcUX64rG/8/ixsDQqAVQ0FpcWw422JJHfdY4eS3HS8VnJtnf8ACc/pq83JS1u8V3t+VgYB/mJf5ngB3MxsFJnkmZmbbvl0rxcdYrFaxHT4+y1h6LhGp7jAiOP/AKunu8mdM2nPDnHpeGLdcUjd391oVLRfaOIXpr6rpp0xs9s7+P8Ad5beh6uWLTFYjd7R3nP4flHUdpsuEct4r09XZ6p9PxTfrmsb9RqMRNk97k6enq7H924uv3OmOr6/f6/n7ie/os+5bYnfDXs8cxNc7TOz+SWXQIoRAihA0CQCAlA0ChA4QCBgIBAIghFJA4QEIBAQiEgEUKIEUKgQACAhA0ChAICEBCARAihAIBAIBAIAIGgFA1QkTAgEUIBAIBAICUAgSAQCBoBAiommqpIGoEVUEIaIQCKIQCAREoRcJDBCIcIaSgaqnCBQg//Z" alt="bamboozled again" />
     * Creates a Client which offers a run() function. This function will establish a connection to the server, doing so in a new thread. For this, it is neccessary to run that function in a new thread in case waiting for connection takes forever.
     * This class only implements Runnable because it can, not because it must.
     * @param serverIP the IP to connect to
     * @param serverPort the Port to connect to
     * @param appContext the ApplicationContext from getApplicationContext()
     * @param username the desired username. The server might respond with a registerFailure though
     * @param role the desired role. Currently this could be "spectator" or "player"
     * @param isHost an indicator whether the server is running on this device.
     *               The first client to connect to the server is the admin. We hope that this will be consistently the same device, because of the network delay.
     *               isHost is only used to decide when to send the registration message
     * @param lobbyUI the implementation of{@link LobbyUIInterface} that should be called to update the UI
     */
    public EinzClient(String serverIP, int serverPort, Context appContext, String username, String role, boolean isHost, LobbyUIInterface lobbyUI) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.appContext = appContext;
        this.lobbyUI = lobbyUI;
        this.actionCallbackInterface = new ClientMessengerCallback(lobbyUI, appContext, this);
        this.clientMessenger = new ClientMessenger(appContext, this.actionCallbackInterface, this);
        this.connection = new EinzClientConnection(serverIP, serverPort, clientMessenger, this);
        this.keepaliveScheduler= new KeepaliveScheduler(EinzClient.this.connection, new OnKeepaliveTimeoutCallback() {
            @Override
            public void onKeepaliveTimeout() {
                EinzClient.this.connection.onKeepaliveTimeout();
            }
        });
        this.username = username;
        this.role = role;
        this.isHost = isHost;
        this.dead=false;
        Log.d("EinzClient", "Finished constructing this instance ("+username+")");
    }

    /**
     * Starts receiving(spinning) EinzClientConnection in background thread
     */
    @Override
    public void run() {

        /*
        How to use this class:
            EinzClientConnection handles connecting to the server and receiving packets.
                It features a method sendMessage() which can be used from different threads to send a message to the server
            Once a Message is received, the corresponding parser is looked up through the parserFactory by checking the mappings
                specified in the files R.raw.initial_networking_parsing_mappings and R.raw.initial_game_logic_parser_mappings.
                These files are not only one file because you might want to separate the networking-related messaging with the game-logic related.
                However, you could technically specify any parser mapping in either of the files (If you specify it in both, we'll see what happens, I think it just keeps one)
            The thus found Parser takes the message and returns it as a Java Object with the content as variables.
            This goes through the actionFactory which is mapped in R.raw.client_initial_networking_action_mappings and R.raw.client_initial_game_logic_action_mappings
                those are separated from the server's action mappings because while the server does not receive different messages than those specified in these files,
                the reaction might be different.
            This action has a function run(). You should implement this function in the corresponding child of EinzAction, found in messageparsing.actiontypes
                Within this run function, you have access to an interface of type ClientActionCallbackInterface, which is implemented by ClientMessengerCallback,
                So you can implement any functionality there and update the interface, then use those functions in the class.
            This Client is instanciated once the server is up, called from lobbyactivity, if the device is hosting it, or when the user entered the address of the server and the port and confirmed if it's a client-only device.
            The LobbyListAction offers what the LobbyUIInterface provides to update the view. (Feel free to change that as well, the LobbyActivity is the only class implementing this)
            This client should implement some functions that allow the UI-thread to make this client send a message (in a different thread), e.g. when the UI realizes that the host wants to specifyRules or StartGame.
         */

        // <debug>
        // fake receiving new lobby list
        //debug_faceReceiveUpdateLobbyList();
        //</debug>

        this.clientConnectionThread = new Thread(this.connection);
        this.clientConnectionThread.start(); // establish connection
        Log.d("EinzClient/run", "initiating connection in background");

        if(this.isHost) {
            Log.d("EinzClient/run", "server is up methinks"); // if server is running on localhost, it told us when it was ready to accept connections
            // still need to spin until isConnected to make sure we do not send register message before connecting, thus losing that message
        }

        this.keepaliveScheduler.runInParallel(); // run the timeout timers in background

        // send messages in background because android does not allow networking in main thread
         if(!isHost){ // if the server runs on the same device, it will tell the client when it is ready to receive the registrationmessage, and will execute onServersideHandlerReady
             spinUntilConnectedAndSleep();
             sendRegistrationMessage();
         }

        // TODO: all other messages
    }

    private void debug_fakeReceiveUpdateLobbyList() {
        this.clientMessenger.messageReceived("{\"header\":{\"messagegroup\":\"registration\",\"messagetype\":\"UpdateLobbyList\"},\"body\":{\"lobbylist\":{\""+username+"\":\""+role+"\"},\"admin\":\"this is a debug packet\"}}");
    }

    private void debug_fakeReceiveRegisterSuccess() {
        this.clientMessenger.messageReceived("{\"header\":{\"messagegroup\":\"registration\",\"messagetype\":\"RegisterSuccess\"},\"body\":{\"role\":\""+this.role+"\",\"username\":\""+this.username+"\"}}");
    }

    public EinzClientConnection getConnection() {
        return connection;
    }

    public ClientActionCallbackInterface getActionCallbackInterface() {
        return actionCallbackInterface;
    }

    /**
     * starts a new thread and sends the registration message from there. username and role as specified when this client was constructed.
     */
    public void sendRegistrationMessage(){
        (new Thread(new Runnable() {
            @Override
            public void run() {

                /*
                // the following bugfix is no longer needed because the server tells the first client that connected when it is ready for the register message
                // other clients are hopefully slow enough, else they would have to wait or get a response from the server for this...
                //<Bugfix>
                while (!connection.isConnected()) { // spin until connected
                    //sleep(10);
                    // wait for server ready. it works if you put a breakpoint on the line with "new Thread(", so waiting should help
                    // this sleeping doesn't seem to help. sometimes it still doesn't get response of the server even after sleeping 1000000, or 10000. Seems to work with 1 and 10 ms though
                    // BUT: why is this the case? And why does it only sometimes work?
                    //      below sleep was added after this comment
                }

                // sleep a little after the connection is there, somehow this helps. If this is not there, the message is lost before the server is fully ready
                try {
                    sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //</Bugfix>
                */

                // example message sending. implement this where you like
                EinzMessageHeader header = new EinzMessageHeader("registration", "Register");
                EinzRegisterMessageBody body = new EinzRegisterMessageBody(username, role); // getting all the girls
                final EinzMessage<EinzRegisterMessageBody> message = new EinzMessage<>(header, body);

                //DEBUG
                Log.d("EinzClient", "Trying to...");
                connection.sendMessageRetryXTimes(5,message);


                //simple logging:
                try {
                    Log.d("EinzClient/run", "...send(/t) register message: " + message.toJSON().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // DEBUG: trigger kick failure message
                //<Debug>
                /*
                try {
                    connection.sendMessage(Debug.debug_getFailingKickMessage());
                } catch (SendMessageFailureException e) {
                    Log.d("EinzClient/DEBUG", "failed to send bad kick message :(");
                    e.printStackTrace();
                }
                */
                //</Debug>
            }
        })).start();
    }

    /**
     * this only spins until the socket is designated connected. The tcp connection might still not be initiated.
     * This function is currently not needed if you're the host, because the server will then inform you via callback when it is ready.
     */
    private void spinUntilConnectedAndSleep(){
        //<Bugfix>
        while (!connection.isConnected()) { // spin until connected. sadly, this can only tell us that the socket is connected locally, not that the client actually is connected
            try {
                sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // wait for server ready. it works if you put a breakpoint on the line with "new Thread(", so waiting should help
            // this sleeping doesn't seem to help. sometimes it still doesn't get response of the server even after sleeping 1000000, or 10000. Seems to work with 1 and 10 ms though
            // BUT: why is this the case? And why does it only sometimes work?
            //      below sleep was added after this comment
        }
        Log.d("EinzClient", "socket thinks it is connected");


        // sleep a little after the connection is there, somehow this helps. If this is not there, the message is lost before the server is fully ready
        // this helps because above while loop ending does not mean that the server is ready, only that the connection is said to exist when socket.connect() has been called
        // EDIT: that's kinda wrong. it might also be the client who has not yet initialized the buffer, but that was a bug in checking if it was null
        if(Debug.CLIENT_SLEEP_AFTER_CONNECTION_ESTABLISHED) { // for debugging, no longer needed because of sendMessageRetryXTimes instead.
            try {
                sleep(Globals.CLIENT_WAIT_TIME_AFTER_CONNECTION_ESTABLISHED);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //</Bugfix>
    }

    /**
     * Called by the Activity once the server stated that not only is it up and running {@link ServerActivityCallbackInterface#onLocalServerReady()}},
     * but also that it is ready to handle the first connected client. This happens after the client established a connection and the server initialized an {@link ch.ethz.inf.vs.a4.minker.einz.server.EinzServerClientHandler}.
     * That means that the server calls {@link ServerActivityCallbackInterface#onFirstESCHReady()} on the host. The {@link ch.ethz.inf.vs.a4.minker.einz.UI.LobbyActivity} implements this interface and will inform the host client by calling this method.
     * From then on, the client is allowed to send messages freely.
     */
    public void onServersideHandlerReady(){
        sendRegistrationMessage();
    }

    /**
     * call this in an non-main-thread
     * @param unregisterFirst true if the client should first try to unregister
     */
    public void shutdown(boolean unregisterFirst) {
        if(unregisterFirst &&!this.dead) {sendUnregisterRequest();}
        this.connection.stopClient();
        this.dead = true;
        this.keepaliveScheduler.onShuttingDown();
    }

    private void sendUnregisterRequest() {
        EinzMessageHeader header = new EinzMessageHeader("registration", "UnregisterRequest");
        EinzUnregisterRequestMessageBody body = new EinzUnregisterRequestMessageBody(this.username); // getting all the girls
        final EinzMessage<EinzUnregisterRequestMessageBody> message = new EinzMessage<>(header, body);
        this.connection.sendMessageIgnoreFailures(message);
        Log.d("EinzClient/shutdown", "sent unregister message for "+this.username);
    }

    public void sendKickRequest(String username) {
        EinzMessageHeader header = new EinzMessageHeader("registration", "Kick");
        EinzKickMessageBody body = new EinzKickMessageBody(username);
        final EinzMessage<EinzKickMessageBody> message = new EinzMessage<>(header, body);
        this.connection.sendMessageRetryXTimes(5, message);
        Log.d("EinzClient/kick", "Sent kick request (kick "+username+")");
    }

    public boolean isDead() {
        return dead;
    }

    /**
     * when the clientconnection stops completely after {@link EinzClientConnection#stopClient()}
     */
    void onClientConnectionDead() {
        this.dead=true;
    }
}
