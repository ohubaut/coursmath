// import les package utilises
import java.awt.* ;
import java.util.* ;
import java.applet.* ;
// package personnel : objet graphique
import packsprite.* ;

/* objet pyramide2
 * applet principale
 * optimisation du code :
 * - le maximum de clas sont declare final
 * - le code est compile avec option -O
 */
final public class pyramide2 extends Applet implements Runnable {

  /* declaration des variables */
  // canvas animation
  protected animation anim ;
  // barre de defilement permettant de definir le nombre de rangee
  protected Scrollbar sb ;
  // nombre de rangee, par defaut 8
  static int NbRangee = 8 ;
  // label indiquent le nombre de rangee visuellement pour l'utilisateur
  protected Label indicationNombreRangee ;
  private Thread threadGarbageCollector ;

  /* initialisation de l'applet */
public void init() {
  // sert a vider les objets de la memoires par appels recursifs de la methode vider jusqu'a ce que la memoire soit vide
  threadGarbageCollector = new Thread(this) ;
  threadGarbageCollector.start() ;
  // definit la couleur d'arriere plan
  setBackground(Color.white) ;
  // recupere du document HTML le nombre de rangee
  String nb = getParameter("nombre_rangee") ;
  if (nb != null) NbRangee = Integer.valueOf(nb).intValue() ;
  // cree un paneau de controle contenant tous les boutons utils a l'utilisateur
  Panel paneau1 = new Panel() ;
  paneau1.setBackground(Color.white) ;
  // bouton start permettant de demarrer une nouvelle animation
  paneau1.add(new Button("Start")) ;
  // bouton pause permettant de stoper momentanement une animation
  paneau1.add(new Button("Pause")) ;
  // bouton continue pour continue une animation deja debute
  paneau1.add(new Button("Continue")) ;
  // barre de defilement(nombre de rangee) : valeur max et min 2 et 16
  sb = new Scrollbar(Scrollbar.HORIZONTAL, NbRangee, 2, 2, 16) ;
  paneau1.add(sb) ;
  // label definissant le nombre de rangee visuellement pour l'utilisateur
  indicationNombreRangee = new Label("n = "+Long.toString((long)sb.getValue())) ;
  paneau1.add(indicationNombreRangee) ;
  // ajoute a l'applet le paneau de controle et l'animation
  add("North", paneau1) ;
  anim = new animation() ;
  add("Center", anim) ;
  anim.demarrage(NbRangee) ;
  anim.actif = true ;
}

  /* sert a vider les objets du Garbage collector 
     methode bourrin mais qui marche tres bien */
public void run() {
  /*
  while(threadGarbageCollector!=null) {
    viderGC() ;
    try {
      threadGarbageCollector.sleep(20000) ;
    } catch (Exception e) {System.out.println(e);}
  }
  */
}
  /* sert a vider les objets du Garbage collector 
     methode bourrin mais qui marche tres bien */
public static void viderGC() {
  Runtime rt = Runtime.getRuntime() ;
  long vide=rt.freeMemory() ;
  long aVide ;
  do {
    aVide=vide ;
    rt.gc() ;
    vide=rt.freeMemory() ;
  } while (vide>aVide) ;
  rt.runFinalization() ;
}


  /* gere les evenements utilisateur */
public boolean handleEvent(Event e) {
  // si la barre de defilement est modifie...
  if (e.target instanceof Scrollbar) {
    // definit le label du nombre de range
    long nouveauNrangee = (long)(sb.getValue()/2)*2 ;
    indicationNombreRangee.setText("n = "+Long.toString(nouveauNrangee)) ;
    return true ;
  }
  // si un bouton est active...
  if (e.target instanceof Button) {
    if (e.id==Event.ACTION_EVENT) {
      // recupere le label du bouton active
      Button bt = (Button)e.target ;
      // gere le bt start
      if ("Start".equals(bt.getLabel())) {
	// recupere le nombre de rangee de la barre de defilement : un nombre paire
	NbRangee = (sb.getValue()/2)*2 ;
	// demarre l'animation
	anim.demarrage(NbRangee) ;
	anim.actif = true ;
	return true ;
      }
      // gere le bt pause
      if ("Pause".equals(bt.getLabel())) {
	// met l'animation inactif
	anim.actif = false ;
      }
      // gere le bt continue
      if ("Continue".equals(bt.getLabel())) {
	// met l'animaiton actif
	anim.actif = true ;
      }
    }
  }
  return false ;
}
}

