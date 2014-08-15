/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.views;

import ca.weblite.scriblets.models.Card;
import ca.weblite.scriblets.models.Player;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Display;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.Layout;


/**
 *
 * @author shannah
 */
public class HandView extends Container {
    private final Player player;
    private GameController controller;
    
    public HandView(Player player){
        this.player = player;
        this.setLayout(new HandLayout());
        this.setUIID("HandView");
        this.setDropTarget(true);
        updateHand();
    }
    
    public void setGameController(GameController controller){
        this.controller = controller;
    }
    
    public final void updateHand(){
        this.removeAll();
        Card[] cards = getPlayer().getHand();
        int len = cards.length;
        
        
        
        for ( int i=0; i<len; i++){
            if ( cards[i] != null ){
                CardView cv = new CardView(cards[i]);
                
                this.addComponent(new CardView(cards[i]));
            }
        }
    }
    
    
    private int calcCardWidth(){
        int displayWidth = Display.getInstance().getDisplayWidth();
        int displayHeight = Display.getInstance().getDisplayHeight();
        if ( displayWidth > displayHeight ){
            return getHeight()/Player.MAX_CARDS;
        } else {
            return getWidth()/Player.MAX_CARDS;
        }
    }

    /**
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
    
    private class HandLayout extends Layout {

        @Override
        public void layoutContainer(Container parent) {
            int displayWidth = Display.getInstance().getDisplayWidth();
            int displayHeight = Display.getInstance().getDisplayHeight();
            
            int cardWidth = calcCardWidth();
            int len = parent.getComponentCount();
            int x = 0;
            int y = 0;
            for ( int i=0; i<len; i++){
                Component c = parent.getComponentAt(i);
                if ( c instanceof CardView ){
                    CardView cv = (CardView)c;
                    cv.setWidth(cardWidth);
                    cv.setHeight(cardWidth);
                    if ( displayWidth > displayHeight ){
                        cv.setX(y);
                        cv.setY(x);
                    } else {
                        cv.setX(x);
                        cv.setY(y);
                    }
                    x+= cardWidth;
                    
                }
            }
            
            
        }

        @Override
        public Dimension getPreferredSize(Container parent) {
            int w = calcCardWidth();
            return new Dimension(w*5,w);
        }
        
    }

    @Override
    protected boolean draggingOver(Component dragged, int x, int y) {
        if ( dragged instanceof CardView ){
            if ( controller != null ){
                return controller.draggingCardOverHand((CardView)dragged, this,  x,  y);
            }
        }
        return false;
    }

    
    
    @Override
    public void drop(Component dragged, int x, int y) {
        if ( dragged instanceof CardView ){
            if ( controller != null ){
                controller.cardDroppedOnHand((CardView)dragged, this, x, y);
            }
        }
        
    }
    
    
    
    
    
    
}
