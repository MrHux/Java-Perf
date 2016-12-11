package org.projectsforge.performance.tp.fourmispeintre;

import java.util.Vector;

public class Thread_ForFourmis implements Runnable {

  private Vector<CFourmi> fourmis;
  private int iNbFourmis;
  private int iNbThreads;
  private int iThread;
  public Thread_ForFourmis(Vector<CFourmi> fourmis, int nb_fourmis, int nb_threads, int thread) {
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
