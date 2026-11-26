package com.cinema.service;

import com.cinema.entity.Bill;
import com.cinema.entity.Movie;
import com.cinema.entity.Room;
import com.cinema.entity.User;

public interface TelegramService {
    void sendMessage(String messageText);
    void sendBookingAlert(Bill bill, Movie movie, Room room, String seats, String date, String time);
    void sendNewUserAlert(User user);
    void broadcastMoviePromotion(Movie movie);
    boolean testConnection();
}
