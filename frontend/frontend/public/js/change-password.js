document.addEventListener('DOMContentLoaded', () => {
  const pw = document.getElementById('password');
  const pwConfirm = document.getElementById('passwordConfirm');
  const pwError = document.getElementById('passwordError');
  const pwConfirmError = document.getElementById('passwordConfirmError');
  const btn = document.getElementById('updatePwBtn');
  const toast = document.getElementById('toastMsg');

  const pwRule = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^\w\s]).{8,20}$/;

  function validate() {
    const pwVal = pw.value.trim();
    const confirmVal = pwConfirm.value.trim();
    let valid = true;

    if (!pwVal) {
      pwError.textContent = '* 비밀번호를 입력해주세요';
      pwError.style.display = 'block';
      valid = false;
    } else if (!pwRule.test(pwVal)) {
      pwError.textContent = '* 비밀번호는 8자 이상, 20자 이하이며, 대문자, 소문자, 숫자, 특수문자를 각각 최소 1개 포함해야 합니다.';
      pwError.style.display = 'block';
      valid = false;
    } else pwError.style.display = 'none';

    if (!confirmVal) {
      pwConfirmError.textContent = '* 비밀번호를 한번 더 입력해주세요';
      pwConfirmError.style.display = 'block';
      valid = false;
    } else if (pwVal !== confirmVal) {
      pwConfirmError.textContent = '* 비밀번호가 다릅니다.';
      pwConfirmError.style.display = 'block';
      valid = false;
    } else pwConfirmError.style.display = 'none';

    btn.disabled = !valid;
    btn.classList.toggle('active', valid);
  }

  pw.addEventListener('input', validate);
  pwConfirm.addEventListener('input', validate);

  btn.addEventListener('click', () => {
    toast.textContent = '수정 완료';
    toast.style.display = 'block';
    setTimeout(() => toast.style.display = 'none', 2000);
  });
});
