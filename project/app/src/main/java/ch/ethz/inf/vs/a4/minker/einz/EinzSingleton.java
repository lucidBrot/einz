package ch.ethz.inf.vs.a4.minker.einz;

import ch.ethz.inf.vs.a4.minker.einz.client.EinzClient;

/**
 * Created by Josua on 12/8/17.
 */

public class EinzSingleton {
    private static EinzSingleton instance;

    private EinzClient client;

    private EinzSingleton(){

    }

    public static EinzSingleton getInstance(){
        if(instance == null){
            instance = new EinzSingleton();
        }
        return instance;
    }

    public void setEinzClient(EinzClient client){
        this.client = client;
    }

    public EinzClient getEinzClient(){
        return client;
    }
}
