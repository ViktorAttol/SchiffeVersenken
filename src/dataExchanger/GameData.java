package dataExchanger;

public interface GameData {
    /**
     *
     * @return timestamp of player input
     */
    long getTimeStamp();

    /**
     *
     * @return input string
     */
    String getPositionInput();

    /**
     *
     * @return name of player who made the input
     */
    String getPlayername();
}
