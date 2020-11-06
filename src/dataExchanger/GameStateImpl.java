package dataExchanger;

public class GameStateImpl implements GameState {
    private int layoutWidth;
    private int layoutHeight;

    private int[][] gameState;

    public GameStateImpl(int width, int height){
        this.layoutWidth = width;
        this.layoutHeight = height;
        this.gameState = new int[height][width];
    }

    @Override
    public int getGameLayoutWidth() {
        return this.layoutWidth;
    }

    @Override
    public int getGameLayoutHeight() {
        return this.layoutHeight;
    }

    @Override
    public int[][] getGameState() {
        return this.gameState;
    }
}
