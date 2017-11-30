package ch.ethz.inf.vs.a4.minker.einz;

public class TodoException extends Exception {

    private String appendum;

    public TodoException(){
        this.appendum="";
    }

    public TodoException(String appendum){
        this.appendum="\n"+appendum;
    }

    @Override
    public String getMessage() {
        return super.getMessage()+this.appendum;
    }
}
