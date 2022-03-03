/* MD
** 1/6/97
** OBJET SPRITE APPLET PERMETTANT DE CREER UNE ANIMATION
*/
package packsprite ;

import java.applet.* ;
import java.awt.* ;

abstract public class spritecanvas extends Canvas implements Runnable {
  
  // TEMPS DE RAFRAICHISSEMENT
  protected int delai = 30 ;
  protected Thread threadSprite = null ;
  public Image imageHorsEcran ;
  
  // DEMMARRAGE APPLET
public void start() {
  if (threadSprite == null) {
    threadSprite = new Thread(this) ;
    threadSprite.start() ;
  }
}
  
  // ARRET APPLET
public void stop() {
  if (threadSprite != null) {
    threadSprite.stop() ;
    threadSprite = null ;
  }
}
abstract public void paint(Graphics g) ;

  // GERE LE DOUBLE BUFFERING
public void update(Graphics g) {
  if(imageHorsEcran==null) imageHorsEcran = createImage(size().width, size().height) ;
  Graphics graphicsHorsEcran = imageHorsEcran.getGraphics() ;
  graphicsHorsEcran.setColor(getBackground()) ;
  graphicsHorsEcran.fillRect(0, 0, size().width, size().height) ;
  paint(graphicsHorsEcran) ;
  g.drawImage(imageHorsEcran, 0, 0, this) ;
}

  // EXECUTION APPLET
public void run() {
  while(threadSprite != null) {
    try {
      threadSprite.sleep(delai) ;
    }
    catch (Exception e) {
      System.out.println(e) ;
    }
    repaint() ;
  }
}
}
