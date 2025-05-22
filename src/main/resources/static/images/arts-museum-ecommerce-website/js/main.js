const sectionNavigator = name => {
    let sections = document.querySelectorAll('section');
    sections.forEach(section => {
        section.classList.remove('active');
        if (section.id === name) {
            section.classList.add('active');
        }
    });
}

const toggleMenu = () => {
    // You'll need to implement this function based on your menu toggle needs
    console.log("Toggle menu function needs implementation");
}

window.addEventListener('load', () => {
    const navList = document.querySelectorAll('.nav-btn');
    navList.forEach(nav => {
        nav.addEventListener('click', function(e) {
            e.preventDefault();
            navList.forEach(el => {
                el.classList.remove('active');
            });
            this.classList.add('active');
            const target = this.getAttribute('data-target');
            if (target) {
                sectionNavigator(target);
            }
            if (screen.width < 768) {
                toggleMenu();
            }
        });
    });
});