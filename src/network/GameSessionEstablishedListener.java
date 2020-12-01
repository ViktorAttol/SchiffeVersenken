package network;
/**
 * Call back interface gives feedback of established connection to remote player
 */
public interface GameSessionEstablishedListener {
    /**
     * is called when oracle was created
     * @param oracle true if local should be the starting player
     * @param partnerName name of remote Player
     */
    void gameSessionEstablished(boolean oracle, String partnerName);
}
