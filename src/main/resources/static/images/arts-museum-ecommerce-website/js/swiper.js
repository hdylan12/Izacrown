var swiper = new Swiper('.artsswiper', {  // Fixed class name (Swiper, not Swipe)
    grabCursor: true,
    effect: 'creative',
    creativeEffect: {
        prev: {
            shadow: false,
            translate: [0, 0, -400],
        },
        next: {
            translate: ['100%', 0, 0],
        },
    },
    // Recommended additional parameters
    loop: true,
    speed: 800,
    autoplay: {
        delay: 3000,
        disableOnInteraction: false,
    },
});