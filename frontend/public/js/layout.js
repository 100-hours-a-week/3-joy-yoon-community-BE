class HeaderComponent extends HTMLElement {
  connectedCallback() {
    this.innerHTML = `
        <!doctype html>
        <html lang="ko">
        <head>
            <meta charset="utf-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1" />
            <title>커뮤니티 레이아웃</title>
            <link rel="stylesheet" href="/css/style.css" />
        </head>
        <body>
        <header>
            <h1>아무 말 대잔치</h1>
        </header>

        <div class="container">
            <body></body>
        </div>

        <footer>
            © 2025 My Community —
            <a href="http://localhost:8080/service">이용약관</a> ·
            <a href="http://localhost:8080/privacy">개인정보처리방침</a>
        </footer>
        </body>
        </html>
    `;
  }
}
customElements.define('header-component', HeaderComponent);
