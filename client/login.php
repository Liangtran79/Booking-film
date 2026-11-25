<?php
if (session_status() === PHP_SESSION_NONE) {
    session_start();
}
require_once __DIR__ . '/../config/config.php';
require_once __DIR__ . '/../models/Users.php';

$login_error = '';
$register_error = '';
$active_tab = 'login';
$next = isset($_GET['next']) ? $_GET['next'] : (isset($_POST['next']) ? $_POST['next'] : '');

// Nếu đã đăng nhập rồi
if (!empty($_SESSION['user_id'])) {
    if (!empty($_SESSION['is_staff'])) {
        header("Location: " . (!empty($next) ? $next : "../admin/index.php"));
        exit;
    } else {
        header("Location: " . (!empty($next) ? $next : "index.php"));
        exit;
    }
}

// Xử lý POST (Đăng nhập / Đăng ký)
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = $_POST['action'] ?? 'login';

    if ($action === 'login') {
        $login_id = trim($_POST['email'] ?? '');
        $password = $_POST['password'] ?? '';

        if (empty($login_id) || empty($password)) {
            $login_error = "Vui lòng nhập đầy đủ tài khoản và mật khẩu!";
        } else {
            $user = User::authenticate($login_id, $password);
            if ($user) {
                $_SESSION['user_id'] = $user['user_id'];
                $_SESSION['user_name'] = $user['user_name'];
                $_SESSION['user_email'] = $user['user_email'];
                $_SESSION['user_role'] = $user['user_role'];
                $_SESSION['is_staff'] = !empty($user['IsStaff']) || (isset($user['user_role']) && strtolower($user['user_role']) === 'admin');

                if (!empty($next)) {
                    header("Location: " . $next);
                    exit;
                }

                if (!empty($_SESSION['is_staff'])) {
                    header("Location: ../admin/index.php");
                    exit;
                } else {
                    header("Location: index.php");
                    exit;
                }
            } else {
                $login_error = "Tài khoản hoặc mật khẩu không chính xác!";
            }
        }
        $active_tab = 'login';
    } elseif ($action === 'register') {
        $name = trim($_POST['name'] ?? '');
        $email = trim($_POST['email'] ?? '');
        $password = $_POST['password'] ?? '';

        if (empty($name) || empty($email) || empty($password)) {
            $register_error = "Vui lòng nhập đầy đủ thông tin!";
            $active_tab = 'register';
        } elseif (User::findByLogin($email) || User::findByLogin($name)) {
            $register_error = "Email hoặc Tên tài khoản này đã được sử dụng!";
            $active_tab = 'register';
        } else {
            $created = User::create($name, $email, $password, 'Customer', 0);
            if ($created) {
                $user = User::findByLogin($email);
                $_SESSION['user_id'] = $user['user_id'];
                $_SESSION['user_name'] = $user['user_name'];
                $_SESSION['user_email'] = $user['user_email'];
                $_SESSION['user_role'] = $user['user_role'];
                $_SESSION['is_staff'] = false;

                header("Location: index.php");
                exit;
            } else {
                $register_error = "Đăng ký thất bại, vui lòng thử lại!";
                $active_tab = 'register';
            }
        }
    }
}
?>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <!-- Logo web -->
    <link rel="icon" href="./img/logo_web.jpg" type="image/x-icon">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/boxicons@latest/css/boxicons.min.css">
    <link rel="stylesheet" href="./css/login.css">
    <title>Đăng nhập - Movies</title>
    <style>
        body {
            background-color: #c9d6ff;
            background: linear-gradient(to right, #e2e2e2, #c9d6ff);
            background-image: url('img/login.jpg');
            background-size: cover;
            background-position: center;
            background-repeat: no-repeat;
            background-attachment: fixed;
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            height: 100vh;
        }
        .error-msg {
            color: #e74c3c;
            font-size: 0.85rem;
            margin: 6px 0;
            display: block;
            text-align: center;
            font-weight: 500;
        }
    </style>
</head>

<body>
    <header>
        <a href="./index.php" class="logo">
            <i class="bx bx-movie"></i> Movies
        </a>
        <div class="bx bx-menu" id="menu-icon"></div>

        <!-- Menu -->
        <ul class="navbar">
            <li><a href="./index.php">Trang chủ</a></li>
            <li><a href="./movie.php">Phim</a></li>
        </ul>
    </header>
    <div class="container <?= ($active_tab === 'register') ? 'active' : '' ?>" id="container">
        <div class="form-container sign-up">
            <form method="POST" action="login.php">
                <input type="hidden" name="action" value="register">
                <h1>Tạo Tài Khoản</h1>
                <div class="social-icons">
                    <a href="#" class="icon"><i class="fa-brands fa-google-plus-g"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-github"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-linkedin-in"></i></a>
                </div>
                <span>sử dụng thông tin của bạn để đăng ký</span>
                <input type="text" name="name" placeholder="Họ và tên" required value="<?= htmlspecialchars($_POST['name'] ?? '') ?>">
                <input type="email" name="email" placeholder="Email" required value="<?= htmlspecialchars($_POST['email'] ?? '') ?>">
                <input type="password" name="password" placeholder="Mật khẩu" required>
                <?php if (!empty($register_error)): ?>
                    <span class="error-msg"><?= htmlspecialchars($register_error) ?></span>
                <?php endif; ?>
                <button type="submit">Đăng ký</button>
            </form>
        </div>
        <div class="form-container sign-in">
            <form method="POST" action="login.php">
                <input type="hidden" name="action" value="login">
                <?php if (!empty($next)): ?>
                    <input type="hidden" name="next" value="<?= htmlspecialchars($next) ?>">
                <?php endif; ?>
                <h1>Đăng nhập</h1>
                <div class="social-icons">
                    <a href="#" class="icon"><i class="fa-brands fa-google-plus-g"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-facebook-f"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-github"></i></a>
                    <a href="#" class="icon"><i class="fa-brands fa-linkedin-in"></i></a>
                </div>
                <span>sử dụng tài khoản của bạn</span>
                <input type="text" name="email" placeholder="Email hoặc Tên đăng nhập" required value="<?= htmlspecialchars($_POST['email'] ?? '') ?>">
                <input type="password" name="password" placeholder="Mật khẩu" required>
                <a href="#">Quên mật khẩu?</a>
                <?php if (!empty($login_error)): ?>
                    <span class="error-msg"><?= htmlspecialchars($login_error) ?></span>
                <?php endif; ?>
                <button type="submit">Đăng nhập</button>
            </form>
        </div>
        <div class="toggle-container">
            <div class="toggle">
                <div class="toggle-panel toggle-left">
                    <h1>Chào mừng trở lại!</h1>
                    <p>Nhập thông tin cá nhân của bạn để sử dụng tất cả các tính năng của trang web</p>
                    <button class="hidden" id="login">Đăng nhập</button>
                </div>
                <div class="toggle-panel toggle-right">
                    <h1>Chào bạn!</h1>
                    <p>Đăng ký thông tin cá nhân của bạn để sử dụng tất cả các tính năng của trang web</p>
                    <button class="hidden" id="register">Đăng ký</button>
                </div>
            </div>
        </div>
    </div>

    <script src="js/login.js"></script>
</body>

</html>
