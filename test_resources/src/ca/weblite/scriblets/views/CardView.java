/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ca.weblite.scriblets.views;

import ca.weblite.scriblets.models.Card;
import com.codename1.io.Log;
import com.codename1.ui.Component;
import com.codename1.ui.Container;
import com.codename1.ui.Form;
import com.codename1.ui.Graphics;
import com.codename1.ui.Image;
import com.codename1.ui.geom.Dimension;
import com.codename1.ui.layouts.CoordinateLayout;

/**
 *
 * @author shannah
 */
public class CardView extends Component {
    private final Card card;
    private boolean dragInProgress = false;
    private Container previousParent = null;
    
    public CardView(Card card){
        this.card = card;
        this.setDraggable(true);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g); //To change body of generated methods, choose Tools | Templates.
        Image img = getCard().getIcon(this.getPreferredSize());
        if ( img != null ){
            g.drawImage(img, getX(), getY());
        } else {
            g.setColor(0xffffff);
            g.drawString(getCard().getShortLabel(), getX(), getY());
        }
    }

    /**
     * @return the card
     */
    public Card getCard() {
        return card;
    }

    
    
    
    
    
    
    
    
    
    
    
}
