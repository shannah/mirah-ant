/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.models;

import ca.weblite.scriblets.Calculator;
import ca.weblite.scriblets.Util;
import ca.weblite.scriblets.models.Board.Direction;
import ca.weblite.scriblets.models.Board.Tile;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



/**
 *
 * @author shannah
 */
public class Move {
    private final Game game;
    private final Player player;
    private final Board boardSnapshot;
    private Set<Board.Tile> tilesUsed;
    private boolean done = false;
    
    public Move(Game game, Player player){
        this.game = game;
        this.player = player;
        this.boardSnapshot = game.getBoard().createSnapshot();
        tilesUsed = new HashSet<Board.Tile>();
    }
    
    
    public Board getBoardSnapshot(){
        return boardSnapshot;
    }
    
    public void setDone(boolean done){
        this.done = done;
    }
    
    public boolean isDone(){
        return this.done;
    }
    
    public Player getPlayer(){
        return player;
    }
    
    public Board.Tile[] getTilesUsed(){
        return tilesUsed.toArray(new Board.Tile[0]);
    }
    
    public void removeTileUsed(Board.Tile tile){
        tilesUsed.remove(tile);
    }
    
    public void revert(){
        game.getBoard().copyInto(this.getBoardSnapshot());
        for ( Board.Tile tile : this.getTilesUsed()){
            this.getPlayer().addCard(tile.getCard());
            tile.setCard(null);
            this.removeTileUsed(tile);
        }
    }
    
    public void commit(){
        game.commit(this);
    }
    
    
    public void placeCardOnBoard(Card card, int row, int col){
        placeCard(card, getBoardSnapshot().getTile(row, col));
    }
    
    public void adjustCardPlacement(Board.Tile existingTile, Board.Tile newTile){
        Card card = existingTile.getCard();
        if ( card == null ){
            throw new RuntimeException("No card in existing position");
        }
        existingTile.setCard(null);
        newTile.setCard(card);
        tilesUsed.remove(existingTile);
        tilesUsed.add(newTile);
    }
    
    
    public void placeCard(Card card, Board.Tile tile){
        if ( player.removeCard(card)){
            tile.setCard(card);
            tilesUsed.add(tile);
        } else {
            throw new RuntimeException("Cannot place card because it is not in the current player's hand");
        }
    }
    
    public void pickupCardFromBoard(Card card){
        Tile tile = this.boardSnapshot.findTileWithCard(card);
        pickupCardFromBoard(tile);
        
    }
    
    public void pickupCardFromBoard(Tile tile){
        if ( tile == null ){
            throw new RuntimeException("Card is not currently on board so we can't pick it up");
        }
        Card card = tile.getCard();
        tile.setCard(null);
        tilesUsed.remove(tile);
        player.addCard(card);
    }
    
    
    public Direction getMainMoveDirection(){
        int minRow=-1;
        int maxRow=-1;
        int minCol=-1;
        int maxCol=-1;
        for ( Tile tile : tilesUsed ){
            if ( minRow == -1 || minRow > tile.getRow() ){
                minRow = tile.getRow();
            }
            if ( maxRow == -1 || maxRow < tile.getRow() ){
                maxRow = tile.getRow();
            }
            if ( minCol == -1 || minCol > tile.getCol() ){
                minCol = tile.getCol();
            }
            if ( maxCol == -1 || maxCol < tile.getCol() ){
                maxCol = tile.getCol();
            }
        }
        
        if ( maxRow-minRow > 0 && maxCol-minCol > 0){
            return null;
        }
        
        Direction mainDir = (maxRow-minRow==0) ? Direction.HORIZONTAL:Direction.VERTICAL;
        //Direction orthogonalDir = mainDir==Direction.HORIZONTAL? Direction.VERTICAL : Direction.HORIZONTAL;
        return mainDir;
    }
    
    public boolean isValidMove(){
        // Make sure that tiles occupy single row or column.
        
        if ( tilesUsed.isEmpty() ){
            return false;
        }
        
        Direction mainDir = getMainMoveDirection();
        if ( mainDir == null ){
            return false;
        }
        
        
        Direction orthogonalDir = mainDir.orthogonal();
        
        Tile sampleTile = null;
        for ( Tile t : tilesUsed ){
            sampleTile = t;
            break;
        }
        
        
        Collection<Tile> mainSet = boardSnapshot.getContiguousFilledTiles(mainDir, sampleTile.getRow(), sampleTile.getCol());
        Collection<Card> mainHand = Util.getCards(mainSet);
        if ( !Calculator.isValidHand(mainHand)){
            return false;
        }
        
        // Make sure that every card is used in every contigous run it is a
        // part of
        for ( Tile tile : tilesUsed ){
            
            Collection<Tile> tiles = boardSnapshot.getContiguousFilledTiles(orthogonalDir, tile.getRow(), tile.getCol());
            if ( tiles.size() > 1 && !Calculator.isValidHand(Util.getCards(tiles))){
                return false;
            }
            
        }
        
        return true;
    }
    
    public int calculateScore(){
        if ( !isValidMove() ){
            return 0;
        }
        
        Tile sampleTile = null;
        for ( Tile t : tilesUsed ){
            sampleTile = t;
            break;
        }
        
        Direction mainDir = this.getMainMoveDirection();
        if ( mainDir == null ){
            return 0;
        }
        Direction orthogonalDir = mainDir.orthogonal();
        
        
        Collection<Tile> mainSet = boardSnapshot.getContiguousFilledTiles(mainDir, sampleTile.getRow(), sampleTile.getCol());
        Collection<Card> mainHand = Util.getCards(mainSet);
        
        int score = Calculator.calculateScore(mainHand);
        
        for ( Tile tile : tilesUsed){
            Collection<Tile> tiles = this.boardSnapshot.getContiguousFilledTiles(orthogonalDir, tile.getRow(), tile.getCol());
            if ( tiles.size() > 1 ){
                score += Calculator.calculateScore(Util.getCards(tiles));
            }
            
        }
        return score;
    }
    
    public boolean involvesCard(Card card){
        for ( Tile tile : tilesUsed ){
            if ( tile.getCard() == card ){
                return true;
            }
        }
        return false;
    }
    
    public int[] getRange(){
        int minCol = 999;
        int minRow = 999;
        int maxCol = -1;
        int maxRow = -1;
        for ( Tile t : getTilesUsed()){
            if ( t.getCol() < minCol ){
                minCol = t.getCol();
            }
            if ( t.getCol() > maxCol ){
                maxCol = t.getCol();
            }
            if (t.getRow() < minRow ){
                minRow = t.getRow();
            }
            if ( t.getRow() > maxRow ){
                maxRow = t.getRow();
            }
        }
        
        return new int[]{minRow, minCol, maxRow, maxCol};
    }
}
