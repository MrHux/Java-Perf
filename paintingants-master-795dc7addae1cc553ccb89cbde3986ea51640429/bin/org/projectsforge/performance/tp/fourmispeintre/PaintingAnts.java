package org.projectsforge.performance.tp.fourmispeintre;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.VolatileImage;
import java.awt.image.WritableRaster;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PaintingAnts extends java.applet.Applet implements Runnable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * @param pool
	 */
	public static void shutdownAndAwaitTermination(ExecutorService pool) {
		pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
					System.err.println("Pool did not terminate");
				}
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Code ajouter
	 */
	public final int cores = Runtime.getRuntime().availableProcessors();
	
	private final ExecutorService executor = Executors.newFixedThreadPool(cores);	
	
	public static volatile ReentrantLock canDoFillRect = new ReentrantLock();
	
	// parametres
	private int mLargeur;

	private int mHauteur;
	// l'objet graphique lui meme
	private CPainting mPainting;

	// les fourmis
	private final Vector<CFourmi> mColonie = new Vector<>();

	private Thread mApplis;

	private Dimension mDimension;

	private boolean mPause = false;

	public BufferedImage mBaseImage;

	private StatisticsHandler statisticsHandler;
	
	/**
	 * Modification de Code Java Perf
	 */
	// tableau des couleurs, il permert de conserver en memoire l'état de chaque
	// pixel du canvas, ce qui est necessaire au deplacemet des fourmi
	// il sert aussi pour la fonction paint du Canvas
	public volatile Color[][] mCouleurs;
	public static volatile BufferedImage image; 
	public static Graphics g;
	/**
	 * FIn modification
	 */
	
	// couleur du fond
	private Color mCouleurFond = new Color(255, 255, 255);

	/****************************************************************************/
	/**
	 * Détruire l'applet
	 *
	 */
	@Override
	public void destroy() {
		// System.out.println(this.getName()+ ":destroy()");

		if (mApplis != null) {
			mApplis = null;
		}
	}

	/****************************************************************************/
	/**
	 * Obtenir l'information Applet
	 *
	 */
	@Override
	public String getAppletInfo() {
		return "Painting Ants";
	}

	/****************************************************************************/
	/**
	 * Obtenir l'information Applet
	 *
	 */

	@Override
	public String[][] getParameterInfo() {
		String[][] lInfo = { { "SeuilLuminance", "string", "Seuil de luminance" }, { "Img", "string", "Image" },
				{ "NbFourmis", "string", "Nombre de fourmis" }, { "Fourmis", "string",
						"Paramètres des fourmis (RGB_déposée)(RGB_suivie)(x,y,direction,taille)(TypeDeplacement,ProbaG,ProbaTD,ProbaD,ProbaSuivre);...;" } };
		return lInfo;
	}

	/****************************************************************************/
	/**
	 * Obtenir l'état de pause
	 *
	 */
	public boolean getPause() {
		return mPause;
	}

	public void IncrementFpsCounter() {
		statisticsHandler.incrementFpsCounter();
	}

	/****************************************************************************/
	/**
	 * Initialisation de l'applet
	 *
	 */
	@Override
	public void init() {
		URL lFileName;
		URLClassLoader urlLoader = (URLClassLoader) this.getClass().getClassLoader();

			
		// lecture des parametres de l'applet

		mDimension = getSize();
		mLargeur = mDimension.width;
		mHauteur = mDimension.height;
		
		/**
		 * Modification de code Perf
		 */
		// initialisation de la matrice des couleurs
		
		
		mCouleurs = new Color[mDimension.width][mDimension.height];
		for (int i = 0; i != mDimension.width; i++) {
			for (int j = 0; j != mDimension.height; j++) {
				mCouleurs[i][j] = new Color(mCouleurFond.getRed(), mCouleurFond.getGreen(), mCouleurFond.getBlue());
			}
		}
		image = new BufferedImage(mDimension.width, mDimension.height, BufferedImage.TYPE_INT_ARGB);
		

		/**
		 * FIN  Modification de code Perf
		 */
		
		mPainting = new CPainting(mDimension, this);
		add(mPainting);

		// lecture de l'image
		lFileName = urlLoader.findResource("images/" + getParameter("Img"));
		try {
			if (lFileName != null) {
				mBaseImage = javax.imageio.ImageIO.read(lFileName);
			}
		} catch (java.io.IOException ex) {
		}

		if (mBaseImage != null) {
			mLargeur = mBaseImage.getWidth();
			mHauteur = mBaseImage.getHeight();
			mDimension.setSize(mLargeur, mHauteur);
			resize(mDimension);
		}

		readParameterFourmis();

		setLayout(null);

		statisticsHandler = new StatisticsHandler();	
		
	}

	/****************************************************************************/
	/**
	 * Paint the image and all active highlights.
	 */
	@Override
	public void paint(Graphics g) {

		if (mBaseImage == null) {
			return;
		}
		g.drawImage(mBaseImage, 0, 0, this);
	}

	/****************************************************************************/
	/**
	 * Mettre en pause
	 *
	 */
	public void pause() {
		mPause = !mPause;
	}

	// =========================================================================
	// cette fonction analyse une chaine :
	// si pStr est un nombre : sa valeur est retournée
	// si pStr est un interval x..y : une valeur au hasard dans [x,y] est
	// retournée
	private float readFloatParameter(String pStr) {
		float lMin, lMax, lResult;
		// System.out.println(" chaine pStr: "+pStr);
		StringTokenizer lStrTok = new StringTokenizer(pStr, ":");
		// on lit une premiere valeur
		lMin = Float.valueOf(lStrTok.nextToken()).floatValue();
		// System.out.println(" lMin: "+lMin);
		lResult = lMin;
		// on essaye d'en lire une deuxieme
		try {
			lMax = Float.valueOf(lStrTok.nextToken()).floatValue();
			// System.out.println(" lMax: "+lMax);
			if (lMax > lMin) {
				// on choisit un nombre entre lMin et lMax
				lResult = (float) (Math.random() * (lMax - lMin)) + lMin;
			}
		} catch (java.util.NoSuchElementException e) {
			// il n'y pas de deuxieme nombre et donc le nombre retourné
			// correspond au
			// premier nombre
		}
		return lResult;
	}

	// =========================================================================
	// cette fonction analyse une chaine :
	// si pStr est un nombre : sa valeur est retournée
	// si pStr est un interval x..y : une valeur au hasard dans [x,y] est
	// retournée
	private int readIntParameter(String pStr) {
		int lMin, lMax, lResult;
		StringTokenizer lStrTok = new StringTokenizer(pStr, ":");
		// on lit une premiere valeur
		lMin = Integer.valueOf(lStrTok.nextToken()).intValue();
		lResult = lMin;
		// on essaye d'en lire une deuxieme
		try {
			lMax = Integer.valueOf(lStrTok.nextToken()).intValue();
			if (lMax > lMin) {
				// on choisit un nombre entre lMin et lMax
				lResult = (int) (Math.random() * (lMax - lMin + 1)) + lMin;
			}
		} catch (java.util.NoSuchElementException e) {
			// il n'y pas de deuxieme nombre et donc le nombre retourné
			// correspond au
			// premier nombre
		}
		return lResult;
	}

	// =========================================================================
	// lecture des paramètres de l'applet
	private void readParameterFourmis() {
		String lChaine;
		int R, G, B;
		Color lCouleurDeposee, lCouleurSuivie;
		CFourmi lFourmi;
		float lProbaTD, lProbaG, lProbaD, lProbaSuivre, lSeuilLuminance;
		char lTypeDeplacement = ' ';
		int lInitDirection, lTaille;
		float lInit_x, lInit_y;
		int lNbFourmis;

		lChaine = getParameter("SeuilLuminance");
		if (lChaine != null) {
			// récupération du seuil de luminance
			lSeuilLuminance = readFloatParameter(lChaine);
		} else {
			lSeuilLuminance = 40f;
		}

		// Lecture des paramètres des fourmis
		lChaine = getParameter("Fourmis");
		if (lChaine != null) {

			// on affiche la chaine de parametres
			System.out.println("Paramètres:" + lChaine);

			// on va compter le nombre de fourmis dans la chaine de parametres :
			lNbFourmis = 0;
			// chaine de paramètres pour une fourmi
			StringTokenizer lSTFourmi = new StringTokenizer(lChaine, ";");
			while (lSTFourmi.hasMoreTokens()) {
				// chaine de parametres de couleur et proba
				StringTokenizer lSTParam = new StringTokenizer(lSTFourmi.nextToken(), "()");
				// lecture de la couleur déposée
				StringTokenizer lSTCouleurDéposée = new StringTokenizer(lSTParam.nextToken(), ",");
				R = readIntParameter(lSTCouleurDéposée.nextToken());
				if (R == -1) {
					R = (int) (Math.random() * 256);
				}

				G = readIntParameter(lSTCouleurDéposée.nextToken());
				if (G == -1) {
					G = (int) (Math.random() * 256);
				}
				B = readIntParameter(lSTCouleurDéposée.nextToken());
				if (B == -1) {
					B = (int) (Math.random() * 256);
				}
				lCouleurDeposee = new Color(R, G, B);
				System.out.print("Parametres de la fourmi " + lNbFourmis + ":(" + R + "," + G + "," + B + ")");

				// lecture de la couleur suivie
				StringTokenizer lSTCouleurSuivi = new StringTokenizer(lSTParam.nextToken(), ",");
				R = readIntParameter(lSTCouleurSuivi.nextToken());
				G = readIntParameter(lSTCouleurSuivi.nextToken());
				B = readIntParameter(lSTCouleurSuivi.nextToken());
				lCouleurSuivie = new Color(R, G, B);
				System.out.print("(" + R + "," + G + "," + B + ")");

				// lecture de la position de la direction de départ et de la
				// taille de
				// la trace
				StringTokenizer lSTDéplacement = new StringTokenizer(lSTParam.nextToken(), ",");
				lInit_x = readFloatParameter(lSTDéplacement.nextToken());
				if (lInit_x < 0.0 || lInit_x > 1.0) {
					lInit_x = (float) Math.random();
				}
				lInit_y = readFloatParameter(lSTDéplacement.nextToken());
				if (lInit_y < 0.0 || lInit_y > 1.0) {
					lInit_y = (float) Math.random();
				}
				lInitDirection = readIntParameter(lSTDéplacement.nextToken());
				if (lInitDirection < 0 || lInitDirection > 7) {
					lInitDirection = (int) (Math.random() * 8);
				}
				lTaille = readIntParameter(lSTDéplacement.nextToken());
				if (lTaille < 0 || lTaille > 3) {
					lTaille = (int) (Math.random() * 4);
				}
				System.out.print("(" + lInit_x + "," + lInit_y + "," + lInitDirection + "," + lTaille + ")");

				// lecture des probas
				StringTokenizer lSTProbas = new StringTokenizer(lSTParam.nextToken(), ",");
				lTypeDeplacement = lSTProbas.nextToken().charAt(0);
				// System.out.println(" lTypeDeplacement:"+lTypeDeplacement);

				if (lTypeDeplacement != 'o' && lTypeDeplacement != 'd') {
					if (Math.random() < 0.5) {
						lTypeDeplacement = 'o';
					} else {
						lTypeDeplacement = 'd';
					}
				}

				lProbaG = readFloatParameter(lSTProbas.nextToken());
				lProbaTD = readFloatParameter(lSTProbas.nextToken());
				lProbaD = readFloatParameter(lSTProbas.nextToken());
				lProbaSuivre = readFloatParameter(lSTProbas.nextToken());
				// on normalise au cas ou
				float lSomme = lProbaG + lProbaTD + lProbaD;
				lProbaG /= lSomme;
				lProbaTD /= lSomme;
				lProbaD /= lSomme;

				System.out.println("(" + lTypeDeplacement + "," + lProbaG + "," + lProbaTD + "," + lProbaD + ","
						+ lProbaSuivre + ");");

				// création de la fourmi
				lFourmi = new CFourmi(lCouleurDeposee, lCouleurSuivie, lProbaTD, lProbaG, lProbaD, lProbaSuivre,
						mPainting, lTypeDeplacement, lInit_x, lInit_y, lInitDirection, lTaille, lSeuilLuminance, this);
				mColonie.addElement(lFourmi);
				lNbFourmis++;
			}
		} else // initialisation aléatoire des fourmis
		{

			int i;
			lNbFourmis = (int) (Math.random() * 5) + 2;
			System.out.print("Nombre de fourmis aléa:" + lNbFourmis + "\n");
			Color lTabColor[] = new Color[lNbFourmis];
			int lColor;
			lSeuilLuminance = 40f;
			System.out.println("Seuil de luminance:" + lSeuilLuminance);

			// initialisation aléatoire de la couleur de chaque fourmi
			for (i = 0; i < lNbFourmis; i++) {
				R = (int) (Math.random() * 256);
				G = (int) (Math.random() * 256);
				B = (int) (Math.random() * 256);
				lTabColor[i] = new Color(R, G, B);
			}

			// construction des fourmis
			for (i = 0; i < lNbFourmis; i++) {
				// la couleur suivie est la couleur d'une autre fourmi
				lColor = (int) (Math.random() * lNbFourmis);
				if (i == lColor) {
					lColor = (lColor + 1) % lNbFourmis;
				}

				// une chance sur deux d'avoir un déplacement perpendiculaire
				if ((float) Math.random() < 0.5f) {
					lTypeDeplacement = 'd';
				} else {
					lTypeDeplacement = 'o';
				}

				// position initiale
				lInit_x = (float) (Math.random()); // *mPainting.getLargeur()
				lInit_y = (float) (Math.random()); // *mPainting.getHauteur()

				// direction initiale
				lInitDirection = (int) (Math.random() * 8);

				// taille du trait
				lTaille = (int) (Math.random() * 4);

				// proba de déplacement :
				lProbaTD = (float) (Math.random());
				lProbaG = (float) (Math.random() * (1.0 - lProbaTD));
				lProbaD = (float) (1.0 - (lProbaTD + lProbaG));
				lProbaSuivre = (float) (0.5 + 0.5 * Math.random());

				System.out.print("Random:(" + lTabColor[i].getRed() + "," + lTabColor[i].getGreen() + ","
						+ lTabColor[i].getBlue() + ")");
				System.out.print("(" + lTabColor[lColor].getRed() + "," + lTabColor[lColor].getGreen() + ","
						+ lTabColor[lColor].getBlue() + ")");
				System.out.print("(" + lInit_x + "," + lInit_y + "," + lInitDirection + "," + lTaille + ")");
				System.out.println("(" + lTypeDeplacement + "," + lProbaG + "," + lProbaTD + "," + lProbaD + ","
						+ lProbaSuivre + ");");

				// création et ajout de la fourmi dans la colonie
				lFourmi = new CFourmi(lTabColor[i], lTabColor[lColor], lProbaTD, lProbaG, lProbaD, lProbaSuivre,
						mPainting, lTypeDeplacement, lInit_x, lInit_y, lInitDirection, lTaille, lSeuilLuminance, this);
				mColonie.addElement(lFourmi);
			}
		}
		// on affiche le nombre de fourmis
		 System.out.println("Nombre de Fourmis:"+lNbFourmis);
	}

	@Override
	public void run() {
		String lMessage;

		mPainting.init();
		int cores = Runtime.getRuntime().availableProcessors();
		
		Thread currentThread = Thread.currentThread();
		try {
			for (int i = 0; i < cores; i++) {
				executor.submit(new CColonie(mColonie, mColonie.size() ,cores, i));
			}
			while (mApplis == currentThread) {
				if (mPause) {
					lMessage = "pause";
					Logger.getLogger("log").log(Level.INFO, "Pause", "");
					executor.shutdownNow();//on arrête tout
				} else {
					lMessage = "running (" + statisticsHandler.getLastFPS() + ") ";
					g.drawImage(image, 0, 0, this);
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				showStatus(lMessage);

				try {
					Thread.sleep(2);
				} catch (InterruptedException e) {
					showStatus(e.toString());
				}
			}
			if (!executor.isShutdown()) {
				executor.shutdownNow();
				shutdownAndAwaitTermination(executor);// gére l'intéruption normalement
			}
		} catch (RejectedExecutionException e) {
			executor.shutdownNow();
		}
	}

	/****************************************************************************/
	/**
	 * Lancer l'applet
	 *
	 */
	@Override
	public void start() {

		showStatus("starting...");
		statisticsHandler.start();

		// Create the thread.
		mApplis = new Thread(this);
		// and let it start running
		mApplis.setPriority(Thread.MIN_PRIORITY);
		mApplis.start();
	}

	/****************************************************************************/
	/**
	 * Arrêter l'applet
	 *
	 */
	@Override
	public void stop() {
		showStatus("stopped...");

		// On demande au Thread Colony de s'arreter et on attend qu'il s'arrete
		executor.shutdownNow();
		
		shutdownAndAwaitTermination(executor);// gére l'intéruption normalement

		statisticsHandler.stop();

		mApplis = null;
	}
}
