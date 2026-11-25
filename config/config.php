<?php
if (!ob_get_level()) {
    ob_start();
}
if (session_status() === PHP_SESSION_NONE && !headers_sent()) {
    session_start();
}

$host = getenv('DB_HOST') ?: 'localhost';           
$dbname = getenv('DB_NAME') ?: 'cinema';  
$username = getenv('DB_USER') ?: 'root';            
$password = getenv('DB_PASS') !== false ? getenv('DB_PASS') : '';                
$port = getenv('DB_PORT') ?: '3306';

// Khởi tạo kết nối PDO
try {
    $pdo = new PDO("mysql:host=$host;port=$port;dbname=$dbname;charset=utf8mb4", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    // echo "Kết nối thành công!"; 

} catch (PDOException $e) {
    die("Kết nối thất bại: " . $e->getMessage());
}
