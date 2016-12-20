package org.projectsforge.performance.tp.fourmispeintre;
// package PaintingAnts_v3;
// version : 4.0

import java.awt.Color;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class CFourmi {
  // Tableau des incrémentations à effectuer sur la position des fourmis
  // en fonction de la direction du deplacement
  static private int[][] mIncDirection = new int[8][2];
  // le generateur aléatoire (Random est thread safe donc on la partage)
  private static Random GenerateurAleatoire = new Random();
  // couleur déposé par la fourmi
  private Color mCouleurDeposee;
  private float mLuminanceCouleurSuivie;
  // objet graphique sur lequel les fourmis peuvent peindre
  private CPainting mPainting;
  // Coordonées de la fourmi
  private int x, y;
  // Proba d'aller a gauche, en face, a droite, de suivre la couleur
  private float[] mProba = new float[4];
  // Numéro de la direction dans laquelle la fourmi regarde
  private int mDirection;
  // Taille de la trace de phéromones déposée par la fourmi
  private int mTaille;
  // Pas d'incrémentation des directions suivant le nombre de directions
  // allouées à la fourmies
  private int mDecalDir;
  // l'applet
  private PaintingAnts mApplis;
  // seuil de luminance pour la détection de la couleur recherchée
  private float mSeuilLuminance;
  // nombre de déplacements de la fourmi
  private AtomicLong mNbDeplacements;

  /*************************************************************************************************
  */
  public CFourmi(Color pCouleurDeposee, Color pCouleurSuivie, float pProbaTD, float pProbaG, float pProbaD,
      float pProbaSuivre, CPainting pPainting, char pTypeDeplacement, float pInit_x, float pInit_y, int pInitDirection,
      int pTaille, float pSeuilLuminance, PaintingAnts pApplis) {

    mCouleurDeposee = pCouleurDeposee;
    mLuminanceCouleurSuivie = 0.2426f * pCouleurDeposee.getRed() + 0.7152f * pCouleurDeposee.getGreen()
        + 0.0722f * pCouleurDeposee.getBlue();
    mPainting = pPainting;
    mApplis = pApplis;

    // direction de départ
    mDirection = pInitDirection;

    // taille du trait
    mTaille = pTaille;

    // initialisation des probas
    mProba[0] = pProbaG; // proba d'aller à gauche
    mProba[1] = pProbaTD; // proba d'aller tout droit
    mProba[2] = pProbaD; // proba d'aller à droite
    mProba[3] = pProbaSuivre; // proba de suivre la couleur

    // nombre de directions pouvant être prises : 2 types de déplacement
    // possibles
    if (pTypeDeplacement == 'd') {
      mDecalDir = 2;
    } else {
      mDecalDir = 1;
    }

    // initialisation du tableau des directions
    CFourmi.mIncDirection[0][0] = 0;
    CFourmi.mIncDirection[0][1] = -1;
    CFourmi.mIncDirection[1][0] = 1;
    CFourmi.mIncDirection[1][1] = -1;
    CFourmi.mIncDirection[2][0] = 1;
    CFourmi.mIncDirection[2][1] = 0;
    CFourmi.mIncDirection[3][0] = 1;
    CFourmi.mIncDirection[3][1] = 1;
    CFourmi.mIncDirection[4][0] = 0;
    CFourmi.mIncDirection[4][1] = 1;
    CFourmi.mIncDirection[5][0] = -1;
    CFourmi.mIncDirection[5][1] = 1;
    CFourmi.mIncDirection[6][0] = -1;
    CFourmi.mIncDirection[6][1] = 0;
    CFourmi.mIncDirection[7][0] = -1;
    CFourmi.mIncDirection[7][1] = -1;

    mSeuilLuminance = pSeuilLuminance;
    mNbDeplacements = new AtomicLong(0);
  }

  /*************************************************************************************************
   * Titre : void deplacer() Description : Fonction de deplacement de la fourmi
   *
   */
  public void deplacer() {
    float tirage, prob1, prob2, prob3, total;
    int[] dir = new int[3];
    int i, j;
    Color lCouleur;

    mNbDeplacements.incrementAndGet();

    dir[0] = 0;
    dir[1] = 0;
    dir[2] = 0;

    // le tableau dir contient 0 si la direction concernée ne contient pas la
    // couleur
    // à suivre, et 1 sinon (dir[0]=gauche, dir[1]=tt_droit, dir[2]=droite)
    i = modulo(x + CFourmi.mIncDirection[modulo(mDirection - mDecalDir, 8)][0], mPainting.getLargeur());
    j = modulo(y + CFourmi.mIncDirection[modulo(mDirection - mDecalDir, 8)][1], mPainting.getHauteur());
    if (mApplis.mBaseImage != null) {
      lCouleur = new Color(mApplis.mBaseImage.getRGB(i, j));
    } else {
      lCouleur = new Color(mPainting.getCouleur(i, j).getRGB());
    }
    if (testCouleur(lCouleur)) {
      dir[0] = 1;
    }

    i = modulo(x + CFourmi.mIncDirection[mDirection][0], mPainting.getLargeur());
    j = modulo(y + CFourmi.mIncDirection[mDirection][1], mPainting.getHauteur());
    if (mApplis.mBaseImage != null) {
      lCouleur = new Color(mApplis.mBaseImage.getRGB(i, j));
    } else {
      lCouleur = new Color(mPainting.getCouleur(i, j).getRGB());
    }
    if (testCouleur(lCouleur)) {
      dir[1] = 1;
    }
    i = modulo(x + CFourmi.mIncDirection[modulo(mDirection + mDecalDir, 8)][0], mPainting.getLargeur());
    j = modulo(y + CFourmi.mIncDirection[modulo(mDirection + mDecalDir, 8)][1], mPainting.getHauteur());
    if (mApplis.mBaseImage != null) {
      lCouleur = new Color(mApplis.mBaseImage.getRGB(i, j));
    } else {
      lCouleur = new Color(mPainting.getCouleur(i, j).getRGB());
    }
    if (testCouleur(lCouleur)) {
      dir[2] = 1;
    }

    // tirage d'un nombre aléatoire permettant de savoir si la fourmi va suivre
    // ou non la couleur
    tirage = GenerateurAleatoire.nextFloat();// Math.random();

    // la fourmi suit la couleur
    if (((tirage <= mProba[3]) && ((dir[0] + dir[1] + dir[2]) > 0)) || ((dir[0] + dir[1] + dir[2]) == 3)) {
      prob1 = (dir[0]) * mProba[0];
      prob2 = (dir[1]) * mProba[1];
      prob3 = (dir[2]) * mProba[2];
    }
    // la fourmi ne suit pas la couleur
    else {
      prob1 = (1 - dir[0]) * mProba[0];
      prob2 = (1 - dir[1]) * mProba[1];
      prob3 = (1 - dir[2]) * mProba[2];
    }
    total = prob1 + prob2 + prob3;
    prob1 = prob1 / total;
    prob2 = prob2 / total + prob1;
    prob3 = prob3 / total + prob2;

    // incrémentation de la direction de la fourmi selon la direction choisie
    tirage = GenerateurAleatoire.nextFloat();// Math.random();
    if (tirage < prob1) {
      mDirection = modulo(mDirection - mDecalDir, 8);
    } else {
      if (tirage < prob2) {
        /* rien, on va tout droit */
      } else {
        mDirection = modulo(mDirection + mDecalDir, 8);
      }
    }

    x += CFourmi.mIncDirection[mDirection][0];
    y += CFourmi.mIncDirection[mDirection][1];

    x = modulo(x, mPainting.getLargeur());
    y = modulo(y, mPainting.getHauteur());

    // coloration de la nouvelle position de la fourmi
    mPainting.setCouleur(x, y, mCouleurDeposee, mTaille);

    mApplis.IncrementFpsCounter();
  }

  /*************************************************************************************************
  */
  public long getNbDeplacements() {
    return mNbDeplacements.get();
  }

  /****************************************************************************/

  /*************************************************************************************************
  */
  public int getX() {
    return x;
  }

  /*************************************************************************************************
  */
  public int getY() {
    return y;
  }

  /*************************************************************************************************
   * Titre : modulo Description : Fcontion de modulo permettant au fourmi de
   * reapparaitre de l autre coté du Canvas lorsque qu'elle sorte de ce dernier
   *
   * @param x
   *          valeur
   *
   * @return int
   */
  private int modulo(int x, int m) {
    return (x + m) % m;
  }

  /*************************************************************************************************
   * Titre : boolean testCouleur() Description : fonction testant l'égalité
   * d'une couleur avec la couleur suivie
   *
   */
  private boolean testCouleur(Color pCouleur) {
    boolean lReponse = false;
    float lLuminance;

    /* on calcule la luminance */
    lLuminance = 0.2426f * pCouleur.getRed() + 0.7152f * pCouleur.getGreen() + 0.0722f * pCouleur.getBlue();

    /* test */
    if (Math.abs(mLuminanceCouleurSuivie - lLuminance) < mSeuilLuminance) {
      lReponse = true;
      // System.out.print(x);
    }

    return lReponse;
  }
}
