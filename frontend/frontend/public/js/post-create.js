document.addEventListener('DOMContentLoaded', () => {
  const titleInput = document.getElementById('title');
  const contentInput = document.getElementById('content');
  const imageInput = document.getElementById('imageUpload');
  const imagePreview = document.getElementById('imagePreview');
  const titleError = document.getElementById('titleError');
  const contentError = document.getElementById('contentError');
  const submitBtn = document.getElementById('submitBtn');
  const toast = document.getElementById('toastMsg');

  // 제목 26자 제한
  titleInput.addEventListener('input', () => {
    if (titleInput.value.length > 26) {
      titleInput.value = titleInput.value.slice(0, 26);
    }
    validateForm();
  });

  // 본문 입력 시
  contentInput.addEventListener('input', validateForm);

  function validateForm() {
    const titleFilled = titleInput.value.trim().length > 0;
    const contentFilled = contentInput.value.trim().length > 0;

    if (titleFilled && contentFilled) {
      submitBtn.disabled = false;
      submitBtn.classList.add('active');
    } else {
      submitBtn.disabled = true;
      submitBtn.classList.remove('active');
    }
  }

  // 이미지 업로드
  imagePreview.addEventListener('click', () => {
    imageInput.click();
  });

  imageInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 5 * 1024 * 1024) {
      alert('이미지 크기는 5MB 이하로 제한됩니다.');
      imageInput.value = '';
      return;
    }

    const reader = new FileReader();
    reader.onload = (event) => {
      imagePreview.innerHTML = `<img src="${event.target.result}" alt="미리보기" />`;
    };
    reader.readAsDataURL(file);
  });

  // 폼 제출
  const form = document.getElementById('postForm');
  form.addEventListener('submit', (e) => {
    e.preventDefault();

    const title = titleInput.value.trim();
    const content = contentInput.value.trim();

    // 제목과 본문 모두 작성 확인
    if (!title || !content) {
      titleError.textContent = '* 제목, 내용을 모두 작성해주세요.';
      titleError.style.display = 'block';
      return;
    } else {
      titleError.style.display = 'none';
    }

    // 등록 성공 시
    toast.textContent = '등록 완료';
    toast.style.display = 'block';
    setTimeout(() => toast.style.display = 'none', 2000);

    // TODO: fetch('/api/v1/posts', { ... }) 연결 가능
    console.log('게시글 등록:', { title, content });
  });
});
