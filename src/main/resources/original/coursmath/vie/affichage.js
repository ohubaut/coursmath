//PLF- http://www.jejavascript.net/
var fois=0
function AfficheImage(petiteImage,grandeImage,texteImage,pos)
{
document.write('<a href="javascript:Affichegrande(\''+grandeImage+'\',\''+texteImage+'\')"><img class=' + pos + ' src="'+petiteImage+'" alt="cliquez ici pour afficher en grand"></a>');
} 
function Affichegrande(cheminImage,texte)
{
newImage = new Image;
newImage.src = cheminImage;
html = '<html><head><title>Image</title><meta http-equiv="Pragma" content="no-cache"></head><body leftmargin=0 marginwidth=0 topmargin=0 marginheigth=0 oncontextmenu="return false">'+
'<a href="#" onClick="window.close()"><img src="'+cheminImage+'" border=0 name=monImage alt="'+texte+'"border="0" onLoad="window.resizeTo(document.monImage.width+20,document.monImage.height+140); window.moveTo(20,20)"> </a><div align="center"><h2>' + texte + '</h2></div></body></html>';
if (fois == 1 ) ouvrirImage.close();
ouvrirImage = window.open('','Timbre','toolbar=0,location=0,menuBar=0,scrollbars=0,resizable=1');
ouvrirImage.document.write(html);
fois=1;
}