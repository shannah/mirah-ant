/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets;

import ca.weblite.scriblets.models.Board.Tile;
import ca.weblite.scriblets.models.Card;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author shannah
 */
public class Util {
    public static Collection<Card> getCards(Collection<Tile> tiles){
        Collection<Card> out = new ArrayList<Card>(tiles.size());
        for ( Tile tile : tiles ){
            if ( tile.getCard() != null ){
                out.add(tile.getCard());
            }
        }
        return out;
    }
}
