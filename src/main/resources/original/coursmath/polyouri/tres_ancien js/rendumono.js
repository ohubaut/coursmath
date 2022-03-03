function showPoly(nompoly, nomDiv, scale)
{
	var drawbox;

	var camera, controls, scene, renderer;

	/*var cross;*/

	init(nompoly, nomDiv, scale);
	animate();
}

function init(nompoly, nomDiv, scale) {
	var width=360; 
	var height=360;
	// renderer

	renderer = Detector.webgl? new THREE.WebGLRenderer(): new THREE.CanvasRenderer();
	//renderer = new THREE.CanvasRenderer();
	renderer.setClearColor( 0xfffff0 ); // Background color
	//renderer.setSize(  window.innerWidth, window.innerHeight  );
	renderer.setSize( width, height );
	drawbox = document.getElementById( nomDiv );
	drawbox.appendChild( renderer.domElement );
	
	// world

	scene = new THREE.Scene();
	
	// camera
	
	camera = new THREE.PerspectiveCamera( 60, width / height, 1, 1000 );
	camera.position.z = 80;
	
	// controls

	controls = new THREE.TrackballControls( camera, drawbox );

	controls.rotateSpeed = 1.0;
	controls.zoomSpeed = 1.2;
	controls.minDistance = 33;
	controls.panSpeed = 0.8;

	controls.noZoom = false;
	controls.noPan = false;

	controls.staticMoving = false;
	controls.dynamicDampingFactor = 0.07;

	controls.keys = [ 65, 83, 68 ];

	controls.addEventListener( 'change', render );

	// obj				
	
	if (nompoly.substring(nompoly.lastIndexOf(".")) == ".obj") {		
		var loader = new THREE.OBJLoader();
		loader.load( nompoly, function ( object )
		{
			for ( var i = 0, l = object.children.length; i < l; i ++ ) {
					object.children[ i ].material.overdraw = true;
			}
			
			object.scale.set( scale, scale, scale );
			object.rotation.set( 1, 2, 0);
			scene.add( object );
		});
	}
	else {
		alert("Unknown file type.");
	}

	// lights
	
	light = new THREE.AmbientLight (0x000033);
	scene.add( light );
	
	light = new THREE.HemisphereLight (0xffffff, 0x0000ee,0.5);
	scene.add( light );
	
	
/*	light = new THREE.DirectionalLight( 0xff0000, 1 );
	light.position.set( 0, 0, 1 ).normalize();
	scene.add( light );

	light = new THREE.DirectionalLight( 0x00ffff, 0.6 );
	light.position.set( 0, 0, -1 ).normalize();
	scene.add( light );
	
	light = new THREE.DirectionalLight( 0xffff00, 1.0 );
	light.position.set( 1, 0, 0 ).normalize();
	scene.add( light );
	
	light = new THREE.DirectionalLight( 0x0000ff, 1 );
	light.position.set( -1, 0, 0 ).normalize();
	scene.add( light );
	
	light = new THREE.DirectionalLight( 0x00ff00, 0.7 );
	light.position.set( 0, 1, 0 ).normalize();
	scene.add( light );

	light = new THREE.DirectionalLight( 0xff00ff, 1 );
	light.position.set( 0, -1, 0 ).normalize();
	scene.add( light );
*/

	// Call to onWindowResize if the window is resized.

//	window.addEventListener( 'resize', onWindowResize, false );

}

/*function onWindowResize() {
	
	// if the window is resized, we keep the same matrix.
	
	camera.aspect = width / height;
	camera.updateProjectionMatrix();

	renderer.setSize( width, height );

	controls.handleResize();

	render();

}*/

function animate() {

	// camera animation

	requestAnimationFrame( animate );
	controls.update(); render();
	
}

function render() {

	// render
	
	renderer.render( scene, camera );
	
}