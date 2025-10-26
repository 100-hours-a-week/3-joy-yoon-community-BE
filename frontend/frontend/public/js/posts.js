async function loadPosts() {
  const container = document.getElementById('postsContainer');
  container.innerHTML = '<p>게시글을 불러오는 중...</p>';

  try {
    const resp = await fetch('/api/v1/posts');
    if (!resp.ok) throw new Error('게시글 불러오기 실패');
    const posts = await resp.json();

    if (posts.length === 0) {
      container.innerHTML = '<p>게시글이 없습니다.</p>';
      return;
    }

    container.innerHTML = posts.map(p => `
      <article class="post">
        <h2>${p.title}</h2>
        <p>${p.contents.substring(0, 80)}...</p>
        <div class="meta">
          <span>작성자: ${p.authorNickname || '익명'}</span>
          <span>${new Date(p.createdAt).toLocaleString('ko-KR')}</span>
        </div>
      </article>
    `).join('');
  } catch (err) {
    container.innerHTML = '<p style="color:red;">게시글을 불러오는 중 오류가 발생했습니다.</p>';
    console.error(err);
  }
}

loadPosts();
