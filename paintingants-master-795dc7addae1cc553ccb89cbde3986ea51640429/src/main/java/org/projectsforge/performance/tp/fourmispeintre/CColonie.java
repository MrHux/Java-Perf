package org.projectsforge.performance.tp.fourmispeintre;

/*
 * CColonie.java
 *
 * Created on 11 avril 2007, 16:35
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */
import java.util.Vector;

public class CColonie implements Runnable {

  private Boolean mContinue = Boolean.TRUE;
  private Vector<CFourmi> mColonie;
  private PaintingAnts mApplis;

  /** Creates a new instance of CColonie */
  public CColonie(Vector<CFourmi> pColonie, PaintingAnts pApplis) {
    mColonie = pColonie;
    mApplis = pApplis;
  }

  public void pleaseStop() {
    mContinue = false;
  }

  @Override
  public void run() {

    while (mContinue == true) {
      if (!mApplis.getPause()) {
    	//TODO créer des threads qui font le deplacement (CFourmi en thread ? / des threads qui font seulement une partie de la boucle)
        for (int i = 0; i < mColonie.size(); i++) {
          mColonie.get(i).deplacer();
        }
      } else {

      }
    }
  }

}
