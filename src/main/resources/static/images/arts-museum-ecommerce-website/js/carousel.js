var radius = screen.width < 768 ? 145 : 390;
var autoRotate = true;
var rotateSpeed = -60;
var imgWidth = screen.width < 768 ? 120 : 324;
var imgHeight = screen.width < 768 ? 170 : 459;
var currentZoom = 1; // Track zoom level
var maxZoom = 2; // Maximum zoom level
var minZoom = 0.5; // Minimum zoom level

setTimeout(init, 1000);

var odrag = document.querySelector('#drag-container');
var ospin = document.querySelector('#spin-container');
var aImg = ospin.getElementsByTagName('img');
var aEle = [...aImg];

ospin.style.width = imgWidth + 'px';
ospin.style.height = imgHeight + 'px';

var ground = document.querySelector('#ground');
ground.style.width = radius * 3 + 'px';
ground.style.height = radius * 3 + 'px';

function init(delayTime) {
    for (var i = 0; i < aEle.length; i++) {
        const originalTransform = `rotateY(${i * (360 / aEle.length)}deg) translateZ(${radius}px)`;
        aEle[i].style.transform = originalTransform;
        aEle[i].dataset.originalTransform = originalTransform;
        aEle[i].style.transition = 'transform 0.4s cubic-bezier(0.25, 0.1, 0.25, 1)';
        aEle[i].style.transitionDelay = delayTime !== undefined ? delayTime : `${i * 0.1}s`;
        
        // Lift effect
        aEle[i].addEventListener('pointerdown', function(e) {
            this.style.transform = this.dataset.originalTransform + ' translateZ(50px)';
        });
        
        aEle[i].addEventListener('pointerup', function(e) {
            this.style.transform = this.dataset.originalTransform;
        });
        
        aEle[i].addEventListener('touchstart', function(e) {
            this.style.transform = this.dataset.originalTransform + ' translateZ(50px)';
        });
        
        aEle[i].addEventListener('touchend', function(e) {
            this.style.transform = this.dataset.originalTransform;
        });
    }
}

// Mouse wheel zoom effect
odrag.addEventListener('wheel', function(e) {
    e.preventDefault();
    
    // Calculate new zoom level (inverse because wheel down = zoom out)
    currentZoom += e.deltaY * -0.001;
    
    // Limit zoom level
    currentZoom = Math.min(Math.max(currentZoom, minZoom), maxZoom);
    
    // Apply zoom transform
    ospin.style.transform = `scale(${currentZoom})`;
}, { passive: false });

if (autoRotate) {
    var animationName = rotateSpeed > 0 ? 'spin' : 'spinRevert';
    ospin.style.animation = `${animationName} ${Math.abs(rotateSpeed)}s infinite linear`;
}

// Drag rotation variables
var sX, sY, nX, nY, desX = 0, desY = 0, tX = 0, tY = 10;

function applyTransform(obj) {
    if (tY > 180) tY = 180;
    if (tY < 0) tY = 0;
    obj.style.transform = `rotateX(${-tY}deg) rotateY(${tX}deg) scale(${currentZoom})`;
}

function playSpin(yes = true) {
    ospin.style.animationPlayState = yes ? 'running' : 'paused';
}

// Drag events
odrag.addEventListener('mousedown', dragStart, false);
odrag.addEventListener('touchstart', dragStart, false);
document.addEventListener('mousemove', dragMove, false);
document.addEventListener('touchmove', dragMove, false);
document.addEventListener('mouseup', dragEnd, false);
document.addEventListener('touchend', dragEnd, false);

function dragStart(e) {
    e.preventDefault();
    playSpin(false);
    sX = e.clientX || e.touches[0].clientX;
    sY = e.clientY || e.touches[0].clientY;
}

function dragMove(e) {
    if (!sX || !sY) return;
    e.preventDefault();
    nX = (e.clientX || e.touches[0].clientX) - sX;
    nY = (e.clientY || e.touches[0].clientY) - sY;
    tX = desX + nX * 0.1;
    tY = desY + nY * 0.1;
    applyTransform(ospin);
}

function dragEnd() {
    desX = tX;
    desY = tY;
    sX = sY = null;
    if (autoRotate) playSpin(true);
}