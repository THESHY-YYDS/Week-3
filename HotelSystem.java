import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.time.LocalDate;

public class HotelSystem {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    /**
     * This methode invokes the service in ReservationSystem and handles the interaction with user
     * @Author Jinhao
     * @param args which is normally ignored
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // initialise the reservation system with the default path
        ReservationSystem reservationSystem = new ReservationSystem("l4Hotels.csv",
                "reservation.csv", "billing.csv");
        // decode l4hotel.csv in order to get information about hotel and its rooms
        HotelList hotelList = reservationSystem.decodeHotelCSV();
        if (hotelList == null) {
            System.err.println("Can not decode l4Hotel.csv, exiting...");
            System.exit(1);
        }
        // initialise the header of reservation.csv and billing.csv
        reservationSystem.initCSV();
        // the program stopped when the flag is set to false
        boolean flag = true;
        // the reference number begins with 0
        int refNo = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("Welcome to the hotel reservation system developed by BestSolutions Ltd\n");
        sb.append("1 : Show all room information\n");
        sb.append("2 : Show all reservation information\n");
        sb.append("3 : Make a room reservation\n");
        sb.append("4 : Cancel a room reservation\n");
        sb.append("======================================================================\n");
        sb.append("5 : Start the occupancy analysis\n");
        sb.append("6 : Start the billing/accounts analysis\n");
        sb.append("7 : Create .csv File for all charged hotel reservations\n");
        sb.append("8 : Quit the system\n");
        System.out.println(sb);

        while(flag) {
            System.out.println("Enter one character in order to continue");
            String cmd = br.readLine().trim();
            switch (cmd) {
                case "1" -> {
                    // deliver information about all reservable rooms to user
                    System.out.println("You can choose which hotel type you want to view");
                    System.out.println("Enter 0 for all information. Enter 5 for 5-star hotel, " +
                            "4 for 4-star hotel and 3 for 3-star hotel");
                    String star = br.readLine();
                    int s = Integer.parseInt(star);
                    switch (s) {
                        case 0 -> {
                            hotelList.showAllRooms();
                        }
                        case 5 -> {
                            hotelList.showRooms5StarHotel();
                        }
                        case 4 -> {
                            hotelList.showRooms4StarHotel();
                        }
                        case 3 -> {
                            hotelList.showRooms3StarHotel();
                        }
                        default -> {
                            System.out.println("The number you entered is invalid");
                        }
                    }
                }
                case "2" -> {
                    // show all reservation made by user excludes the canceled reservations
                    reservationSystem.showAllReservation();
                }
                case "3" -> {
                    // make a new reservation
                    RoomList roomList = new RoomList();
                    System.out.println("Please enter your name");
                    String name = br.readLine();
                    System.out.println("Please enter the checkin date");
                    System.out.println("Notice: The date format should be yyyy-mm-dd");
                    LocalDate checkIn = LocalDate.parse(br.readLine());
                    System.out.println("Please enter the checkout date");
                    LocalDate checkOut = LocalDate.parse(br.readLine());

                    int numOfRoom = 0;
                    while (true) {
                        System.out.println("Please enter the room type you selected");
                        String roomType = br.readLine();
                        System.out.println("Please enter the number of occupancy");
                        int maxOcc = HotelList.getMaxOccupancy(roomType);
                        System.out.println("Notice : The room you selected has a maximum occupancy of " + maxOcc
                                + " people");
                        String occ = br.readLine();
                        int occupancy = Integer.parseInt(occ);
                        Room room = new Room(roomType, occupancy);
                        roomList.add(room);
                        numOfRoom++;
                        System.out.println("You want to reserve more rooms at once?");
                        System.out.println("Notice : You can book at most 3 rooms in one reservation");
                        System.out.println("Notice : Enter Y for yes to continue, N for no to leave");
                        if (br.readLine().toUpperCase().contains("N")) {
                            break;
                        }
                    }

                    System.out.println("Please enter the order type");
                    System.out.println("Notice: You have a choice of a standard reservation(S) or an advance " +
                            "an advance(AP)");
                    String resType = br.readLine().toUpperCase().contains("S") ? "S" : "AP";

                    double totalCost = reservationSystem.calcTotalCost(roomList, checkIn, checkOut, resType);

                    Reservation res = reservationSystem.makeReservation(refNo, name, resType, checkIn, checkOut, numOfRoom,
                            roomList, totalCost);
                    // fail to make a new reservation
                    if (res == null) {
                        System.out.println("There is no room available and your reservation can not be made");
                    } else {
                        System.out.println("Your reservation is made successfully and " +
                                "please confirm your reservation here:");
                        System.out.println(res.toString());
                        System.out.println("" + totalCost + " will be charged from your account");
                    }
                    refNo++;
                }
                case "4" -> {
                    // cancel a specified reservation with a valid reference number
                    System.out.println("Please enter your reservation number in oder to proceed");
                    while (true) {
                        String num = br.readLine();
                        int number = Integer.parseInt(num);
                        if (reservationSystem.isValidReservationNumber(number)) {
                            reservationSystem.cancelReservation(reservationSystem.findReservation(number));
                            break;
                        }
                        System.out.println("The reservation number is invalid");
                    }
                }
                case "5" -> {
                    // start the analysis of occupancy within the given period
                    System.out.println("Please enter the start date for the occupancy analysis");
                    System.out.println("Notice: The date format should be yyyy-mm-dd");
                    String startDate = br.readLine();
                    LocalDate start = LocalDate.parse(startDate);
                    System.out.println("Please enter the end date for the occupancy analysis");
                    String endDate = br.readLine();
                    LocalDate end = LocalDate.parse(endDate);
                    int[] figuresAnalysis = reservationSystem.analyseOccupancyFigures(start, end);
                    System.out.println("There are totally " + figuresAnalysis[3] + " guests stayed " +
                            "at the hotel in the given period");
                    System.out.println("There are " + figuresAnalysis[0] + " guests in the 3-star " +
                            "hotel among them");
                    System.out.println("There are " + figuresAnalysis[1] + " guests in the 4-star " +
                            "hotel among them");
                    System.out.println("There are " + figuresAnalysis[2] + " guests in the 5-star " +
                            "hotel among them");
                    double[] ratesAnalysis = reservationSystem.analyseOccupancyRates(start, end);
                    System.out.println("The total occupancy rate in the given period is about "
                            + new DecimalFormat("##.##").format(ratesAnalysis[3]) + "%");
                    System.out.println("The occupancy rate of 3-star hotel in the given period is about "
                            + new DecimalFormat("##.##").format(ratesAnalysis[0]) + "%");
                    System.out.println("The occupancy rate of 4-star hotel in the given period is about "
                            + new DecimalFormat("##.##").format(ratesAnalysis[1]) + "%");
                    System.out.println("The occupancy rate of 5-star hotel in the given period is about "
                            + new DecimalFormat("##.##").format(ratesAnalysis[2]) + "%");
                }
                case "6" -> {
                    // start the analysis of billing within the given period
                    System.out.println("Please enter the start date for the billing analysis");
                    System.out.println("Notice: The date format should be yyyy-mm-dd");
                    String startDate = br.readLine();
                    LocalDate start = LocalDate.parse(startDate);
                    System.out.println("Please enter the end date for the billing analysis");
                    String endDate = br.readLine();
                    LocalDate end = LocalDate.parse(endDate);
                    double[] billingAnalysis = reservationSystem.analyseBilling(start, end);
                    System.out.println("The total income in the given period is " +
                            new DecimalFormat("##.##").format(billingAnalysis[3]));
                    System.out.println("The income of 3-star hotel in the given period is " +
                            new DecimalFormat("##.##").format(billingAnalysis[0]));
                    System.out.println("The income of 4-star hotel in the given period is " +
                            new DecimalFormat("##.##").format(billingAnalysis[1]));
                    System.out.println("The income of 5-star hotel in the given period is " +
                            new DecimalFormat("##.##").format(billingAnalysis[2]));
                }
                case "7" -> {
                    // manually save the charged reservation into billing.csv
                    reservationSystem.updateBillingCSV();
                }
                case "8" -> {
                    flag = false;
                }
                default -> {
                    System.out.println("Please enter a valid character");
                }
            }
        }
    }
}
