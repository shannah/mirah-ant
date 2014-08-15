/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets;

import ca.weblite.scriblets.models.Board;
import ca.weblite.scriblets.models.Board.Direction;
import ca.weblite.scriblets.models.Board.Tile;
import ca.weblite.scriblets.models.Card;
import ca.weblite.scriblets.models.Move;
import ca.weblite.scriblets.models.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author shannah
 */
public class ComputerPlayer {
    private Player player;
    private Move move;
    
    public void proposeMove(){
        Board board = move.getBoardSnapshot();
        Card[] hand = player.getHand();
        LinkedList<Card> cards = new LinkedList<Card>(Arrays.asList(hand));
        boolean cardPlaced = false;
        for ( Card card : hand ){
            for ( Tile tile : board.getTiles() ){
                if ( tile.getCard() == null && (tile.getNorthCard() != null || tile.getSouthCard() != null || tile.getEastCard() != null || tile.getWestCard() != null)){
                    move.placeCard(card, tile);
                    if ( !move.isValidMove()){
                        move.pickupCardFromBoard(tile);
                    } else {
                        cards.remove(card);
                        cardPlaced = true;
                        break;
                    }
                }
            }
            if (cardPlaced){
                break;
            }
        }
        
        if ( move.getTilesUsed().length == 0 ){
            // There are no valid moves
            return;
        }
        
        
        
        while ( !cards.isEmpty() ){
            Card card = cards.pop();
            
            if ( move.getTilesUsed().length == 1 ){
                // We can try any position around the used tile - north, east, south west
                Tile origin = move.getTilesUsed()[0];
                Tile[] neighbours = new Tile[]{origin.getNorthTile(), origin.getEastTile(), origin.getSouthTile(), origin.getWestTile()};
                for ( Tile t : neighbours){
                    if ( t.getCard() != null ){
                        move.placeCard(card, t);
                        if ( !move.isValidMove() ){
                            move.pickupCardFromBoard(t);
                        } else {
                            break;
                        }
                    }
                }
                
            } else if ( move.getTilesUsed().length > 1 ){
                final Direction dir = move.getTilesUsed()[0].getRow() == move.getTilesUsed()[1].getRow() ? Direction.HORIZONTAL : Direction.VERTICAL;
                Collection<Tile> span = board.getContiguousFilledTiles(dir, move.getTilesUsed()[0].getRow(), move.getTilesUsed()[0].getCol());
                List<Tile> sortedTiles = new ArrayList<Tile>(span);
                Collections.sort(sortedTiles, new Comparator<Tile>(){

                    public int compare(Tile o1, Tile o2) {
                        switch ( dir){
                            case HORIZONTAL:
                                if ( o1.getCol() < o2.getCol() ) return -1;
                                else if ( o1.getCol() > o2.getCol() ) return 1;
                                else return 0;
                                
                            case VERTICAL:
                                if ( o1.getRow() < o2.getRow() ) return -1;
                                else if ( o1.getRow() > o2.getRow()) return 1;
                                else return 0;
                                
                        }
                        return 0;
                    }
                    
                });
                
                Tile firstTile = sortedTiles.get(0);
                Tile lastTile = sortedTiles.get(sortedTiles.size()-1);
                
                switch ( dir ){
                    case HORIZONTAL:
                        if ( firstTile.getWestTile() != null && firstTile.getWestCard() == null ){
                            move.placeCard(card, firstTile.getWestTile());
                            if ( move.isValidMove() ){
                                
                            } else {
                                move.pickupCardFromBoard(firstTile.getWestTile());
                            }
                        } else if ( lastTile.getEastTile() != null && lastTile.getEastCard() == null ){
                            move.placeCard(card, lastTile.getEastTile());
                            if ( move.isValidMove() ){
                                
                            } else {
                                move.pickupCardFromBoard(lastTile.getEastTile());
                            }
                            
                        }
                        break;
                    case VERTICAL:
                        if ( firstTile.getNorthTile() != null && firstTile.getNorthCard() == null ){
                            move.placeCard(card, firstTile.getNorthTile());
                            if ( move.isValidMove() ){
                                
                            } else {
                                move.pickupCardFromBoard(firstTile.getNorthTile());
                            }
                        } else if ( lastTile.getSouthTile() != null && lastTile.getSouthCard() == null ){
                            move.placeCard(card, lastTile.getSouthTile());
                            if ( move.isValidMove() ){
                                
                            } else {
                                move.pickupCardFromBoard(lastTile.getSouthTile());
                            }
                            
                        }
                        break;
                }
                
            }
        }
        
        
        
    }
    
}
