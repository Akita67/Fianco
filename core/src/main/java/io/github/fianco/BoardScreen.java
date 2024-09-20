package io.github.fianco;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class BoardScreen extends InputAdapter implements Screen {

    private final Main game;
    private final GameLogic gameLogic;
    private final OrthographicCamera camera;
    private final ShapeRenderer shapeRenderer;
    private final BitmapFont font;
    private final GlyphLayout layout;
    private final Texture blackStoneTexture;
    private final Texture whiteStoneTexture;

    public final int gridSize = 9;  // 9x9 grid
    private final int cellSize = 60; // Size of each cell in pixels
    private boolean[][] cellClicked; // Tracks clicked cells
    private int[][] board; // Tracks where stones are placed (0 = empty, 1 = white, 2 = black)

    private int selectedRow = -1; // Track the selected stone's row
    private int selectedCol = -1; // Track the selected stone's column
    private boolean isBlackTurn = false; // Track whose turn it is (alternating between white and black stones)
    private boolean reset = false;
    protected int numWinPlayer1 = 0;
    protected int numWinPlayer2 = 0;
    protected boolean isPlayer1White = true;
    List<List<Integer>> possibleMoves = new ArrayList<>();
    private boolean flag = false;
    private Stage stage;
    private TextButton undoButton;
    private float undoButtonX = 700;
    private float undoButtonY = 400;
    private float undoButtonWidth = 100;
    private float undoButtonHeight = 50;
    // Add this in your class attributes
    private Stack<int[][]> boardHistory;

    private boolean ai1, ai2;
    private int index1, index2;
    private Bot bot1,bot2;

    public BoardScreen(Main game) {
        this.game = game;
        gameLogic = new GameLogic(game);

        boardHistory = new Stack<>(); // Initialize the stack for undo

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 600); // Set your camera dimensions
        shapeRenderer = new ShapeRenderer();

        cellClicked = new boolean[gridSize][gridSize]; // Initialize cell click tracking
        board = new int[gridSize][gridSize]; // Initialize the board

        font = new BitmapFont(); // Default LibGDX font
        layout = new GlyphLayout(); // Used to position text

        // Load stone textures
        blackStoneTexture = new Texture(Gdx.files.internal("black_stone.png"));
        whiteStoneTexture = new Texture(Gdx.files.internal("white_stone.png"));

        // Undo Button
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // You will need a skin file
        undoButton = new TextButton("Undo", skin);
        undoButton.setPosition(undoButtonX, undoButtonY); // Position the button on the screen
        undoButton.setSize(undoButtonWidth, undoButtonHeight);

        stage = new Stage();
        undoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //undoMove(); // Trigger undo when the button is clicked
                undoMove();
            }
        });

        stage.addActor(undoButton); // Add the button to the stage

        // Set initial positions for the stones
        setInitialStonePositions();

        // Set this screen as the input processor to capture clicks
        Gdx.input.setInputProcessor(this);
    }
    public BoardScreen(Main game, boolean ai1, boolean ai2, int index1, int index2){
        this.game = game;
        this.ai1 = ai1;
        this.ai2 = ai2;
        this.index1 = index1;
        this.index2 = index2;

        gameLogic = new GameLogic(game);

        boardHistory = new Stack<>(); // Initialize the stack for undo

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1000, 600); // Set your camera dimensions
        shapeRenderer = new ShapeRenderer();

        cellClicked = new boolean[gridSize][gridSize]; // Initialize cell click tracking
        board = new int[gridSize][gridSize]; // Initialize the board

        font = new BitmapFont(); // Default LibGDX font
        layout = new GlyphLayout(); // Used to position text

        // Load stone textures
        blackStoneTexture = new Texture(Gdx.files.internal("black_stone.png"));
        whiteStoneTexture = new Texture(Gdx.files.internal("white_stone.png"));

        // Undo Button
        Skin skin = new Skin(Gdx.files.internal("ui/uiskin.json")); // You will need a skin file
        undoButton = new TextButton("Undo", skin);
        undoButton.setPosition(undoButtonX, undoButtonY); // Position the button on the screen
        undoButton.setSize(undoButtonWidth, undoButtonHeight);

        stage = new Stage();
        undoButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                //undoMove(); // Trigger undo when the button is clicked
                undoMove();
            }
        });

        stage.addActor(undoButton); // Add the button to the stage

        // Set initial positions for the stones
        setInitialStonePositions();

        // Set this screen as the input processor to capture clicks
        Gdx.input.setInputProcessor(this);
    }

    protected void setInitialStonePositions() {
        // Set white stones in the first row
        for (int col = 0; col < gridSize; col++) {
            board[0][col] = 1; // 1 = white stone
        }
        board[1][1]=1;board[1][7]=1;board[2][2]=1;board[2][6]=1;board[3][3]=1;board[3][5]=1;

        // Set black stones in the last row
        for (int col = 0; col < gridSize; col++) {
            board[8][col] = 2; // 2 = black stone
        }
        board[7][1]=2;board[7][7]=2;board[6][2]=2;board[6][6]=2;board[5][3]=2;board[5][5]=2;
    }
    @Override
    public void show() {
        if (ai1) {
            isPlayer1White = !isPlayer1White;
            switch (index1) {
                case 0: {
                    bot1 = new RandomBot(false,board);
                    break;
                }
                case 1:{
                    bot1 = new AlphaBetaBot(false,board,5);
                    break;
                }
                case 2:{
                    bot1 = new MTCS(false,board,8000);
                    break;
                }
            }
        }

        if(ai2) {
            switch (index2) {
                case 0: {
                    bot2 = new RandomBot(true,board);
                    break;
                }
                case 1:{
                    bot2 = new AlphaBetaBot(true,board,5); // at 7 becomes slow
                    break;
                }
                case 2:{
                    bot2 = new MTCS(true,board,8000);
                    break;
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(1, 1, 1, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        game.mainBatch.setProjectionMatrix(camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        makeGrid();
        updateScore();

        gameLogic.winCondition(this, board, game.mainBatch,bot1,bot2,ai1,ai2);
        gameLogic.finished(numWinPlayer1,numWinPlayer2);

        // If AI let it play
        if((!isPlayer1White && ai1 && !isBlackTurn) || (isPlayer1White && ai1 && isBlackTurn)){
            bot1.execMove(this,board);
        } else if ((isPlayer1White && ai2 && isBlackTurn) || (!isPlayer1White && ai2 && !isBlackTurn)) {
            bot2.execMove(this,board);
        }

        // Render the stage (which contains the undo button)
        stage.act(delta);
        stage.draw();
    }

    private String getChessNotation(int row, int col) {
        char file = (char) ('a' + col); // 'a' to 'i'
        int rank = 1 + row; // 1 to 9
        return "" + file + rank;
    }
    private void saveBoardState() {
        int[][] boardCopy = new int[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            System.arraycopy(board[i], 0, boardCopy[i], 0, gridSize);
        }
        boardHistory.push(boardCopy);
    }
    private void undoMove() {
        if (!boardHistory.isEmpty() && (ai1 || ai2)) {
            board = boardHistory.pop(); // Restore the last board state
            selectedRow = -1; // Deselect any selected stone
            selectedCol = -1;
            flag = false;
            movesToNull();
        }else if (!boardHistory.isEmpty()){
            board = boardHistory.pop(); // Restore the last board state
            selectedRow = -1; // Deselect any selected stone
            selectedCol = -1;
            isBlackTurn = !isBlackTurn; // Reverse the turn
            flag = false;
        }
        else {
            System.out.println("No moves to undo!");
        }
    }


    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        Vector3 worldCoordinates = camera.unproject(new Vector3(screenX, screenY, 0));
        // Check if the undo button was pressed
        if (worldCoordinates.x + 140 >= undoButtonX && worldCoordinates.x + 140 <= undoButtonX + undoButtonWidth &&
            worldCoordinates.y + 60 >= undoButtonY && worldCoordinates.y + 60 <= undoButtonY + undoButtonHeight) {
            undoMove(); // Call undoMove when undo button is pressed
            return true;
        }

        int col = (int) (worldCoordinates.x / cellSize);
        int row = (int) (worldCoordinates.y / cellSize);

        if ( (row >= 0 && row < gridSize && col >= 0 && col < gridSize)) {
            if (selectedRow == -1 && selectedCol == -1 || board[row][col] != 0) {
                // Select a stone depending on the player turn
                if (board[row][col] == 1 && !isBlackTurn) {
                    selectedRow = row;
                    selectedCol = col;
                } else if (board[row][col] == 2 && isBlackTurn) {
                    selectedRow = row;
                    selectedCol = col;
                }
                movesToNull();
            } else {
                saveBoardState();
                // Attempt to move the stone if a stone is already selected
                if(flag){
                    System.out.println("Need to attack");
                    flag = AttackStone(row,col);
                    System.out.println(flag);
                }else{
                    moveStone(row, col);
                }
            }
        }
        //printBoard(board);
        if(checkForCaptures(board, !isBlackTurn)){
            System.out.println("I can attack");
            flag = true;

        }else{
            System.out.println("nobody can attack");
        }
        return true; // Return true to indicate the event was handled
    }

    private void moveStone(int row, int col) {
        // Check if the move is valid (forward, left, right, and only by one cell)
        if(board[row][col] == 0){
            if ( ((!isBlackTurn) && (row - selectedRow == 1 && col == selectedCol)) || ((isBlackTurn) && (selectedRow - row == 1 && col == selectedCol)) || // Forward
                (Math.abs(col - selectedCol) == 1 && row == selectedRow) ) {  // Left or right

                // Move the stone
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = 0;

                // Reset the selection
                selectedRow = -1;
                selectedCol = -1;

                // Change turns
                isBlackTurn = !isBlackTurn;

            }
        }
    }
    protected void botMoveStone(int rowS, int colS, int rowE, int colE) {
        // Check if the move is valid (forward, left, right, and only by one cell)

        // Move the stone
        board[rowE][colE] = board[rowS][colS];
        board[rowS][colS] = 0;

        // Reset the selection
        selectedRow = -1;
        selectedCol = -1;

        // Change turns
        isBlackTurn = !isBlackTurn;
    }
    protected void botAttackStone(int rowS, int colS, int rowE, int colE) {
        // Check if the move is valid (forward, left, right, and only by one cell)

        // Move the stone
        board[rowE][colE] = board[rowS][colS];
        board[rowS][colS] = 0;
        if(isBlackTurn){
            if (colE>colS)
                board[rowS-1][colS+1] = 0;
            else
                board[rowS-1][colS-1] = 0;
        }
        else{
            if (colE>colS)
                board[rowS+1][colS+1] = 0;
            else
                board[rowS+1][colS-1] = 0;
        }

        // Reset the selection
        selectedRow = -1;
        selectedCol = -1;

        // Change turns
        isBlackTurn = !isBlackTurn;
        flag = false;
        movesToNull();
    }
    protected boolean checkForCaptures(int [][]board, boolean player1) {
        //When a player is on the wall the other player needs to attack is a bug

        int playerStone = player1 ? 1 : 2; // 1 for white, 2 for black
        int opponentStone = player1 ? 2 : 1; // Opponent's stone
        List<Integer> move = new ArrayList<>();

        // Loop through the board to check for capture possibilities
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                if (board[row][col] == playerStone) {
                    // Check diagonally left and right for opponent stones

                    // Check diagonally left
                    if (playerStone == 1) { // White moves upwards
                        // Check diagonally left
                        if (col>1 && row < 7 && board[row + 1][col - 1] == opponentStone) {
                            // Check if there's an empty space behind the opponent (for white moving up)
                            if (board[row + 2][col - 2] == 0) {
                                move.add(row);move.add(col);move.add(row+1);move.add(col-1);move.add(row+2);move.add(col-2);possibleMoves.add(move);move=new ArrayList<>();
                            }
                        }
                        // Check diagonally right
                        if (col < board[row].length - 2 && row < 7 && board[row + 1][col + 1] == opponentStone) {
                            // Check if there's an empty space behind the opponent (for white moving up)
                            if (board[row + 2][col + 2] == 0) {
                                move.add(row);move.add(col);move.add(row+1);move.add(col+1);move.add(row+2);move.add(col+2);possibleMoves.add(move);move=new ArrayList<>();
                            }
                        }
                    }


                    // Similar logic for black moving down (opposite direction)
                    if (playerStone == 2) { // Black moves downwards
                        // Check diagonally left
                        if (col > 1 && row > 1 && board[row - 1][col - 1] == opponentStone) {
                            // Check if there's an empty space behind the opponent (for black moving down)
                            if (board[row - 2][col - 2] == 0) {
                                move.add(row);move.add(col);move.add(row-1);move.add(col-1);move.add(row-2);move.add(col-2);possibleMoves.add(move);move=new ArrayList<>();
                            }
                        }

                        // Check diagonally right
                        if (col < board[col].length - 2 && row > 1 && board[row - 1][col + 1] == opponentStone) {
                            // Check if there's an empty space behind the opponent (for black moving down)
                            if (board[row - 2][col + 2] == 0) {
                                move.add(row);move.add(col);move.add(row-1);move.add(col+1);move.add(row-2);move.add(col+2);possibleMoves.add(move);move=new ArrayList<>();
                            }
                        }
                    }
                }
            }
        }
        if(possibleMoves.size()>0){
            System.out.println(possibleMoves.get(0).get(0) + " " + possibleMoves.get(0).get(1));
            return true;}

        // If no attack is possible, allow the move
        return false;

    }
    protected List<Move> checkForCapturesSpec(int [][]board, boolean player1, int row, int col) {
        //When a player is on the wall the other player needs to attack is a bug

        int playerStone = player1 ? 1 : 2; // 1 for white, 2 for black
        int opponentStone = player1 ? 2 : 1; // Opponent's stone
        List<Move> getAttackMoves = new ArrayList<>();

        // Loop through the board to check for capture possibilities
        if (board[row][col] == playerStone) {
            // Check diagonally left and right for opponent stones

            // Check diagonally left
            if (playerStone == 1) { // White moves upwards
                // Check diagonally left
                if (col>1 && row < 7 && board[row + 1][col - 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for white moving up)
                    if (board[row + 2][col - 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row+2, col-2, true));

                    }
                }
                // Check diagonally right
                if (col < board[row].length - 2 && row < 7 && board[row + 1][col + 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for white moving up)
                    if (board[row + 2][col + 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row+2, col+2, true));
                    }
                }
            }


            // Similar logic for black moving down (opposite direction)
            if (playerStone == 2) { // Black moves downwards
                // Check diagonally left
                if (col > 1 && row > 1 && board[row - 1][col - 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for black moving down)
                    if (board[row - 2][col - 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row-2, col-2, true));
                    }
                }

                // Check diagonally right
                if (col < board[col].length - 2 && row > 1 && board[row - 1][col + 1] == opponentStone) {
                    // Check if there's an empty space behind the opponent (for black moving down)
                    if (board[row - 2][col + 2] == 0) {
                        getAttackMoves.add(new Move(row, col, row-2, col+2, true));
                    }
                }
            }
        }
        return getAttackMoves;

    }
    // Capture the opponent's stone
    private boolean AttackStone(int row, int col){
        for (List<Integer> move: possibleMoves) {
            if(selectedRow==move.get(0) && selectedCol==move.get(1)){
                if(row==move.get(4) && col==move.get(5)){
                    // Move the stone
                    board[row][col] = board[selectedRow][selectedCol];
                    board[selectedRow][selectedCol] = 0;
                    board[move.get(2)][move.get(3)] = 0;

                    // Reset the selection
                    selectedRow = -1;
                    selectedCol = -1;

                    // Change turns
                    isBlackTurn = !isBlackTurn;
                    movesToNull();
                    return false;
                }
            }
        }
        return true;
    }

    public void makeGrid(){
        // Draw the grid lines
        shapeRenderer.setColor(0, 0, 0, 1); // Grid color (black)
        for (int i = 0; i <= gridSize; i++) {
            // Draw vertical lines
            shapeRenderer.line(i * cellSize, 0, i * cellSize, gridSize * cellSize);
            // Draw horizontal lines
            shapeRenderer.line(0, i * cellSize, gridSize * cellSize, i * cellSize);
        }

        shapeRenderer.end();

        // Render the chess notations
        game.mainBatch.begin();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                String notation = getChessNotation(row, col);
                layout.setText(font, notation);

                // Center the text in the cell
                float x = col * cellSize + (cellSize - layout.width) / 2;
                float y = row * cellSize + (cellSize + layout.height) / 2;

                font.draw(game.mainBatch, notation, x, y);

                // Draw stones based on the board state
                if (board[row][col] == 1) { // White stone
                    game.mainBatch.draw(whiteStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
                } else if (board[row][col] == 2) { // Black stone
                    game.mainBatch.draw(blackStoneTexture, col * cellSize, row * cellSize, cellSize, cellSize);
                }
            }
        }
        game.mainBatch.end();
    }
    public void resetGrid(){
        board = new int[gridSize][gridSize]; // Initialize the board
        setInitialStonePositions();
        isBlackTurn=false;
    }
    public void movesToNull(){
        possibleMoves = new ArrayList<>();
    }
    public void updateScore(){
        game.mainBatch.begin();
        font.setColor(Color.BLACK);
        if(isPlayer1White)
            font.draw(game.mainBatch, "Player 1 is White",250,600);
        else{
            font.draw(game.mainBatch, "Player 1 is Black",250,600);
        }

        font.draw(game.mainBatch, "Score of player 1 :",550,500);
        font.draw(game.mainBatch, new String(String.valueOf(numWinPlayer1)),680,500);
        font.draw(game.mainBatch, "Score of player 2 :",550,450);
        font.draw(game.mainBatch, new String(String.valueOf(numWinPlayer2)),680,450);
        game.mainBatch.end();
    }
    public void addPlayer1Score(){
        numWinPlayer1++;
    }
    public void addPlayer2Score(){
        numWinPlayer2++;
    }
    public void changePlayer1Side(){
        isPlayer1White = !isPlayer1White;
    }
    public void clearHistory() { boardHistory = new Stack<>();}
    public void printBoard(int[][] board){
        for (int i = board.length-1; i >= 0; i--) {
            for (int j = 0; j < board.length; j++) {
                System.out.print(board[i][j] + " ");
            }
            System.out.println("");
        }
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }


    @Override
    public void hide() {
        // Dispose resources when hidden
    }

    @Override
    public void pause() {
        // Pause logic if needed
    }

    @Override
    public void resume() {
        // Resume logic if needed
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        font.dispose();
        blackStoneTexture.dispose();
        whiteStoneTexture.dispose();
        stage.dispose(); // Dispose of the stage
    }
}
