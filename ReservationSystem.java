import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationSystem {

    String pathHotel;
    String pathReservation;
    String pathBilling;
    private List<Reservation> resList = new ArrayList<>();

    /**ReservationSystem constructor
     * 
     * @param pathHotel path for hotel file with rooms and rates
     * @param pathReservation path for reservations to be stored
     * @param pathBilling path to store billings
     * @author SeanFitzgerald*/
    public ReservationSystem(String pathHotel, String pathReservation, String pathBilling) {
        this.pathHotel = pathHotel;
        this.pathReservation = pathReservation;
        this.pathBilling = pathBilling;
    }

    /**Get list of reservations
     * 
     * @return list<reservation> list of reservations
     * @author SeanFitzgerald*/
    public List<Reservation> getResList() {
        return resList;
    }

    /**Check if a room is available
     * 
     * @param roomType the chosen room type
     * @param checkIn check in date
     * @param checkOut check out date
     * @return boolean true if available, false if not
     * @author SeanFitzgerald*/
    public boolean available(String roomType, LocalDate checkIn, LocalDate checkOut) {
        int taken = 0;
        for (Reservation res : resList) {
            LocalDate checkInRes = res.getCheckIn();
            LocalDate checkOutRes = res.getCheckOut();
            if ((checkIn.isBefore(checkInRes) && checkOut.isBefore(checkInRes)) ||
                    (checkIn.isBefore(checkInRes) && checkOut.isEqual(checkInRes)) ||
                    (checkIn.isAfter(checkOutRes) && checkOut.isAfter(checkOutRes)) ||
                    (checkIn.isEqual(checkOutRes) && checkOut.isAfter(checkOutRes))) {
                break;
            }

            RoomList rl = res.getRoomList();
            List<Room> rooms = rl.getRooms();
            for (Room r : rooms) {
                if (r.getRoomType().equals(roomType)) {
                    taken++;
                }
            }
        }
        if (taken >= HotelList.getNumberOfRooms(roomType)) {
            return false;
        }
        return true;
    }

    /**Calculates total cost of booking
     * 
     * @param roomType selected room type
     * @param checkIn checkin date
     * @param checkOut checkout date
     * @param resType type of reservation
     * @return double total cost
     * @author SeanFitzgerald*/
    public double calcTotalCost(String roomType, LocalDate checkIn, LocalDate checkOut, String resType) {
        LocalDate dateCopy = checkIn;
        int[] rates = HotelList.getRates(roomType);
        double total = 0;
        while (!dateCopy.equals(checkOut)) {
            int index = dateCopy.getDayOfWeek().getValue() - 1;
            total += rates[index];
            dateCopy = dateCopy.plusDays(1);
        }
        if (resType.equals("AP")) {
            total *= 0.95;
        }
        return total;
    }

    /**Constructor which uses a room list for multiple rooms
     * 
     * @param roomList the chosen rooms
     * @param checkIn checkin date
     * @param checkOut checkout date
     * @param resType type of reservation
     * @return double total cost
     * @author SeanFitzgerald*/
    public double calcTotalCost(RoomList roomList, LocalDate checkIn, LocalDate checkOut, String resType) {
        double totalCost = 0;
        List<Room> rooms = roomList.getRooms();
        for (Room r : rooms) {
            totalCost += calcTotalCost(r.getRoomType(), checkIn, checkOut, resType);
        }
        return totalCost;
    }

    /**Makes a new reservation
     * 
     * @param refNo reference number
     * @param name name of person booking room
     * @param resType type of reservation
     * @param checkIn check in date
     * @param checkOut check out date
     * @param numOfRoom number of rooms
     * @param roomList list of rooms
     * @param totalCost total cost
     * @return Reservation the reservation using those details
     * @author SeanFitzgerald*/
    public Reservation makeReservation(int refNo, String name, String resType, LocalDate checkIn, LocalDate checkOut,
                                       int numOfRoom, RoomList roomList, double totalCost) {
        List<Room> rooms = roomList.getRooms();
        for (Room r : rooms) {
            if (!available(r.getRoomType(), checkIn, checkOut)) {
                return null;
            }
        }
        Reservation reservation = new Reservation(refNo, name, resType, checkIn, checkOut, numOfRoom,
                roomList, totalCost);
        resList.add(reservation);
        updateReservationCSV();
        return reservation;
    }

    /**Check if the refNo is valid
     * 
     * @param refNo reference number
     * @return boolean true if a valid number (the reservation exists)
     * @author SeanFitzgerald*/
    public boolean isValidReservationNumber(int refNo) {
        return resList.stream().anyMatch(r -> r.getRefNo() == refNo);
    }

    /**Get a reservation object based on refNo
     * 
     * @param refNo reference number
     * @return Reservation reservation with that refNo
     * @author SeanFitzgerald*/
    public Reservation findReservation(int refNo) {
        return resList
                .stream()
                .filter(p -> p.getRefNo() == refNo)
                .findFirst()
                .get();
    }

    /**Cancel a reservation
     * 
     * @param reservation the reservation to be cancelled
     * 
     * @author SeanFitzgerald*/
    public void cancelReservation(Reservation reservation) {
        if (reservation.getResType().equals("AP")) {
            System.out.println("Your reservation is an advance an advance and therefore can not be refunded");
            resList.remove(reservation);
            System.out.println("Your reservation is now canceled");
            updateReservationCSV();
            return;
        }

        LocalDate cancelLimit = reservation.getCheckIn().minusDays(2);
        if (LocalDate.now().isAfter(cancelLimit)) {
            System.out.println("Your standard reservation can not be refunded because you canceled it 48 " +
                    "hours after the check-in date");
            resList.remove(reservation);
            System.out.println("Your reservation is now canceled");
            updateReservationCSV();
            return;
        }
        System.out.println("" + reservation.getTotalCost() + " will be refunded to your account");
        resList.remove(reservation);
        System.out.println("Your reservation is now canceled");
        updateReservationCSV();
    }

    /**Shows all reservations
     * 
     * @author SeanFitzgerald*/
    public void showAllReservation() {
        for (Reservation res : resList) {
            System.out.println(res.toString());
        }
    }

    /**Check if csv files are writable to
     * 
     * @author SeanFitzgerald*/
    public void initCSV() {
        CSVEncoder csvEncoder1 = new CSVEncoder(pathReservation);
        try {
            csvEncoder1.csvInit();
        } catch (IOException e) {
            System.err.println("Can not write file reservation.csv");
        }
        CSVEncoder csvEncoder2 = new CSVEncoder(pathBilling);
        try {
            csvEncoder2.csvInit();
        } catch (IOException e) {
            System.err.println("Can not write file billing.csv");
        }
    }

    /**Decode the hotel csv file and return a HotelList
     * 
     * @return HotelList list of the 3 difference hotels with their rooms
     * @author SeanFitzgerald*/
    public HotelList decodeHotelCSV() {
        CSVEncoder csvEncoder = new CSVEncoder(pathHotel);
        try {
            String[][] list = csvEncoder.csvRead();
            Hotel hotel3Star = new Hotel();
            Hotel hotel4Star = new Hotel();
            Hotel hotel5Star = new Hotel();

            for (int i = 2; i < list[0].length; i++) {
                int[] rates = new int[7];
                for (int j = 0; j < 7; j++) {
                    rates[j] = Integer.parseInt(list[i][5 + j]);
                }
                HotelRoom hotelRoom = new HotelRoom(list[i][1], Integer.parseInt(list[i][2]),
                    Integer.parseInt(list[i][4]), rates);
                if (list[i][1].contains("Deluxe")) {
                    hotel5Star.getListOfRooms().add(hotelRoom);
                } else if (list[i][1].contains("Executive")) {
                    hotel4Star.getListOfRooms().add(hotelRoom);
                } else {
                    hotel3Star.getListOfRooms().add(hotelRoom);
                }
            }
            return new HotelList(hotel3Star, hotel4Star, hotel5Star);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can not read file hotel.csv");
        }
        return null;
    }

    /**Updates the reservation csv file
     * 
     *@author SeanFitzgerald*/
    public void updateReservationCSV() {
        CSVEncoder csvEncoder = new CSVEncoder(pathReservation);
        try {
            csvEncoder.csvWrite(resList);
        } catch (IOException e) {
            System.err.println("Can not write file reservation.csv");
        }
    }

    /**Updates the billing csv file
     * 
     *@author SeanFitzgerald*/
    public void updateBillingCSV() {
        List<Reservation> billList = new ArrayList<>();
        resList
                .stream()
                .filter(r -> r.getCheckOut().isBefore(LocalDate.now()))
                .forEach(billList::add);
        CSVEncoder csvEncoder = new CSVEncoder(pathBilling);
        try {
            csvEncoder.csvWrite(billList);
        } catch (IOException e) {
            System.err.println("Can not write file reservation.csv");
        }
    }

    /**Analyzes the occupancy figures of rooms during that period
     * 
     * @param start start date
     * @param end end date
     * @return int[] figure statistics for that period
     * @author SeanFitzgerald*/
    public int[] analyseOccupancyFigures(LocalDate start, LocalDate end) {
        int[] figures = new int[4];
        for (Reservation r : resList) {
            if ((r.getCheckIn().isAfter(start) || r.getCheckIn().isEqual(start)) &&
                (r.getCheckOut().isBefore(end) || r.getCheckOut().isEqual(end))) {
                int[] occ = r.getTotalOccupancy();
                figures[0] += occ[0];
                figures[1] += occ[1];
                figures[2] += occ[2];
            }
        }
        figures[3] = figures[0] + figures[1] + figures[2];
        return figures;
    }

    /**Analyzes occupancy rates
     * 
     * @param start start date
     * @param end end date
     * @return double[] percentage statistics for that period
     * @author SeanFitzgerald*/
    public double[] analyseOccupancyRates(LocalDate start, LocalDate end) {
        int[] currentOcc = analyseOccupancyFigures(start, end);
        int[] maxOcc = new int[4];
        for (Reservation r : resList) {
            if ((r.getCheckIn().isAfter(start) || r.getCheckIn().isEqual(start)) &&
                    (r.getCheckOut().isBefore(end) || r.getCheckOut().isEqual(end))) {
                int[] occ = r.getMaximalOccupancy();
                maxOcc[0] += occ[0];
                maxOcc[1] += occ[1];
                maxOcc[2] += occ[2];
            }
        }
        double[] rates = new double[4];
        for (int i = 0; i < 3; i++) {
            if (maxOcc[i] == 0) {
                rates[i] = 0;
            } else {
                rates[i] = (double) currentOcc[i] / maxOcc[i] * 100;
            }
        }
        int sum = maxOcc[0] + maxOcc[1] + maxOcc[2];
        if (sum == 0) {
            rates[3] = 0;
        } else {
            rates[3] = (double) currentOcc[3] / sum * 100;
        }
        return rates;
    }

    /**Analyzes hotels earnings of a certain period
     * 
     * @param start start date
     * @param end end date
     * @return double[] with earnings for each hotel type, and total
     * @author SeanFitzgerald*/
    public double[] analyseBilling(LocalDate start, LocalDate end) {
        double[] income = new double[4];
        for (Reservation r : resList) {
            if ((r.getCheckIn().isAfter(start) || r.getCheckIn().isEqual(start)) &&
                    (r.getCheckOut().isBefore(end) || r.getCheckOut().isEqual(end))) {
                RoomList rl = r.getRoomList();
                for (Room room : rl.getRooms()) {
                    String roomType = room.getRoomType();
                    double cost = calcTotalCost(roomType, r.getCheckIn(), r.getCheckOut(), r.getResType());
                    if (roomType.contains("Classic")) {
                        income[0] += cost;
                    } else if (roomType.contains("Executive")) {
                        income[1] += cost;
                    } else {
                        income[2] += cost;
                    }
                }
            }
        }
        income[3] = income[0] + income[1] + income[2];
        return income;
    }
}
