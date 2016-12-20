package org.projectsforge.performance.tp.fourmispeintre;
// package PaintingAnts_v2;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

// version : 2.0

import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Titre : Painting Ants
 * </p>
 * <p>
 * Description :
 * </p>
 * <p>
 * Copyright : Copyright (c) 2003
 * </p>
 * <p>
 * Société : Equipe Réseaux/TIC - Laboratoire d'Informatique de l'Université de
 * Tours
 * </p>
 *
 * @author Nicolas Monmarché
 * @version 1.0
 */

public class CPainting extends Canvas implements MouseListener {
	private static final long serialVersionUID = 1L;
	// matrice servant pour le produit de convolution
	static private final float[][] mMatriceConv9 = new float[3][3];
	static private final float[][] mMatriceConv25 = new float[5][5];
	static private final float[][] mMatriceConv49 = new float[7][7];

	// Objet de type Graphics permettant de manipuler l'affichage du Canvas
	public Graphics mGraphics;

	// tableau des couleurs, il permert de conserver en memoire l'état de chaque
	// pixel du canvas, ce qui est necessaire au deplacemet des fourmi
	// il sert aussi pour la fonction paint du Canvas
	private Color[][] mCouleurs;
	
	// couleur du fond
	private Color mCouleurFond = new Color(255, 255, 255);

	// dimensions
	private Dimension mDimension = new Dimension();

	private PaintingAnts mApplis;

	private boolean mSuspendu = false;

	/******************************************************************************
	 * Titre : public CPainting() Description : Constructeur de la classe
	 ******************************************************************************/
	public CPainting(Dimension pDimension, PaintingAnts pApplis) {
		int i, j;
		addMouseListener(this);

		mApplis = pApplis;

		mCouleurs = pApplis.mCouleurs;

		mDimension = pDimension;
		setBounds(new Rectangle(0, 0, mDimension.width, mDimension.height));

		this.setBackground(mCouleurFond);		

	}

	/******************************************************************************
	 * Titre : Color getCouleur Description : Cette fonction renvoie la couleur
	 * d'une case
	 ******************************************************************************/
	public Color getCouleur(int x, int y) {
		// TODO faire des variables atomic si possible
		return mCouleurs[x][y];

	}

	/******************************************************************************
	 * Titre : Color getDimension Description : Cette fonction renvoie la
	 * dimension de la peinture
	 ******************************************************************************/
	public Dimension getDimension() {
		return mDimension;
	}

	/******************************************************************************
	 * Titre : Color getHauteur Description : Cette fonction renvoie la hauteur
	 * de la peinture
	 ******************************************************************************/
	public int getHauteur() {
		return mDimension.height;
	}

	/******************************************************************************
	 * Titre : Color getLargeur Description : Cette fonction renvoie la hauteur
	 * de la peinture
	 ******************************************************************************/
	public int getLargeur() {
		return mDimension.width;
	}

