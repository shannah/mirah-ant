/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.views;

import ca.weblite.scriblets.models.Board;
import ca.weblite.scriblets.models.Board.Tile;
import ca.weblite.scriblets.models.Card;
import ca.weblite.scriblets.models.Game;
import ca.weblite.scriblets.models.Move;
import ca.weblite.scriblets.views.BoardView;
import ca.weblite.scriblets.views.CardView;
import ca.weblite.scriblets.views.HandView;
import com.codename1.io.Log;
import com.codename1.ui.Container;

/**
 *
 * @author shannah
 */
public class GameController {
    
    private Game game;
    private GameView gameView;
    
    
    public GameController(Game game, GameView gameView){
        this.game = game;
        this.gameView = gameView;
    }
    
    public void cardDroppedOnHand(CardView cv, HandView handView, int x, int y){
        Move currentMove = game.getCurrentMove();
        if ( currentMove.involvesCard(cv.getCard())){
            currentMove.pickupCardFromBoard(cv.getCard());
            gameView.updateGame();
            gameView.repaint();
        }
        
    }
    
    
    public boolean draggingCardOverHand(CardView cv, HandView hv, int x, int y){
        return game.getCurrentMove().involvesCard(cv.getCard());
    }
    
    public void cardDroppedOnBoard(CardView cv, BoardView boardView, int x, int y){
        Move currentMove = game.getCurrentMove();
        
        Board board = boardView.getBoard();
        Card card = cv.getCard();
        int tileWidth = boardView.calcTileWidth();
        int col = (x-boardView.getAbsoluteX())/tileWidth;
        int row = (y-boardView.getAbsoluteY())/tileWidth;
        if ( col < 0 || row < 0 || col>=board.getWidth() || row>=board.getHeight()){

            return;
        }
        
        
        
        Board.Tile tile = board.getTile(row, col);
        Container oldParent = (Container)cv.getParent();
        if ( oldParent instanceof BoardView.TileView ){
            
            
            BoardView.TileView tv = (BoardView.TileView)oldParent;
            if ( currentMove != null && board == currentMove.getBoardSnapshot()){
                currentMove.adjustCardPlacement(tv.tile, tile);
                //boardView.updateBoard();
                gameView.updateGame();
                //boardView.repaint();
                gameView.repaint();
            } 
        } else if ( oldParent instanceof HandView ){
            
            
            HandView hv = (HandView)oldParent;
            if ( currentMove != null && board == currentMove.getBoardSnapshot()){
                currentMove.placeCard(card, tile);
                //boardView.updateBoard();
                //hv.updateHand();
                gameView.updateGame();
                //hv.repaint();
                //boardView.repaint();
                gameView.repaint();
            } 
            
           

        }
    }
    
    
}
