# LAB211 – Group 3

> **Java OOP Project – Stadium Ticket Booking System**

A Java-based stadium ticket booking system developed for the **LAB211** course.  
This repository is used by Group 3 for source-code management, collaboration, and project submission.

## 👥 Team Members

| Member | Student ID |
|---|---|
| Bùi Khải Triệu | QE210228 |
| Ngô Phạm Nguyệt Minh | QE210021 |
| Hồ Lê Hoàng Nhật | QE200049 |
| Chu Bảo Khánh | HE171093 |
| Đặng Đăng Khoa | QE200021 |

## 🎯 System Overview

The system manages the complete stadium ticket-selling process:

**Admin → Seller → Buyer → Booking & Payment → Database**

### Core Functions

- Account registration, login, logout, and profile management
- Role-based access control
- Match and stadium information
- Stadium zones and seat management
- Ticket creation, pricing, and inventory
- Seat selection and temporary seat holding
- Booking and payment
- E-ticket and booking history
- Notifications and transaction status
- Revenue and system reports

## 🔐 Roles & Permissions

### Admin
Full system management:

- Manage users, buyers, and sellers
- Manage roles and permissions
- Manage stadiums, zones, and seats
- Manage matches and tickets
- Monitor orders, payments, and transactions
- Handle complaints and abnormal cases
- View reports, statistics, and audit logs
- Lock/unlock accounts and seats

### Seller
Responsible for selling and managing tickets:

- Manage personal profile
- Create/manage matches when authorized
- Create and manage tickets
- Set ticket prices and quantities
- Manage available selling areas/seats
- Monitor orders and sales revenue
- View sales history
- Stop ticket sales when permitted

> Seller cannot modify system-wide administration, user roles, other sellers' transactions, or payment status without authorization.

### Buyer
Responsible for searching and purchasing tickets:

- Register and manage account
- Search and view matches
- View stadium, zones, seats, and prices
- Select and temporarily hold seats
- Create bookings and make payments
- Receive and view e-tickets / QR codes
- View purchase history
- Cancel eligible bookings
- Receive transaction notifications

## 🛡️ Double Booking Prevention

**Double Booking** occurs when multiple buyers attempt to purchase the same seat at nearly the same time.

The system must ensure that one seat cannot be successfully assigned to multiple valid bookings.

### Seat Status

```text
AVAILABLE → HELD → SOLD
     ↑        │
     └────────┘
      Hold expired
```

- **AVAILABLE** – Seat can be selected.
- **HELD** – Temporarily reserved by a buyer.
- **SOLD** – Successfully purchased.
- **CANCELLED** – Booking/ticket has been cancelled according to business rules.

### Protection Principles

1. Backend is the authority for seat availability.
2. Database/transaction control prevents concurrent conflicting bookings.
3. A valid booking must not allow duplicate seat ownership.
4. Seat holding must have an expiration time.
5. Expired holds automatically release the seat.
6. Successful payment changes `HELD → SOLD`.
7. Failed/expired transactions release the seat when appropriate.

### Booking Flow

```text
Buyer
  ↓
Select Seat
  ↓
Backend Validation
  ↓
Seat Available?
 ┌───────┴───────┐
 YES             NO
 ↓                ↓
HOLD             Reject
 ↓
Payment
 ↓
Success?
 ┌───────┴───────┐
 YES             NO
 ↓                ↓
SOLD           RELEASE
```

## 🏗️ Project Structure

Pure Java (no framework), MVC + OOP, CSV file storage. Source compiles with Java 8+.

```text
Lab211-Group3/
├── src/
│   ├── Main.java          # Entry point: seeds data, then MainController.start()
│   ├── model/             # Entities (BaseEntity → User/Admin/Seller/Buyer, Stadium, Section, Seat,
│   │   └── enums/         #   Team, Match, Ticket, SeatHold, Booking, BookingSeat, Payment,
│   │                      #   Transaction, ETicket, CancellationRequest, Notification, AuditLog)
│   ├── view/              # Console input/output only (no controller references, no business logic)
│   ├── controller/        # Flow: MainController, AdminController, SellerController, BuyerController,
│   │                      #   ProfileController (own the menu loops). Business: MatchController,
│   │                      #   SeatController, BookingController, PaymentController, TicketController, ...
│   ├── command/           # Command pattern (Lock/Unlock seat, Create/Confirm booking, Payment, ...)
│   ├── repository/        # CsvRepository<T> (CRUD, atomic file write) + one repository per entity
│   ├── service/           # Account, Notification, AuditLog, PaymentGateway (simulated), Report, DataSeeder
│   ├── dto/               # SeatMap, SalesReport
│   ├── exception/         # BusinessException
│   └── util/              # AppConfig, CsvUtil, PasswordUtil, MoneyUtil, ValidationUtil
├── data/                  # CSV database (delete the folder to reset sample data)
├── docs/                  # Member documents, diagrams, MERGE_MAPPING.md
├── build.xml, nbproject/  # Ant / NetBeans project
├── run.bat, run.sh        # Compile & run scripts
└── README.md
```

