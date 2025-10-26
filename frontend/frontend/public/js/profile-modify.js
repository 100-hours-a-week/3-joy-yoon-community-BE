document.addEventListener('DOMContentLoaded', () => {
  const profileCircle = document.getElementById('profileCircle');
  const dropdownMenu = document.getElementById('dropdownMenu');
  const nicknameInput = document.getElementById('nickname');
  const nicknameError = document.getElementById('nicknameError');
  const updateBtn = document.getElementById('updateBtn');
  const withdrawBtn = document.getElementById('withdrawBtn');
  const modal = document.getElementById('withdrawModal');
  const confirmWithdraw = document.getElementById('confirmWithdraw');
  const cancelWithdraw = document.getElementById('cancelWithdraw');
  const toast = document.getElementById('toastMsg');

  // 프로필 클릭 시 드롭다운
  profileCircle.addEventListener('click', () => {
    dropdownMenu.style.display = dropdownMenu.style.display === 'block' ? 'none' : 'block';
  });

  // 닉네임 유효성 검사
  nicknameInput.addEventListener('input', () => {
    const value = nicknameInput.value.trim();
    let message = '';

    if (!value) message = '* 닉네임을 입력해주세요.';
    else if (/\s/.test(value)) message = '* 띄어쓰기를 없애주세요.';
    else if (value.length > 10) message = '* 닉네임은 최대 10자 까지 작성 가능합니다.';

    if (message) {
      nicknameError.textContent = message;
      nicknameError.style.display = 'block';
      updateBtn.disabled = true;
      updateBtn.classList.remove('active');
    } else {
      nicknameError.style.display = 'none';
      updateBtn.disabled = false;
      updateBtn.classList.add('active');
    }
  });

  updateBtn.addEventListener('click', () => {
    if (!nicknameInput.value.trim()) {
      nicknameError.textContent = '* 닉네임을 입력해주세요.';
      nicknameError.style.display = 'block';
      return;
    }

    toast.textContent = '수정 완료';
    toast.style.display = 'block';
    setTimeout(() => toast.style.display = 'none', 2000);
  });

  // 회원탈퇴 모달
  withdrawBtn.addEventListener('click', () => modal.style.display = 'flex');
  cancelWithdraw.addEventListener('click', () => modal.style.display = 'none');
  confirmWithdraw.addEventListener('click', () => {
    modal.style.display = 'none';
    alert('회원 탈퇴가 완료되었습니다.');
    window.location.href = '/login';
  });
});
