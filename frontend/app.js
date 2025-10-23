const express = require('express');
// const expressLayouts = require('express-ejs-layouts');
const path = require('path');

const app = express();
const port = 3000;

// EJS 설정
// app.set('view engine', 'ejs');
// app.set('views', path.join(__dirname, 'views'));
// app.use(expressLayouts);
app.set('layout', 'layout');

// 정적 파일(css, js 등) 제공
app.use(express.static(path.join(__dirname, 'public')));

// 라우팅
app.get('/login', (req, res) => res.render('login', { title: '로그인 페이지' }));
app.get('/posts', (req, res) => res.render('posts', { title: '게시판' }));
app.get('/signup', (req, res) => res.render('signup', { title: '회원가입 페이지' }));

// /login 경로 요청 시 login.html 반환
app.get('/login2', (req, res) => {
 res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// 서버 실행
app.listen(port, () => {
  console.log(`서버가 실행됩니다: http://localhost:${port}`);
});