final class animation extends packsprite.spritecanvas {

  /* declaration des variables */
  // objet directeurs
  private directeurpiquets dp ;
  private directeurballes db ;
  private directeurbarres dbarres ;
  // objet qui trace le graphique
  private tracergraph trace ;
  // nombre de rangee de piquet
  private int NbRangee ;
  // espacement entre les piquets
  private int intervallePiquet = 20 ;
  // image de la courbe gaussienne et des piquets
  private Image imageGaussienne = null ;
  // position horizontale du texte du compteur du nombre de tirage
  private int posHcompteurTirage ;

  /* createur objet animation */
public animation() {
  // redimentionne le canvas
  resize(370, 370) ;
  // temps de rafraichissement
  delai = 33 ;
  // definit une couleur d'arriere plan
  setBackground(Color.white) ;
  start() ;
}
  /* demmare une nouvelle animation */
public void demarrage(int NbRangee) {
  this.NbRangee = NbRangee ;
  // creer une image hors ecran
  imageGaussienne = createImage(size().width, size().height) ;
  // obtient le graphics de l'image creee precedement
  Graphics gg = imageGaussienne.getGraphics() ;
  // dessine le fond
  gg.setColor(getBackground()) ;
  gg.fillRect(0,0,size().width,size().height) ;

  // dessine la gaussienne sur l'image hors ecran
  trace = new tracergraph(size(), NbRangee) ;
  trace.paint(gg) ;

  // definit l'espacement entre les piquets : 
  // la valeur 280 represente une echelle fixe
  intervallePiquet = 280/NbRangee ;

  // cree les directeurs des balles, barre et piquet
  db = new directeurballes(this, intervallePiquet) ;
  dbarres = new directeurbarres(this, NbRangee, intervallePiquet, db.getTableauBalles()) ;
  dp = new directeurpiquets(this, NbRangee, intervallePiquet, db.getTableauBalles()) ;
  // dessine les piquet sur l'image hors ecran
  dp.paint(gg) ;

  // dessine le contour de l'animation avec une bordure noir sur l'image hors ecran
  gg.setColor(Color.black) ;
  gg.drawRect(0,0,size().width-1, size().height-1) ;

  // dessine la phrase suivante :
  String phraseNbTirage = "Number of experiments : " ;
  gg.setFont(new Font("Times", Font.PLAIN, 12)) ;
  gg.drawString(phraseNbTirage, 3, 30) ;
  // calcul la position horizontale du compteur du nombre de tirage
  FontMetrics fm = gg.getFontMetrics() ;
  posHcompteurTirage = fm.stringWidth(phraseNbTirage) ;

}

  /* dessine tous les objets graphique de l'animation */
public void paint(Graphics g) {
  // dessine le fond
  if (imageGaussienne!=null) g.drawImage(imageGaussienne, 0, 0, this) ;
  // mise a jour des piquets
  if (dp!=null) dp.update() ;
  // dessine dans l'ordre les barres puis les balles
  if (dbarres!=null) {
    // inscrit le nb de tirages
    g.setColor(Color.black) ;
    g.drawString(Long.toString((long)dbarres.N), posHcompteurTirage, 30) ;
    // dessine les barres
    dbarres.paint(g) ;
  }
  // dessine les balles
  if (db!=null) db.paint(g) ;
}
}




/* objet directeur des piquets */
final class directeurpiquets {
  
  // vecteur contenant tous les objets piquets
  private Vector vecteurPiquet ;

