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

public class CColonie  implements Runnable {

  private Vector<CFourmi> mColonie;


  private Vector<CFourmi> fourmis;
  private int iNbFourmis;
  private int iNbThreads;
  private int iThread;
  public CColonie(Vector<CFourmi> fourmis, int nb_fourmis, int nb_threads, int thread) {
    this.fourmis = fourmis;
    this.iNbFourmis = nb_fourmis;
    this.iNbThreads = nb_threads;
    this.iThread = thread;
  }

  @Override
  public void run() { 
	while(true){
		int begin = (iNbFourmis/iNbThreads)*iThread;
		int end = begin + (iNbFourmis/iNbThreads);
		for(int i = begin ; i < end ; i++){
			fourmis.get(i).deplacer();
		}
	}
  }

}
