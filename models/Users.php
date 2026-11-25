<?php
require_once __DIR__ . '/../config/config.php';

class User
{
    // Lấy tất cả người dùng
    public static function all()
    {
        global $pdo;
        $stmt = $pdo->query("SELECT * FROM users");
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // Tìm người dùng theo ID
    public static function find($id)
    {
        global $pdo;
        $stmt = $pdo->prepare("SELECT * FROM users WHERE user_id = ?");
        $stmt->execute([$id]);
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Tìm người dùng theo Email hoặc Username
    public static function findByLogin($login)
    {
        global $pdo;
        $stmt = $pdo->prepare("SELECT * FROM users WHERE LOWER(user_email) = LOWER(?) OR LOWER(user_name) = LOWER(?) LIMIT 1");
        $stmt->execute([$login, $login]);
        return $stmt->fetch(PDO::FETCH_ASSOC);
    }

    // Xác thực tài khoản (hỗ trợ cả hash bcrypt $2y$/$2b$ và plain text cũ)
    public static function authenticate($login, $password)
    {
        $user = self::findByLogin($login);
        if (!$user) {
            return false;
        }

        $hash = $user['user_password'] ?? '';

        // 1. Kiểm tra qua hàm password_verify chuẩn của PHP
        if (password_verify($password, $hash)) {
            return $user;
        }

        // 2. Trường hợp hash bắt đầu bằng $2b$ (từ Python bcrypt)
        if (str_starts_with($hash, '$2b$')) {
            $y_hash = '$2y$' . substr($hash, 4);
            if (password_verify($password, $y_hash)) {
                return $user;
            }
        }

        // 3. Nếu mật khẩu cũ lưu dạng plain-text
        if ($hash === $password) {
            // Nâng cấp tự động lên bcrypt
            self::updatePassword($user['user_id'], $password);
            return $user;
        }

        return false;
    }

    // Cập nhật mật khẩu
    public static function updatePassword($id, $newPassword)
    {
        global $pdo;
        $hashed = password_hash($newPassword, PASSWORD_DEFAULT);
        $stmt = $pdo->prepare("UPDATE users SET user_password = ? WHERE user_id = ?");
        return $stmt->execute([$hashed, $id]);
    }

    // Tạo mới tài khoản (Đăng ký)
    public static function create($user_name, $user_email, $password, $user_role = 'Customer', $isStaff = false)
    {
        global $pdo;
        $hashed = password_hash($password, PASSWORD_DEFAULT);
        $stmt = $pdo->prepare("INSERT INTO users (user_name, user_email, user_password, user_role, IsStaff) 
        VALUES (?, ?, ?, ?, ?)");
        return $stmt->execute([
            $user_name,
            $user_email,
            $hashed,
            $user_role,
            $isStaff ? 1 : 0
        ]);
    }

    // Cập nhật thông tin tài khoản
    public static function update($id, $user_name, $user_email, $user_role, $isStaff = false)
    {
        global $pdo;
        $stmt = $pdo->prepare("UPDATE users 
        SET user_name = ?, user_email = ?, user_role = ?, IsStaff = ? 
        WHERE user_id = ?");

        return $stmt->execute([
            $user_name,
            $user_email,
            $user_role,
            $isStaff ? 1 : 0,
            $id
        ]);
    }

    // Xoá tài khoản
    public static function delete($id)
    {
        global $pdo;
        $stmt = $pdo->prepare("DELETE FROM users WHERE user_id = ?");
        return $stmt->execute([$id]);
    }

    // Lấy tất cả người dùng theo phân trang
    public static function paginate($limit, $offset)
    {
        global $pdo;
        $stmt = $pdo->prepare("SELECT * FROM users LIMIT :limit OFFSET :offset");
        $stmt->bindValue(':limit', $limit, PDO::PARAM_INT);
        $stmt->bindValue(':offset', $offset, PDO::PARAM_INT);
        $stmt->execute();
        return $stmt->fetchAll(PDO::FETCH_ASSOC);
    }

    // Đếm tổng số người dùng
    public static function countAll()
    {
        global $pdo;
        $stmt = $pdo->query("SELECT COUNT(*) FROM users");
        return $stmt->fetchColumn();
    }
}
