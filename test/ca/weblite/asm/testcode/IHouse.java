/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.weblite.asm.testcode;

/**
 *
 * @author shannah
 */
import java.util.List;

public interface IHouse<W extends IWindow> {

public int getHouseColor();

public void setHouseColor(int color);

public List<W> getWindows(); 

public void setWindows(List<W> windows);

}