### MVC flow (the program runs from the controller)

```text
Main ──► MainController.start()
            │  login / register (MainView)
            ├──► AdminController.start()  ──► AdminView, MatchView, OrderView, TicketView, ReportView
            ├──► SellerController.start() ──► SellerView, MatchView, TicketView, OrderView, ReportView
            └──► BuyerController.start()  ──► BuyerView, MatchView, SeatView, OrderView, TicketView
                     │ (all three use ProfileController ──► ProfileView)
                     ▼
        business controllers / services ──► repository (CSV) ──► model
```

A view never calls a controller: the controller asks the view for input, runs the business
logic (models + repositories), then hands the result back to the view to display.

### CRUD per entity

| Entity | Create | Read | Update | Delete |
|---|---|---|---|---|
| User (Admin) | Register buyer / create seller | List, search | Profile, lock/unlock, admin edit | Soft delete (`DELETED`, history kept) |
| Stadium (Admin) | Add | List, layout | Name/address/city/capacity | Only if no match uses it (removes its sections & seats) |
| Section (Admin) | Add | By stadium | Name/capacity, open/close | Only if no ticket uses it |
| Seat (Admin) | Generate row | By section | Maintenance, lock/unlock | Only if no ticket uses it |
| Team (Admin) | Add (unique name) | List | Name/logo/country | Only if no match uses it |
| Match (Seller) | Add (no stadium/team clash) | Own matches | Name/date/time, open/close sale | Only if no booking yet |
| Ticket (Seller) | One seat / whole section | By match | Price/type, stop/resume sale | Only if never booked |
| Booking (Buyer) | Hold seats | History, pending | Extend hold, remove seat, pay | Cancel pending booking |
| Cancellation request | Buyer submits | Buyer/Admin | Admin approve/reject | – |

## ▶️ How to Run

Always run from the project root (the program reads/writes `data/`).

```bash
# Windows
run.bat
# Linux / macOS / Git Bash
./run.sh
# Or: NetBeans → File → Open Project → Lab211-Group3 → Run,  or  ant run
```

Sample accounts (password `123456`):

| Username | Role | Note |
|---|---|---|
| `admin` | Admin | Full management |
| `seller1` | Seller | Owns matches M001, M002 |
| `seller2` | Seller | Owns match M003 (not on sale yet) |
| `buyer1`, `khoa` | Buyer | Ready to buy tickets |

### End-to-end flow

```text
Admin   : stadium → sections → seats, teams, seller accounts
Seller  : create match → generate tickets per section → open sale
Buyer   : search match → seat map → select seats (HOLD 5 min, max 4)
          → pay (gateway simulated) → SOLD + E-Ticket (QR)
          → purchase history → cancellation request
Admin   : approve/reject cancellation → refund, ticket back on sale
Seller  : counter sale, QR check-in, sales report
```

Double booking is blocked by optimistic locking on `Ticket.version`
(`TicketRepository.updateIfVersionMatches`); Admin menu **9** runs a two-buyer concurrency test.
Merge notes: [docs/MERGE_MAPPING.md](docs/MERGE_MAPPING.md) · Demo script covering every flow: [docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md).

## 🔧 Development

**Technologies:** Java, JDK, Git, GitHub

Recommended workflow:

```bash
git checkout -b feature/<feature-name>
git add .
git commit -m "feat: add <feature>"
git push origin feature/<feature-name>
```

Create a Pull Request before merging changes into `main`.

### Commit Convention

```text
feat:     New functionality
fix:      Bug fix
refactor: Code restructuring
docs:     Documentation
test:     Testing
chore:    Maintenance
```

## 📚 Documentation

Project requirements and working documents:

[Group Documentation](https://docs.google.com/document/d/1CPT4zTlAQEyt7xDQGggoXAK-JpEAZc7t/edit)

---

<p align="center">
  <strong>LAB211 · Group 3</strong><br>
  Stadium Ticket Booking System
</p>
