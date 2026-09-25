package com.tv4.booking;

import com.tv4.booking.enums.*;
import com.tv4.booking.model.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Seat seatA01 = new Seat(1L, 1L, "A01", new BigDecimal(500000));
        Seat seatA02 = new Seat(2L, 1L, "A02", new BigDecimal(500000));
        Zone vip = new Zone(1L, "VIP", "VIP", 2, 1L, List.of(seatA01, seatA02));
        Stadium stadium = new Stadium(1L, "TV4 Stadium", "Da Nang", 50000, List.of(vip));

        User user = new User(1L, "Nguyen Van A", "a@example.com", "0987654321", "avatar.png");
        Booking booking = new Booking(100L, 1L, 10L, new BigDecimal(500000));
        booking.create();

        BookingSeat bookingSeat = new BookingSeat(1000L, 100L, 1L, new BigDecimal(500000));

        seatA01.lock();
        bookingSeat.lockSeat();

        SeatHold hold = new SeatHold(5000L, 1L, 1L, 100L, LocalDateTime.now().plusMinutes(10));
        hold.crateHold();

        Payment payment = new Payment(2000L, 100L, booking.getTotalAmount(), PaymentMethod.VNPAY);
        payment.createPayment();

        Transaction transaction = new Transaction(3000L, 2000L, "VNPAY", "TXN-001", "00");
        transaction.process();
        transaction.verify();

        payment.updateStatus(PaymentStatus.SUCCESS);

        if (payment.verifyPayment()) {
            bookingSeat.markAsSold();
            seatA01.markAsSold();
            booking.confirm();
        }

        Ticket ticket = new Ticket(4000L, 100L, 1L, 10L, "A01");

        System.out.println("=== TV4 BOOKING / DOUBLE BOOKING / PAYMENT ===");
        System.out.println(user.getProfile());
        System.out.println("Booking total: " + booking.getTotalAmount());
        System.out.println("Seat A01 available after sale: " + seatA01.isAvailable());
        System.out.println("Payment verified: " + payment.verifyPayment());
        System.out.println("Transaction status: " + transaction.getsStatus());
        System.out.println("Ticket QR: " + ticket.generateQr());
        System.out.println(ticket.viewTicket());

        try {
            Booking secondBooking = new Booking(101L, 2L, 10L, new BigDecimal(500000));
            secondBooking.create();
            seatA01.lock();
            System.out.println("ERROR: Double booking was not blocked");
        } catch (IllegalStateException ex) {
            System.out.println("Double booking blocked: " + ex.getMessage());
        }

        System.out.println("Available seats in VIP: " + vip.getAvailableSeats().size());
        System.out.println("All seats in stadium: " + stadium.getAllSeats().size());
    }
}