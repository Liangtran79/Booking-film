FROM php:8.2-apache

# Cài đặt extension pdo_mysql cho PHP
RUN docker-php-ext-install pdo pdo_mysql

# Bật Apache mod_rewrite
RUN a2enmod rewrite

# Cấu hình PHP (tăng giới hạn upload file, bộ nhớ)
RUN { \
    echo 'upload_max_filesize = 64M'; \
    echo 'post_max_size = 64M'; \
    echo 'memory_limit = 256M'; \
    echo 'max_execution_time = 300'; \
} > /usr/local/etc/php/conf.d/custom.ini

# Thư mục làm việc
WORKDIR /var/www/html

# Copy toàn bộ mã nguồn
COPY . /var/www/html

# Phân quyền cho www-data để upload ảnh vào admin/img/uploads
RUN chown -R www-data:www-data /var/www/html \
    && chmod -R 775 /var/www/html/admin/img/uploads

EXPOSE 80
