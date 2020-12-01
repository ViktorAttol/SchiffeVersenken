package network;

/**
 * callback interface gives UI feedback if player has set all of his ships
 */
public interface ShipsPlacedListener {
    void shipsPlaced(String userName);
}