  /* contructeur des piquets */
public directeurpiquets(Canvas parent, // canvas parent
			int NbRangee, // nombre de rangee
			int intervallePiquet, // espacement entre les piquets
			collisionsprite[] collis) { // tableau contenant les objets balles
  // initialisation des variables
  vecteurPiquet = new Vector() ;
  // creer les piquets
  for (int j=0 ; j<=NbRangee ; j++) {
    for (int i=0 ; i<j ; i++) {
      // ajoute un nouveau piquet au vecteurPiquet avec :
      // - 
      vecteurPiquet.addElement(new piquet((parent.size().width + 
					   j*intervallePiquet -
					   intervallePiquet)/2-
					  (i*intervallePiquet)-2, 
					  (j*intervallePiquet) +
					  (parent.size().height-
					   (NbRangee*intervallePiquet)-20),
					  collis,
					  intervallePiquet)) ;
    }
  }
}
  
  /* dessine tous les piquets */
public void paint(Graphics g) {
  for (int i=0 ; i<vecteurPiquet.size() ; i++) {
    piquet p = (piquet)vecteurPiquet.elementAt(i) ;
    p.paint(g) ;
  }	
}
  /* mise a jour de tous les piquets */
public void update() {
  for (int i=0 ; i<vecteurPiquet.size() ; i++) {
    piquet p = (piquet)vecteurPiquet.elementAt(i) ;
    p.update() ;
  }
}  
  /* revoie le vecteur contenant tous les piquets */
public Vector getVecteurPiquets() {
  return vecteurPiquet ;
}
}


/* objet piquet 
 * super-class : spriteovale.java
 * 
 */
final class piquet extends packsprite.spriteovale {
  static collisionsprite[] collis ;
  static int esp ;

  // Random generator
  static Random rand = new Random();

  
  /* createur piquet */

public piquet(int pH, int pV, // position
	      collisionsprite[] collis, // tableau contenant les balles
	      int esp) { // espacement des piquets
  super((double)pH, (double)pV, 4, 4, new Color(0, 0, 0), true) ;
  this.collis = collis ;
  this.esp = esp ;
}
  /* mise a jour */
public void update() {
  // pour chaque balle...
  for (int i=0 ; i<collis.length ; i++) {
    collisionsprite balleCourante = collis[i] ;
    if (balleCourante != null) {
     // verifie si balle touche piquet
      if (balleCourante.collision(pos.x, pos.y, grand.w, grand.h)) {
	// recentre la balle par rapport au piquet
	balleCourante.setPosition((pos.x+(grand.w/2)-(balleCourante.getGrandeur().w/2)),
				  pos.y - balleCourante.getGrandeur().h) ;
	// gere le rebondissement de la balle
	balleCourante.setVitesse(hazardSens()*1, -1*((double)esp/12)) ;
      }
    }
  }
}
  /* renvoie -1 ou 1 */
private double hazardSens() {
  double nb = rand.nextDouble() ;
  if (nb<0.5) return -1 ;
  else return 1 ;
}
}



// DIRECTEUR DES BALLES
final class directeurballes {
  
  protected Canvas parent ;
  // BALLES
  static balle tableauBalles[] ;
  protected int compteur = 0 ;
  protected int esp ;

  // CREATEUR
public directeurballes(Canvas parent, // canvas parent
		       int esp) { // espacement des barres
  this.parent = parent ;
  this.esp = esp ;
  tableauBalles = new balle[15] ;
  creerBalle() ;
}
  
  /* dessine tous les balles */
public void paint(Graphics g) {
  // cree une nuvelle balle
  compteur++ ;
  if (compteur>15) creerBalle() ;
  // pour chaque balle...
  for (int i=0 ; i<tableauBalles.length ; i++) {
    balle balleCourante = tableauBalles[i] ;
    // dessine la balle courante
    if (balleCourante!=null) balleCourante.paint(g) ;
  }
}

