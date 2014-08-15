/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.views;

import ca.weblite.scriblets.models.Game;
import ca.weblite.scriblets.models.Move;
import com.codename1.ui.Container;
import com.codename1.ui.Label;
import com.codename1.ui.layouts.BorderLayout;

/**
 *
 * @author shannah
 */
public class GameView extends Container {
    private final Game game;
    private BoardView boardView;
    private HandView handView;
    private GameController controller;
    private Label scoreLabel = new Label("0");
    
    public GameView(Game game){
        this.game = game;
        
        //boardView = new BoardView(game.getBoard());
        if ( game.getCurrentMove() != null){
            boardView = new BoardView(game.getCurrentMove().getBoardSnapshot());
        } else {
            boardView = new BoardView(game.getBoard());
        }
        
        handView = new HandView(game.getPlayer(0));
        this.setLayout(new BorderLayout());
        this.addComponent(BorderLayout.CENTER, boardView);
        this.addComponent(BorderLayout.SOUTH, handView);
        this.addComponent(BorderLayout.NORTH, scoreLabel);
        
    }
    
    public void setGameController(GameController controller){
        this.controller = controller;
        boardView.setGameController(controller);
        handView.setGameController(controller);
        
    }
    
    public final void updateGame(){
        
        if ( game.getCurrentMove() != null){
            
            Move currentMove = game.getCurrentMove();
            scoreLabel.setText(""+currentMove.calculateScore());
            if ( currentMove.getBoardSnapshot() != boardView.getBoard()){
                removeComponent(boardView);
                boardView = new BoardView(currentMove.getBoardSnapshot());
                addComponent(BorderLayout.CENTER, boardView);
            }
            
        } else {
            if ( boardView.getBoard() != game.getBoard()){
                removeComponent(boardView);
                boardView = new BoardView(game.getBoard());
            }
            
        }
        boardView.updateBoard();
        handView.updateHand();
        
    }
}
