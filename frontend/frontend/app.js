const express = require('express');
const path = require('path');

const app = express();
const port = 3000;

// app.set('layout', 'layout');

// 정적 파일(css, js 등) 제공
app.use(express.static(path.join(__dirname, 'public')));

app.get(['/login'], (req, res) => {
  res.sendFile(path.join(__dirname, 'public/index.html'));
});

app.get(['/signup'], (req, res) => {
  res.sendFile(path.join(__dirname, 'public/layout.html'));
});

app.get(['/post-create'], (req, res) => {
  res.sendFile(path.join(__dirname, 'public/post-create.html'));
});

app.get(['/change-password'], (req, res) => {
  res.sendFile(path.join(__dirname, 'public/change-password.html'));
});

app.get(['/profile-modify'], (req, res) => {
  res.sendFile(path.join(__dirname, 'public/profile-modify.html'));
});

// 라우팅
// app.get('/login', (req, res) => res.render('login', { title: '로그인 페이지' }));
// app.get('/posts', (req, res) => res.render('posts', { title: '게시판' }));
// app.get('/signup', (req, res) => res.render('signup', { title: '회원가입 페이지' }));

// // /login 경로 요청 시 login.html 반환
// app.get('/login', (req, res) => { res.sendFile(path.join(__dirname, 'public', 'login.html'));});
// app.get('/signup', (req, res) => { res.sendFile(path.join(__dirname, 'public', 'signup.html'));});

// 서버 실행
app.listen(port, () => {
  console.log(`서버가 실행됩니다: http://localhost:${port}`);
});