  /* renvoie tableau contenant tous les objets balles */
public balle[] getTableauBalles() {
  return tableauBalles ;
}

  /* cree une nouelle balle */
private void creerBalle() {
  //reinitialise le compteur
  compteur = 0 ;
  // place un objet balle dans une case libre du tableau contenant les balles
  for (int i=0 ; i<tableauBalles.length ; i++) {
    if (tableauBalles[i]==null) {
      tableauBalles[i] = new balle((parent.size().width/2)-(esp/8), 0, esp/4, 0, 3) ;
      break ;
    }
  }
}
}


/* objet balle
 * super-classe : spriteovale.java(packsprite)
 */
final class balle extends packsprite.spriteovale implements collisionsprite {
  
  /* initialisation des variables */
  // objet vitesse, contenant les vecteurs vitesses de la balle
  protected vitesse vit ;

  /* createur d'une balle */
public balle(int pH, int pV, // position initiale balle
	     int diametre, // diametre de la balle
	     int vH, int vV) { // vitesse initiale de la balle
  // appel du createur de la super-class
  super((double)pH, (double)pV, diametre, diametre, new Color(255, 0, 0), true) ;
  // creation de l'objet vitesse contenant les vecteurs vitesses de la balle
  vit = new vitesse(vH, vV) ;
}
 
  /* mise a jour si actif */
public void update() {
  // mise a jour de la position
  updatePosition() ;
}
    
  /**** IMPLEMENTS COLLISION ****/
  /* objet touche */
public void touche() {
  // rien
}
  /* teste une collision avec un objet */
public boolean collision(double x, double y, double w, double h) {
  if (actif && (pos.x+grand.w>=x) && (pos.x<=(x+w)) && 
      (pos.y+grand.h>=y) && (pos.y<=(y+h))) {
    return true ;
  }
  else {
    return false ;
  }
}
  
  /**** IMPLEMENTS BOUGER ****/
  /* mise a jour position de la balle */
public void updatePosition() {
  // au debut vitesse horizontale = 0 donc faible acceleration
  if (vit.vH!=0) setVitesse(vit.vH, vit.vV+0.5) ;
  else setVitesse(vit.vH, vit.vV+0.3) ;
  // nouvelle position de la balle
  setPosition(pos.x+vit.vH, pos.y+vit.vV) ;
}

  /* definit une nouvelle position pour la balle */  
public void setPosition(double x, // nouvelle position horizontale
			double y) { // nouvelle position verticale
  pos.x = x ;
  pos.y = y ;
}
  /* definit une nouvelle vitesse pour la balle */
public void setVitesse(double vH, // nouvelle vitesse horizontale
		       double vV) { // nouvelle vitesse verticale
  vit.vH = vH ;
  vit.vV = vV ;
}

  /**** implement collisionsprite ****/
  // renvoie la postion
public position getPosition() {
  return pos ;
}
/* renvoie la vitesse */
public vitesse getVitesse() {
  return vit ;
}
/* renvoie la taille */
public grandeur getGrandeur() {
  return grand ;
}
/* definit une taille */
public void setGrandeur(double w, // nouvelle taille horizontale
			double h) { // nouvelle taille verticale
  grand.w = w ;
  grand.h = h ;
}
}

/* directeur barres */
final class directeurbarres {
  /* declaration des variables */
  // vecteur contenant tous les objets barres
  protected Vector vecteurBarres ;
  // taille du canvas contenant l'animation
  protected Canvas parent ;
  // nombre de tirage
  protected double N = 0 ;

  
  /* constructeur des barres */
public directeurbarres(Canvas parent, // canvas parent
		       int NbRangee, // nombre de rangee
		       int esp, // taille horizontale des barres
		       collisionsprite[] tabBalles) { // tableau contenant les pointeurs sur toutes les balles
  this.parent = parent ;
  vecteurBarres = new Vector() ;
  int droite = 0 ;
  int gauche = 0 ;
  // creer toutes les barres
  for (int i=0 ; i<=NbRangee ; i++) {
    // determine la position gauche de la barre
    gauche = (int)(((double)parent.size().width/2) + 
		   (((double)esp/2)*((double)NbRangee-1)) - 
		   ((double)i*(double)esp)) ;
    // puis sa position droite
    droite = gauche + esp ;
    // cree une nouvelle barre
    vecteurBarres.addElement(new barre(gauche, 10000, esp, 200, parent,
				       droite, gauche, NbRangee+1, this, tabBalles)) ;
  }
}
  /* dessine toutes les barres */
public void paint(Graphics g) {
  for (int i=0 ; i<vecteurBarres.size() ; i++) {
    barre b = (barre) vecteurBarres.elementAt(i) ;
    b.paint(g) ;
  }
}
}


