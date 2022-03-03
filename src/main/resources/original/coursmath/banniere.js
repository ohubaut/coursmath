/* SCRIPT EDITE SUR L'EDITEUR JAVASCRIPT http://www.editeurjavascript.com */
var nbimage= 7;
var goto = "#";
var url;
var nom;
function banniere()
{
numimage= Math.round(Math.random()*(nbimage-1)+1);
if (numimage == 1)
{
url = "citation_charpak.gif";
nom = "Si on n'est pas curieux, on est foutu. Georges Charpak";
}
if (numimage == 2)
{
url = "citation_confucius.gif";
nom = "j'entends, j'oublie; je vois, je retiens; je manipule, je comprends. Confucius";
}
if (numimage == 3)
{
url = "citation_pierre_dac.gif";
nom = "Le plus court chemin d'un point à un autre est la ligne droite, à condition qu'ils soient bien l'un en face de l'autre. Pierre Dac";
}
if (numimage == 4)
{
url = "citation_s-germain.gif";
nom = "L'algèbre n'est qu'une géométrie écrite, la géométrie n'est qu'une algèbre figurée. Sophie Germain";
}
if (numimage == 5)
{
url = "citation_v-neumann.gif";
nom = "Si les gens ne croient pas que les mathématiques sont simples, c'est parce qu'ils ne savent pas à quel point la vie est compliquée. John von Neumann";
}
if (numimage == 6)
{
url = "citation_hadamard.gif";
nom = "Dans tout travail scientifique, la parole est toujours, pour commencer, à l'intuition ; le raisonnement rigoureux qui s'élabore ensuite n'est autre chose que l'intuition contrôlée. Jacques Hadamard";
}
if (numimage == 7)
{
url = "citation_arnold.gif";
nom = "Une propriété remarquable des mathématiques, que l’on ne peut s’empêcher d’admirer, est l’efficacité incompréhensible de ses parties les plus abstraites, et à première vue complètement inutiles, à condition qu’elles soient belles.";
goto = "Arnold.pdf"
}
document.write('<a href="' + goto + '">');
document.write('<IMG SRC="' + url + '" ALT="' + nom + '" width=600  border=0>');
document.write('</' + 'a>');
}