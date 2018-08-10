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
public interface IWindow {

public int getWindowColor();

public void setWindowColor(int newColor);

public String getShape();

public <T>T get(String url, Class<T> type, long id) throws Exception; 
}