/* OBJET BARRE
 * super-class : spriterectangle
 */
final class barre extends packsprite.spriterectangle {
  
  // position a droite et a gauche : sert a verifier ou tombe une balle
  int droite, gauche ;
  // valeur de la barre : Ni nb de fois de tirage pour cette barre, K constante representant une echelle pour dessiner la barre, H represente l'intervalle entre deux piquets
  protected double Ni, K, H ;
  // tableau contenant tous les pointeurs sur les balles(objet retourne interface)
  protected collisionsprite[] tabBalles ;
  // canvas parent
  protected Canvas parent ;
  // objet directeurbarre (pour avoir acces au nombre de tirage)
  protected directeurbarres db ;

  // CONSTRUCTEUR BARRE
public barre(double pH, double pV, // position
	     double w, double h, // taille
	     Canvas parent, // canvas parent
	     int d, int g, // position a droite et a gauche
	     double intervalle, // taille de l'intervalle
	     directeurbarres db, // directeur barre
	     collisionsprite[] tabBalles) { // tableau contenant tous les pointeurs sur les balles
  // appel du createur de la super-classe
  super(pH, pV, w, h, new Color(0, 0, 255), false) ;
  // initalisation des variables
  this.parent = parent ;
  this.tabBalles = tabBalles ;
  this.db = db ;
  grand.h = 5 ;
  droite = d ;
  gauche = g ;
  H = intervalle ;
  // constante representant une echelle pour dessiner les barres
  K = parent.size().height/3.5 ;
  Ni = 0 ;
}

  /* mise a jour des barres */
public void update() {
  // pour chaque balles...
  for (int i=0 ; i<tabBalles.length ; i++) {
    collisionsprite balleCourante = tabBalles[i] ;
    if (balleCourante!=null) {
      // verifie si balle passe la hauteur de la fenetre
      if (balleCourante.getPosition().y > parent.size().height) {
	// verifie si une balle est entre l'intervalle de la barre
	if ((balleCourante.getPosition().x>gauche) && 
	    (balleCourante.getPosition().x<droite)) {
	  // incremente la barre
	  Ni++ ;
	  // efface la balle du tableau
	  tabBalles[i] = null ;
	  // nombre de tirage incremente
	  db.N++ ;
	}
      }
    }
  }
  // calcul la nouvelle taille verticale de la barre
  grand.h = (int)((Ni*K*H)/db.N) ;
  
  // puis sa nouvelle position
  pos.y = parent.size().height - grand.h + 1 ;
}

  /* dessine la barre : methode outrepasse */
public void paint(Graphics g) {
  // appel de la methode de la super-class
  super.paint(g) ;
  // dessine le haut de la barre avec une couleur differente
  g.setColor(new Color(100, 205, 100)) ;
  g.drawLine((int)pos.x, (int)pos.y, (int)(pos.x+grand.w), (int)pos.y) ;
}
}

/* objet permettant de trace une courbe */
final class tracergraph extends graphiquecourbe {
  /* declaration des variables */
  // constante K
  protected double K = 0 ;
  private int NbRangee;

