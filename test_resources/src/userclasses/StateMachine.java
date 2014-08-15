/**
 * Your application code goes here
 */

package userclasses;

import ca.weblite.scriblets.ComputerPlayer2;
import ca.weblite.scriblets.models.Card;
import ca.weblite.scriblets.models.Game;
import ca.weblite.scriblets.models.Player;
import ca.weblite.scriblets.models.Rank;
import ca.weblite.scriblets.models.Suit;
import ca.weblite.scriblets.views.BoardView;
import ca.weblite.scriblets.views.GameController;
import ca.weblite.scriblets.views.GameView;
import ca.weblite.scriblets.views.HandView;
import generated.StateMachineBase;
import com.codename1.ui.*; 
import com.codename1.ui.events.*;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.util.Resources;

/**
 *
 * @author Your name here
 */
public class StateMachine extends StateMachineBase {
    Game game;
    GameController controller;
    public StateMachine(String resFile) {
        super(resFile);
        // do not modify, write code in initVars and initialize class members there,
        // the constructor might be invoked too late due to race conditions that might occur
    }
    
    /**
     * this method should be used to initialize variables instead of
     * the constructor/class scope to avoid race conditions
     */
    protected void initVars(Resources res) {
        game = new Game();
       
        game.createMove(game.getPlayer(0));
        
    }


    @Override
    protected void beforeMain(final Form f) {
        final GameView gv = new GameView(game);
         controller = new GameController(game, gv);
        gv.setGameController(controller);
        
        f.addComponent(BorderLayout.CENTER, gv);
        
        
    }
}
