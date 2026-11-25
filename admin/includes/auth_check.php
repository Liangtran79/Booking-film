<?php
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}

// Kiểm tra quyền Admin (phải đăng nhập và có quyền is_staff hoặc role Admin)
$is_admin = !empty($_SESSION['user_id']) && (
    !empty($_SESSION['is_staff']) || 
    (isset($_SESSION['user_role']) && strtolower($_SESSION['user_role']) === 'admin')
);

if (!$is_admin) {
    $redirect = urlencode($_SERVER['REQUEST_URI']);
    header("Location: ../client/login.php?next=" . $redirect);
    exit;
}