	/******************************************************************************
	 * Titre : void init() Description : Initialise le fond a la couleur blanche
	 * et initialise le tableau des couleurs avec la couleur blanche
	 ******************************************************************************/
	public void init() {
		int i, j;
		mGraphics = getGraphics().create();
		PaintingAnts.g = mGraphics;
		//mGraphics.clearRect(0, 0, mDimension.width, mDimension.height);

		// initialisation de la matrice de convolution : lissage moyen sur 9
		// cases
		/*
		 * 1 2 1 2 4 2 1 2 1
		 */
		CPainting.mMatriceConv9[0][0] = 1 / 16f;
		CPainting.mMatriceConv9[0][1] = 2 / 16f;
		CPainting.mMatriceConv9[0][2] = 1 / 16f;
		CPainting.mMatriceConv9[1][0] = 2 / 16f;
		CPainting.mMatriceConv9[1][1] = 4 / 16f;
		CPainting.mMatriceConv9[1][2] = 2 / 16f;
		CPainting.mMatriceConv9[2][0] = 1 / 16f;
		CPainting.mMatriceConv9[2][1] = 2 / 16f;
		CPainting.mMatriceConv9[2][2] = 1 / 16f;

		// initialisation de la matrice de convolution : lissage moyen sur 25
		// cases
		/*
		 * 1 1 2 1 1 1 2 3 2 1 2 3 4 3 2 1 2 3 2 1 1 1 2 1 1
		 */
		CPainting.mMatriceConv25[0][0] = 1 / 44f;
		CPainting.mMatriceConv25[0][1] = 1 / 44f;
		CPainting.mMatriceConv25[0][2] = 2 / 44f;
		CPainting.mMatriceConv25[0][3] = 1 / 44f;
		CPainting.mMatriceConv25[0][4] = 1 / 44f;
		CPainting.mMatriceConv25[1][0] = 1 / 44f;
		CPainting.mMatriceConv25[1][1] = 2 / 44f;
		CPainting.mMatriceConv25[1][2] = 3 / 44f;
		CPainting.mMatriceConv25[1][3] = 2 / 44f;
		CPainting.mMatriceConv25[1][4] = 1 / 44f;
		CPainting.mMatriceConv25[2][0] = 2 / 44f;
		CPainting.mMatriceConv25[2][1] = 3 / 44f;
		CPainting.mMatriceConv25[2][2] = 4 / 44f;
		CPainting.mMatriceConv25[2][3] = 3 / 44f;
		CPainting.mMatriceConv25[2][4] = 2 / 44f;
		CPainting.mMatriceConv25[3][0] = 1 / 44f;
		CPainting.mMatriceConv25[3][1] = 2 / 44f;
		CPainting.mMatriceConv25[3][2] = 3 / 44f;
		CPainting.mMatriceConv25[3][3] = 2 / 44f;
		CPainting.mMatriceConv25[3][4] = 1 / 44f;
		CPainting.mMatriceConv25[4][0] = 1 / 44f;
		CPainting.mMatriceConv25[4][1] = 1 / 44f;
		CPainting.mMatriceConv25[4][2] = 2 / 44f;
		CPainting.mMatriceConv25[4][3] = 1 / 44f;
		CPainting.mMatriceConv25[4][4] = 1 / 44f;

		// initialisation de la matrice de convolution : lissage moyen sur 49
		// cases
		/*
		 * 1 1 2 2 2 1 1 1 2 3 4 3 2 1 2 3 4 5 4 3 2 2 4 5 8 5 4 2 2 3 4 5 4 3 2
		 * 1 2 3 4 3 2 1 1 1 2 2 2 1 1
		 */
		CPainting.mMatriceConv49[0][0] = 1 / 128f;
		CPainting.mMatriceConv49[0][1] = 1 / 128f;
		CPainting.mMatriceConv49[0][2] = 2 / 128f;
		CPainting.mMatriceConv49[0][3] = 2 / 128f;
		CPainting.mMatriceConv49[0][4] = 2 / 128f;
		CPainting.mMatriceConv49[0][5] = 1 / 128f;
		CPainting.mMatriceConv49[0][6] = 1 / 128f;

		CPainting.mMatriceConv49[1][0] = 1 / 128f;
		CPainting.mMatriceConv49[1][1] = 2 / 128f;
		CPainting.mMatriceConv49[1][2] = 3 / 128f;
		CPainting.mMatriceConv49[1][3] = 4 / 128f;
		CPainting.mMatriceConv49[1][4] = 3 / 128f;
		CPainting.mMatriceConv49[1][5] = 2 / 128f;
		CPainting.mMatriceConv49[1][6] = 1 / 128f;

		CPainting.mMatriceConv49[2][0] = 2 / 128f;
		CPainting.mMatriceConv49[2][1] = 3 / 128f;
		CPainting.mMatriceConv49[2][2] = 4 / 128f;
		CPainting.mMatriceConv49[2][3] = 5 / 128f;
		CPainting.mMatriceConv49[2][4] = 4 / 128f;
		CPainting.mMatriceConv49[2][5] = 3 / 128f;
		CPainting.mMatriceConv49[2][6] = 2 / 128f;

		CPainting.mMatriceConv49[3][0] = 2 / 128f;
		CPainting.mMatriceConv49[3][1] = 4 / 128f;
		CPainting.mMatriceConv49[3][2] = 5 / 128f;
		CPainting.mMatriceConv49[3][3] = 8 / 128f;
		CPainting.mMatriceConv49[3][4] = 5 / 128f;
		CPainting.mMatriceConv49[3][5] = 4 / 128f;
		CPainting.mMatriceConv49[3][6] = 2 / 128f;

		CPainting.mMatriceConv49[4][0] = 2 / 128f;
		CPainting.mMatriceConv49[4][1] = 3 / 128f;
		CPainting.mMatriceConv49[4][2] = 4 / 128f;
		CPainting.mMatriceConv49[4][3] = 5 / 128f;
		CPainting.mMatriceConv49[4][4] = 4 / 128f;
		CPainting.mMatriceConv49[4][5] = 3 / 128f;
		CPainting.mMatriceConv49[4][6] = 2 / 128f;

		CPainting.mMatriceConv49[5][0] = 1 / 128f;
		CPainting.mMatriceConv49[5][1] = 2 / 128f;
		CPainting.mMatriceConv49[5][2] = 3 / 128f;
		CPainting.mMatriceConv49[5][3] = 4 / 128f;
		CPainting.mMatriceConv49[5][4] = 3 / 128f;
		CPainting.mMatriceConv49[5][5] = 2 / 128f;
		CPainting.mMatriceConv49[5][6] = 1 / 128f;

		CPainting.mMatriceConv49[6][0] = 1 / 128f;
		CPainting.mMatriceConv49[6][1] = 1 / 128f;
		CPainting.mMatriceConv49[6][2] = 2 / 128f;
		CPainting.mMatriceConv49[6][3] = 2 / 128f;
		CPainting.mMatriceConv49[6][4] = 2 / 128f;
		CPainting.mMatriceConv49[6][5] = 1 / 128f;
		CPainting.mMatriceConv49[6][6] = 1 / 128f;

		mSuspendu = false;
	}

