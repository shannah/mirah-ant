/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.views;

import ca.weblite.scriblets.models.Board;
import ca.weblite.scriblets.models.Board.Tile;
import ca.weblite.scriblets.models.Card;
import com.codename1.io.Log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Graphics;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.Layout;

/**
 *
 * @author shannah
 */
public class BoardView extends Container {
    
    
    private Board board;
    private int evenColor = 0x0;
    private int oddColor = 0xff0000;
    private GameController controller;
    
    public BoardView(Board board){
        this.setLayout(new BoardLayout());
        this.board = board;
        this.setDropTarget(true);
        updateBoard();
        
    }
    
    public void setGameController(GameController controller){
        this.controller = controller;
    }
    
    public final void updateBoard(){
        this.removeAll();
        Tile[] tiles = board.getTiles();
        int len = tiles.length;
        for ( int i=0; i<len; i++){
            Tile tile = tiles[i];
            if ( tile.getCard() != null ){
                Log.p("Adding tile view ");
                this.addComponent(new TileView(tile));
            }
        }
    }

    @Override
    public void paintBackground(Graphics g) {
        super.paintBackground(g); 
        
        int w = board.getWidth();
        int h = board.getHeight();
        
        int tileWidth = calcTileWidth();
        // paint evens
        g.setColor(evenColor);
        int y = getY();
        int start =0;
        for ( int i=0; i<h; i++){
            int x = getX()+start*tileWidth;
            
            for ( int j=start; j<w; j+=2){
                g.fillRect(x, y, tileWidth, tileWidth);
                x += tileWidth<<1;
            }
            y+= tileWidth;
            start = (start+1)%2;
        }
        
        g.setColor(oddColor);
        y = getY();
        start = 1;
        for ( int i=0; i<h; i++){
            int x = getX()+start*tileWidth;
            
            for ( int j=start; j<w; j+=2){
                g.fillRect(x, y, tileWidth, tileWidth);
                x += tileWidth<<1;
            }
            y+= tileWidth;
            start = (start+1)%2;
        }
        
    }
    
    
    public int calcTileWidth(){
        return Math.min(getWidth()/board.getWidth(), getHeight()/board.getHeight());
    }
    
    class TileView extends Container {
        final Tile tile;
        
        TileView(Tile tile){
            this.tile = tile;
            this.setLayout(new BorderLayout());
            
            if ( this.tile.getCard() != null ){
                this.addComponent(BorderLayout.CENTER, new CardView(this.tile.getCard()));
            }
            
            
        }


        
        
    }
    
    public Board getBoard(){
        return board;
    }
    
    private static class BoardLayout extends Layout {

        @Override
        public void layoutContainer(Container parent) {
            BoardView bv = (BoardView)parent;
            int len = parent.getComponentCount();
            int tileWidth = bv.calcTileWidth();
            for ( int i=0; i<len; i++){
                Component current = parent.getComponentAt(i);
                if ( current instanceof TileView ){
                    TileView tv = (TileView)current;
                    if ( tv.tile.getCard() != null ){
                        Log.p("Laying out tile");
                        tv.setSize(new Dimension(tileWidth, tileWidth));
                        tv.setX(tv.tile.getCol()*tileWidth);
                        tv.setY(tv.tile.getRow()*tileWidth);
                        continue;
                    }
                } 
                
                current.setSize(new Dimension(1,1));
            }
        }

        @Override
        public Dimension getPreferredSize(Container parent) {
            BoardView bv = (BoardView)parent;
            int tileWidth = bv.calcTileWidth();
            return new Dimension(bv.board.getWidth()*tileWidth, bv.board.getHeight()*tileWidth);
        }
        
    }

    @Override
    protected boolean draggingOver(Component dragged, int x, int y) {
        if ( dragged instanceof CardView ){
            // Let's find the tile
            int tileWidth = calcTileWidth();
            int col = x/tileWidth;
            int row = y/tileWidth;
            if ( col < 0 || row < 0 || col>=board.getWidth() || row>=board.getHeight()){
                return false;
            }
            Tile tile = board.getTile(row, col);
            return (tile.getCard()==null);
        } else {
            return false;
        }
        
    }

    @Override
    public void drop(Component dragged, int x, int y) {
        if ( dragged instanceof CardView ){
            if ( controller != null ){
                controller.cardDroppedOnBoard((CardView)dragged, this, x, y);
            }  
        }
           
    }
    
    
    
    
}
