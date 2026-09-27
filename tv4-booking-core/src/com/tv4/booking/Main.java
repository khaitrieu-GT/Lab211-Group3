package com.tv4.booking;

import com.tv4.booking.command.*;
import com.tv4.booking.enums.*;
import com.tv4.booking.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        System.out.println("=================================");
        System.out.println("       TV4 BOOKING SYSTEM");
        System.out.println("=================================");

        // =========================
        // 1. INPUT SEAT
        // =========================

        System.out.println("\n--- SEAT INFORMATION ---");

        System.out.print("Seat ID: ");
        Long seatId = scanner.nextLong();

        System.out.print("Zone ID: ");
        Long zoneId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Seat number: ");
        String seatNumber = scanner.nextLine();

        System.out.print("Seat price: ");
        BigDecimal seatPrice = scanner.nextBigDecimal();

        Seat seat = new Seat(
                seatId,
                zoneId,
                seatNumber,
                seatPrice
        );

        // =========================
        // 2. INPUT ZONE
        // =========================

        System.out.println("\n--- ZONE INFORMATION ---");

        System.out.print("Zone ID: ");
        Long inputZoneId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Zone name: ");
        String zoneName = scanner.nextLine();

        System.out.print("Zone type: ");
        String zoneType = scanner.nextLine();

        System.out.print("Zone capacity: ");
        Integer capacity = scanner.nextInt();

        System.out.print("Created ID: ");
        Long createdId = scanner.nextLong();

        Zone zone = new Zone(
                inputZoneId,
                zoneName,
                zoneType,
                capacity,
                createdId,
                List.of(seat)
        );

        // =========================
        // 3. INPUT STADIUM
        // =========================

        System.out.println("\n--- STADIUM INFORMATION ---");

        System.out.print("Stadium ID: ");
        Long stadiumId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Stadium name: ");
        String stadiumName = scanner.nextLine();

        System.out.print("Stadium address: ");
        String address = scanner.nextLine();

        System.out.print("Stadium capacity: ");
        Integer stadiumCapacity = scanner.nextInt();

        Stadium stadium = new Stadium(
                stadiumId,
                stadiumName,
                address,
                stadiumCapacity,
                List.of(zone)
        );

        // =========================
        // 4. INPUT USER
        // =========================

        System.out.println("\n--- USER INFORMATION ---");

        System.out.print("User ID: ");
        Long userId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("User name: ");
        String userName = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        System.out.print("Avatar: ");
        String avatar = scanner.nextLine();

        User user = new User(
                userId,
                userName,
                email,
                phone,
                avatar
        );

        // =========================
        // 5. INPUT BOOKING
        // =========================

        System.out.println("\n--- BOOKING INFORMATION ---");

        System.out.print("Booking ID: ");
        Long bookingId = scanner.nextLong();

        System.out.print("Match ID: ");
        Long matchId = scanner.nextLong();

        System.out.print("Booking total amount: ");
        BigDecimal totalAmount = scanner.nextBigDecimal();

        Booking booking = new Booking(
                bookingId,
                userId,
                matchId,
                totalAmount
        );

        // Command: Create Booking
        Command createBooking =
                new CreateBookingCommand(booking);

        createBooking.execute();

        // =========================
        // 6. BOOKING SEAT
        // =========================

        System.out.println("\n--- BOOKING SEAT ---");

        System.out.print("Booking Seat ID: ");
        Long bookingSeatId = scanner.nextLong();

        BookingSeat bookingSeat = new BookingSeat(
                bookingSeatId,
                bookingId,
                seatId,
                seatPrice
        );

        // Lock booking seat
        Command lockBookingSeat =
                new LockBookingSeatCommand(
                        bookingSeat
                );

        lockBookingSeat.execute();

        // =========================
        // 7. LOCK SEAT
        // =========================

        Command lockSeat =
                new LockSeatCommand(seat);

        lockSeat.execute();

        System.out.println(
                "Seat " + seatNumber + " locked successfully."
        );

        // =========================
        // 8. SEAT HOLD
        // =========================

        System.out.println("\n--- SEAT HOLD ---");

        System.out.print("Seat Hold ID: ");
        Long holdId = scanner.nextLong();

        System.out.print("Hold duration (minutes): ");
        long holdMinutes = scanner.nextLong();

        SeatHold hold = new SeatHold(
                holdId,
                seatId,
                userId,
                bookingId,
                LocalDateTime.now()
                        .plusMinutes(holdMinutes)
        );

        Command createHold =
                new CreateHoldCommand(hold);

        createHold.execute();

        // =========================
        // 9. PAYMENT
        // =========================

        System.out.println("\n--- PAYMENT ---");

        System.out.print("Payment ID: ");
        Long paymentId = scanner.nextLong();

        System.out.println("Payment method:");
        System.out.println("1. VNPAY");
        System.out.println("2. MOMO");
        System.out.println("3. CASH");

        System.out.print("Choose payment method: ");
        int paymentChoice = scanner.nextInt();

        PaymentMethod paymentMethod;

        switch (paymentChoice) {
            case 1:
                paymentMethod = PaymentMethod.VNPAY;
                break;

            case 2:
                paymentMethod = PaymentMethod.MOMO;
                break;

            case 3:
                paymentMethod = PaymentMethod.BANK_TRANSFER;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid payment method."
                );
        }

        Payment payment = new Payment(
                paymentId,
                bookingId,
                totalAmount,
                paymentMethod
        );

        Command createPayment =
                new CreatePaymentCommand(payment);

        createPayment.execute();

        // =========================
        // 10. TRANSACTION
        // =========================

        System.out.println("\n--- TRANSACTION ---");

        System.out.print("Transaction ID: ");
        Long transactionId = scanner.nextLong();
        scanner.nextLine();

        System.out.print("Gateway: ");
        String gateway = scanner.nextLine();

        System.out.print("Transaction number: ");
        String transactionNo = scanner.nextLine();

        System.out.print("Response code: ");
        String responseCode = scanner.nextLine();

        Transaction transaction = new Transaction(
                transactionId,
                paymentId,
                gateway,
                transactionNo,
                responseCode
        );

        Command processTransaction =
                new ProcessTransactionCommand(
                        transaction
                );

        processTransaction.execute();

        try {

            Command verifyTransaction =
                    new VerifyTransactionCommand(
                            transaction
                    );

            verifyTransaction.execute();

            System.out.println(
                    "Transaction verified successfully."
            );

            // Payment SUCCESS
            Command paymentSuccess =
                    new UpdatePaymentStatusCommand(
                            payment,
                            PaymentStatus.SUCCESS
                    );

            paymentSuccess.execute();

        } catch (IllegalStateException ex) {

            System.out.println(
                    "Transaction failed: "
                            + ex.getMessage()
            );
        }

        // =========================
        // 11. CONFIRM BOOKING
        // =========================

        if (payment.verifyPayment()) {

            Command sellBookingSeat =
                    new MarkBookingSeatAsSoldCommand(
                            bookingSeat
                    );

            Command sellSeat =
                    new MarkSeatAsSoldCommand(
                            seat
                    );

            Command confirmBooking =
                    new ConfirmBookingCommand(
                            booking
                    );

            sellBookingSeat.execute();
            sellSeat.execute();
            confirmBooking.execute();

            System.out.println(
                    "\nBooking confirmed successfully!"
            );
        }

        // =========================
        // 12. TICKET
        // =========================

        Ticket ticket = new Ticket(
                bookingId,
                bookingId,
                userId,
                matchId,
                seatNumber
        );

        Command generateTicketQr =
                new GenerateTicketQrCommand(ticket);

        generateTicketQr.execute();

        // =========================
        // 13. DISPLAY RESULT
        // =========================

        System.out.println("\n=================================");
        System.out.println("         BOOKING RESULT");
        System.out.println("=================================");

        System.out.println(
                "User: " + user.getProfile()
        );

        System.out.println(
                "Booking total: "
                        + booking.getTotalAmount()
        );

        System.out.println(
                "Seat: " + seatNumber
        );

        System.out.println(
                "Seat available: "
                        + seat.isAvailable()
        );

        System.out.println(
                "Payment verified: "
                        + payment.verifyPayment()
        );

        System.out.println(
                "Transaction status: "
                        + transaction.getsStatus()
        );

        System.out.println(
                "Ticket QR: "
                        + ticket.generateQr()
        );

        System.out.println(
                ticket.viewTicket()
        );

        System.out.println(
                "Available seats in "
                        + zoneName
                        + ": "
                        + zone.getAvailableSeats().size()
        );

        System.out.println(
                "All seats in stadium: "
                        + stadium.getAllSeats().size()
        );

        // =========================
        // 14. DOUBLE BOOKING TEST
        // =========================

        System.out.println("\n--- DOUBLE BOOKING TEST ---");

        try {

            System.out.print("Second Booking ID: ");
            Long secondBookingId = scanner.nextLong();

            System.out.print("Second User ID: ");
            Long secondUserId = scanner.nextLong();

            Booking secondBooking = new Booking(
                    secondBookingId,
                    secondUserId,
                    matchId,
                    seatPrice
            );

            Command createSecondBooking =
                    new CreateBookingCommand(
                            secondBooking
                    );

            createSecondBooking.execute();

            Command lockAgain =
                    new LockSeatCommand(seat);

            lockAgain.execute();

            System.out.println(
                    "ERROR: Double booking was not blocked!"
            );

        } catch (IllegalStateException ex) {

            System.out.println(
                    "Double booking blocked: "
                            + ex.getMessage()
            );
        }

        scanner.close();
    }
}