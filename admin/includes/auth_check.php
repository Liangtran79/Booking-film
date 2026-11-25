<?php
if (!ob_get_level()) {
    ob_start();
}

if (session_status() === PHP_SESSION_NONE && !headers_sent()) {
    session_start();
}

// Kiểm tra quyền Admin (phải đăng nhập và có quyền is_staff hoặc role Admin)
$is_admin = !empty($_SESSION['user_id']) && (
    !empty($_SESSION['is_staff']) || 
    (isset($_SESSION['user_role']) && strtolower($_SESSION['user_role']) === 'admin')
);

if (!$is_admin) {
    $redirect = urlencode($_SERVER['REQUEST_URI']);
    $login_url = "../client/login.php?next=" . $redirect;
    if (!headers_sent()) {
        header("Location: " . $login_url);
    } else {
        echo "<script>window.location.href = '" . addslashes($login_url) . "';</script>";
        echo "<noscript><meta http-equiv='refresh' content='0;url=" . htmlspecialchars($login_url) . "'></noscript>";
    }
    exit;
}