	/****************************************************************************/
	@Override
	public void mouseClicked(MouseEvent pMouseEvent) {
		pMouseEvent.consume();
		if (pMouseEvent.getButton() == MouseEvent.BUTTON1) {
			// double clic sur le bouton gauche = effacer et recommencer
			if (pMouseEvent.getClickCount() == 2) {
				init();
			}
			// simple clic = suspendre les calculs et l'affichage
			mApplis.pause();
		} else {
			// bouton du milieu (roulette) = suspendre l'affichage mais
			// continuer les calculs
			if (pMouseEvent.getButton() == MouseEvent.BUTTON2) {
				suspendre();
			} else {
				// clic bouton droit = effacer et recommencer
				// case pMouseEvent.BUTTON3:
				init();
			}
		}
	}

	/****************************************************************************/
	@Override
	public void mouseEntered(MouseEvent pMouseEvent) {
	}

	/****************************************************************************/
	@Override
	public void mouseExited(MouseEvent pMouseEvent) {
	}

	/****************************************************************************/
	@Override
	public void mousePressed(MouseEvent pMouseEvent) {

	}

	/****************************************************************************/
	@Override
	public void mouseReleased(MouseEvent pMouseEvent) {
	}

	/******************************************************************************
	 * Titre : void paint(Graphics g) Description : Surcharge de la fonction qui
	 * est appelé lorsque le composant doit être redessiné
	 ******************************************************************************/
	@Override
	public void paint(Graphics pGraphics) {

		int i, j;

		for (i = 0; i < mDimension.width; i++) {
			for (j = 0; j < mDimension.height; j++) {
				pGraphics.setColor(mCouleurs[i][j]);
				pGraphics.fillRect(i, j, 1, 1);

			}
		}
	}

	/******************************************************************************
	 * Titre : void colorer_case(int x, int y, Color c) Description : Cette
	 * fonction va colorer le pixel correspondant et mettre a jour le tabmleau
	 * des couleurs
	 ******************************************************************************/
	public void setCouleur(int x, int y, Color c, int pTaille) {
		int i, j, k, l, m, n;
		float R, G, B;
		Color lColor;
		
		mCouleurs[x][y] = c;
		PaintingAnts.image.setRGB(x, y, mCouleurs[x][y].getRGB());
		// on fait diffuser la couleur :
		switch (pTaille) {
		case 0:
			// on ne fait rien = pas de diffusion
			break;
		case 1:
			// produit de convolution discrete sur 9 cases
			doConvolution(x, y, CPainting.mMatriceConv9, 3, 3, 1);
			break;
		case 2:
			// produit de convolution discrete sur 25 cases
			doConvolution(x, y, CPainting.mMatriceConv25, 5, 5, 2);
			break;
		case 3:
			// produit de convolution discrete sur 49 cases
			doConvolution(x, y, CPainting.mMatriceConv49, 7, 7, 3);
			break;
		}// end switch

	}

	private void doConvolution(int x, int y, float[][] matricConvolution, int nbColumnMatrice, int nbRowMatrice,
			int constant) {
		int i, j, k, l, m, n;
		float R, G, B;
		Color lColor;

		// produit de convolution discrete
		for (i = 0; i < nbColumnMatrice; i++) {
			for (j = 0; j < nbRowMatrice; j++) {
				R = G = B = 0f;

				m = (x + i - constant + mDimension.width);
				n = (y + j - constant + mDimension.height);

				for (k = 0; k < 7; k++) {
					for (l = 0; l < 7; l++) {
						int m1 = (m + k - constant) % mDimension.width;
						int n2 = (n + l - constant) % mDimension.height;
						R += matricConvolution[k][l] * mCouleurs[m1][n2].getRed();
						G += matricConvolution[k][l] * mCouleurs[m1][n2].getGreen();
						B += matricConvolution[k][l] * mCouleurs[m1][n2].getBlue();
					}
				}

				lColor = new Color((int) R, (int) G, (int) B);

				m = m % mDimension.width;
				n = n % mDimension.height;
				mCouleurs[m][n] = lColor;
				PaintingAnts.image.setRGB(m, n, mCouleurs[m][n].getRGB());

			}
		}
	}

	/******************************************************************************
	 * Titre : setSupendu Description : Cette fonction change l'état de
	 * suspension
	 ******************************************************************************/

	public void suspendre() {
		mSuspendu = !mSuspendu;
		if (!mSuspendu) {
			repaint();
		}
	}
}
