BUYER TICKET SYSTEM - MVC

Technology:
- Java SE / Core Java
- MVC architecture
- No Spring, Hibernate, JPA, Lombok, Maven or external framework

Structure:
src/model       -> Class Diagram entities and domain behavior
src/view        -> Console display/input
src/controller  -> Application flow and coordination
src/Main.java   -> Program entry point and MVC wiring

Use cases implemented:
UC04 Search Match (name/date)
UC05 View Match Information
UC06 View Stadium Map
UC07 Select/Unselect Seat and create order
UC12 View E-Ticket
UC13 Purchase History
UC14 Submit Cancellation Request

Payment is intentionally excluded according to the Buyer scope.

Run in NetBeans:
1. File -> Open Project
2. Select BuyerTicketMVC
3. Run project

Run from command line:
- javac -d build/classes $(find src -name "*.java")
- java -cp build/classes Main
