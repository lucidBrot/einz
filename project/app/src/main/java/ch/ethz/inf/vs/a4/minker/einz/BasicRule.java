package ch.ethz.inf.vs.a4.minker.einz;

public abstract class BasicRule {

    protected GameConfig config;

    public BasicRule(GameConfig config){
        this.config = config;
    }

    public abstract String getName();

    public abstract String getDescription();




}
