package schiffeversenken;

public class StatusException extends Exception{
    /**
     * Used if methods are called in wrong status
     */
    public StatusException(){super();}
    public StatusException(String message){super(message);}
    public StatusException(String message, Throwable t){super(message, t);}
}