  /* objet aui permet de tracer la courbe */
public tracergraph(Dimension tailleFenetre, int N) {
  // appel de la fonction de la super-class
  super(-0.5*N - N/280.0*(tailleFenetre.width-280)/2.0, 
	0.5*N  + N/280.0*(tailleFenetre.width-280)/2.0, 
	0.0, 1.0+1.9/N, tailleFenetre, new Color(235, 235, 235), true) ;
  // definition de la constante
  K = (1/(Math.sqrt(2*Math.PI))) ;
  NbRangee = N;
}
  /* definition de la fonction */
protected double calculer() {
  prochainY = Math.exp(-x*x/(NbRangee/2)) ;
  return prochainY ;
}
}


/* objet graphiquecourbe 
 * super-class : null
 */
abstract class graphiquecourbe {
  // represente le pas pour tracer la courbe
  protected double pasH, pasV ;
  // dessine pleine sous la courbe
  protected boolean pleine ;
  // taille de la fenetre dans laquel tracer la courbe
  protected Dimension tailleFenetre ;
  // couleur de la courbe
  protected Color couleur ;
  // caracteristiques de la courbe a tracer
  protected double minX, maxX, minY, maxY = 0 ; // valeur min et max des axes
  protected double x, y, prochainX, prochainY = 0 ; // variables
  protected double prochainPixelV, pixelV  = 0 ; // 

  /* contructeur de la courbe */
public graphiquecourbe(double minX, double maxX, // valeur minimale et maximale sur X
		       double minY, double maxY, // valeur minimale et maximale sur Y
		       Dimension tailleFenetre, // taille fenetre
		       Color c, // couleur de la courbe
		       boolean pleine) { // pleine sous la courbe ou pas
  this.minX = minX ;
  this.maxX = maxX ;
  this.minY = minY ;
  this.maxY = maxY ;
  this.tailleFenetre = tailleFenetre ;
  this.couleur = c ;
  this.pleine = pleine ;
  // calcul le pas horizontale et verticale : cad la valeur en unite de graphique d'un pixel
  //pasH = (Math.abs(minX) + Math.abs(maxX))/(double)tailleFenetre.width ;
  //pasV = (Math.abs(minY) + Math.abs(maxY))/(double)tailleFenetre.height ;
  pasH = (maxX-minX)/(double)tailleFenetre.width ;

  pasV = (maxY-minY)/(double)tailleFenetre.height ;
}
  /* dessine la courbe */
public void paint(Graphics g) {
  // definit une couleur pour la courbe
  g.setColor(couleur) ;
  // definit x
  x = minX ;
  // calcul la valeur de y
  y = calculer() ;
  // convertie y en pixel : *-1 pour une symetrie, +hauteurFenetre pour une translation
  //pixelV = -1*(y/pasV)+(double)tailleFenetre.height-(minY/pasV) ;
  pixelV = (double)tailleFenetre.height - (y-minY)/pasV;
  // pour chaque pixel horizontale...
  for (int pixelH=0 ; pixelH<tailleFenetre.width ; pixelH++) {
    // incremente x du pas et calcul y
    prochainX = x + pasH ;
    prochainY = calculer() ;
    // convertie en pixel y
    //prochainPixelV = -1*(prochainY/pasV)+(double)tailleFenetre.height-(minY/pasV) ;
    prochainPixelV = (double)tailleFenetre.height - (prochainY-minY)/pasV;
    // dessine une droite representant la courbe entre deux points
    g.drawLine((int)pixelH, (int)pixelV, (int)pixelH+1, (int)prochainPixelV) ;
    // dessine une courbe pleine
    double posYaxe = tailleFenetre.height*Math.abs(maxY)/(Math.abs(minY) + Math.abs(maxY)) ;
    if (pleine) g.drawLine((int)pixelH, (int)pixelV, (int)pixelH, (int)posYaxe) ;
    // assigne les coordonnees du point calcule comme ancien point
    x = prochainX ;
    y = prochainY ;
    pixelV = prochainPixelV ;
  }
}
  /* methode a outrepasse : comporte la definition de la fonction */
abstract protected double calculer() ;
}
