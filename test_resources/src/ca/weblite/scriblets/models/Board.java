/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author shannah
 */
public class Board {

    public static enum Direction {
        VERTICAL,
        HORIZONTAL;
        public static final Direction[] ALL = new Direction[]{VERTICAL, HORIZONTAL};
        public Direction orthogonal(){
            switch (this){
                case VERTICAL: return HORIZONTAL;
                case HORIZONTAL: return VERTICAL;
            }
            return null;
        }
    }
    
    /**
     * @return the tiles
     */
    public Tile[] getTiles() {
        return tiles;
    }
    
    public Tile getTile(int row, int col){
        return tiles[getWidth()*row + col];
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }
    public class Tile {
        private final int row;
        private final int col;
        private Card card;
        
        public Tile(int row, int col){
            this.row = row;
            this.col = col;
        }

        /**
         * @return the row
         */
        public int getRow() {
            return row;
        }

        /**
         * @return the col
         */
        public int getCol() {
            return col;
        }

        /**
         * @return the card
         */
        public Card getCard() {
            return card;
        }
        
        public void setCard(Card card){
            this.card = card;
        }
        
        public Tile getNorthTile(){
            return row == 0 ? null : getTile(row-1,col);
        }
        
        public Tile getSouthTile(){
            return row == height-1 ? null : getTile(row+1, col);
        }
        
        public Tile getEastTile(){
            return col == width-1 ? null : getTile(row, col+1);
        }
        
        public Tile getWestTile(){
            return col == 0 ? null : getTile(row, col-1);
        }
        
        public Card getNorthCard(){
            Tile nt = getNorthTile();
            if ( nt != null ) return nt.getCard();
            return null;
            
        }
        
        public Card getEastCard(){
            Tile et = getEastTile();
            if ( et != null ) return et.getCard();
            return null;
        }
        
        public Card getWestCard(){
            Tile et = getWestTile();
            if ( et != null ) return et.getCard();
            return null;
        }
        
        public Card getSouthCard(){
            Tile et = getSouthTile();
            if ( et != null ) return et.getCard();
            return null;
        }
        
    }
    
    private final Tile[] tiles;
    private final int width;
    private final int height;
    
    
    public Board(int width, int height){
        this.width = width;
        this.height = height;
        int len = width*height;
        this.tiles = new Tile[len];
        
        for ( int i=0; i<height; i++){
            for ( int j=0; j<width; j++){
                tiles[i*width+j] = new Tile(i, j);
            }
        }
    }
    
    public Board createSnapshot(){
        Board out = new Board(width, height);
        copyInto(out);
        return out;
    }
    
    public void copyInto(Board target){
        if ( target.width != width || target.height != height ){
            throw new RuntimeException("Boards are not the same size so cannot be copied");
        }
        int len = tiles.length;
        for (int i=0; i<len; i++){
            if ( tiles[i].getCard() != null ){
                target.tiles[i].setCard(tiles[i].getCard());
            }
        }
        
    }
    
    public Collection<Tile> getContiguousFilledTiles(Direction dir, int row, int col){
        Collection<Tile> out = new LinkedList<Tile>();
        Tile origin = this.getTile(row, col);
        if ( origin.getCard() == null ){
            return out;
        }
        out.add(origin);
        switch ( dir ){
            case VERTICAL:
                for ( int i=row-1; i>0; i-- ){
                    Tile curr = this.getTile(i, col);
                    if ( curr.getCard() == null ){
                        break;
                    } else {
                        out.add(curr);
                    }
                }
            
                for (int i=row+1; i<getWidth(); i++){
                    Tile curr = this.getTile(i, col);
                    if ( curr.getCard() == null ){
                        break;
                    } else {
                        out.add(curr);
                        
                    }
                }
                break;
                
            case HORIZONTAL:
                for ( int i=col-1; i>0; i-- ){
                    Tile curr = this.getTile(row, i);
                    if ( curr.getCard() == null ){
                        break;
                    } else {
                        out.add(curr);
                    }
                }
            
                for (int i=col+1; i<getWidth(); i++){
                    Tile curr = this.getTile(row, i);
                    if ( curr.getCard() == null ){
                        break;
                    } else {
                        out.add(curr);
                        
                    }
                }
                break;
            
            
        }
        return out;
    }
    
    public Tile findTileWithCard(Card card){
        for ( Tile tile : tiles ){
            if ( tile.getCard() == card ){
                return tile;
            }
        }
        return null;
    }
}
