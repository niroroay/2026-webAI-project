// ================
// 取得HTML元素
// ================

// 漢堡選單按鈕
const menuButton = document.querySelector('.menu-button');

// 手機版選單
const mobileNav = document.querySelector('.mobile-nav');

// 搜尋按鈕
const searchButton = document.querySelector('.mobile-search');

// 搜尋框
const mobileSearchForm = document.querySelector('.mobile-search-form');

// 手機漢堡選單內按鈕（公告訊息、資源地圖...）
const menuTitles = document.querySelectorAll('.menu-title');

// ================
// 漢堡選單
// ================

menuButton.addEventListener('click', function () {
    // 開啟+關閉 mobile-nav
    mobileNav.classList.toggle('active');
    // 關閉 search-form
    mobileSearchForm.classList.remove('active');
    // 控制 body 滾動
    updateBodyScroll();
})


// ================
// 搜尋按鈕
// ================

searchButton.addEventListener('click', function () {
    // 開啟+關閉 search-form
    mobileSearchForm.classList.toggle('active');
    // 關閉 mobile-nav
    mobileNav.classList.remove('active');
    // 控制 body 滾動
    updateBodyScroll();
})

// =========================
// 漢堡選單內按鈕+第二層選單
// =========================

menuTitles.forEach(function (button) {
    button.addEventListener('click', function () {
        const menu = button.parentElement.nextElementSibling;

        // 如果已開啟→關閉
        if (button.classList.contains('open')) {
            button.classList.remove('open');
            menu.classList.remove('open');
        } else {
            // 先關閉其他選單
            menuTitles.forEach(function (item) {
                item.classList.remove('open');
                item.parentElement.nextElementSibling.classList.remove('open');
            });

            // 打開目前選單
            button.classList.add('open');
            menu.classList.add('open');
        }
    });
})

function updateBodyScroll() {
    if (
        mobileNav.classList.contains('active') || mobileSearchForm.classList.contains('active')
    ) {
        document.body.classList.add('menu-open');
    } else {
        document.body.classList.remove('menu-open');
    }
}

// =========================
// 螢幕尺寸改變時，重設手機版選單
// =========================

window.addEventListener('resize', function () {

    // 切換到電腦版時
    if (window.innerWidth > 991) {

        // 關閉手機版選單
        mobileNav.classList.remove('active');

        // 關閉手機版搜尋框
        mobileSearchForm.classList.remove('active');

        // 解除 body 滾動鎖定
        document.body.classList.remove('menu-open');
    }

});

// =========================
// 右下角浮動選單動態
// =========================

const quickMenuTexts = document.querySelectorAll(".quick-menu-text");

quickMenuTexts.forEach(function (item) {
    item.innerHTML = [...item.innerText]
        .map(function (char) {
            return `<span>${char === " " ? "&nbsp;" : char}</span>`;
        })
        .join("");
});