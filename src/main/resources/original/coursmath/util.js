function permute_statique_anime(objTag) {
  if (objTag.src.slice(-10) == '-anime.gif') {
    objTag.src = objTag.src.slice(0, -10)+'.gif';
  } else {
    objTag.src = objTag.src.slice(0, -4)+'-anime.gif';
  }
}

function affiche(id) {
	document.getElementById(id).style.display = 'inline';
}
function cache(id) {
	document.getElementById(id).style.display = 'none';
}
