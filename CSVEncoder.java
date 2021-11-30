import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code CSVEncoder} read and write file which store information of hotels
 * 
 * @author YongxinZhuang
 *
 */
public class CSVEncoder {

	private String path;

	/**
	 * {@code CSVEncoder} CSVEncoder constructor
	 * 
	 * @param path the location of the CSV file
	 */
	public CSVEncoder(String path) {
		this.path = path;
	}

	/**
	 * {@code csvRead} read the file and store it into 2d array
	 * 
	 * @return the encoded file
	 * @throws IOException
	 */
	public String[][] csvRead() throws IOException {
		ArrayList<ArrayList<String>> content = new ArrayList<>();
		ArrayList<String> lineFormatted = new ArrayList<>();

		try (Reader fr = new FileReader(path)) {
			// use BufferedReader to read the file line by line
			BufferedReader br = new BufferedReader(fr);
			StringBuilder sb = new StringBuilder();
			String line;
			while (true) {
				line = br.readLine();
				if (line == null) {
					break;
				}
				// for loop reads every char in this line
				for (char c : line.toCharArray()) {
					if (c == ',') {
						// check if StringBulider sb is empty, if so add "," into arraylist.
						if (sb.isEmpty()) {
							lineFormatted.add("");
						} else {
							lineFormatted.add(sb.toString());
							sb.setLength(0);// init sb
						}
					} else {
						sb.append(c);
					}
				}
				lineFormatted.add(sb.toString());
				sb.setLength(0);
				content.add(lineFormatted);
				lineFormatted = new ArrayList<>();// init lineFormatted
			}
		}
		// line first, then column
		String[][] result = new String[content.size()][content.get(0).size()];
		for (int i = 0; i < content.size(); i++) {
			for (int j = 0; j < content.get(0).size(); j++) {
				result[i][j] = content.get(i).get(j);
			}
		}
		return result;
	}

	/**
	 * {@code csvInit} init the csv file
	 * 
	 * @throws IOException
	 */
	public void csvInit() throws IOException {
		try (Writer fw = new FileWriter(path)) {
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append("Reservation number").append(",");
			bw.append("Reservation name").append(",");
			bw.append("Reservation type").append(",");
			bw.append("Check-in date").append(",");
			bw.append("Check-out date").append(",");
			bw.append("Number of rooms").append(",");
			bw.append("Room type").append(",");
			bw.append("Occupancy").append(",");
			bw.append("Room type").append(",");
			bw.append("Occupancy").append(",");
			bw.append("Room type").append(",");
			bw.append("Occupancy").append(",");
			bw.append("Total cost").append("\n");
			bw.flush();// forces buffered output to be written out
		}
	}

	/**
	 * {@code csv Write} write a csv file that stored reservation imformation
	 * 
	 * @param resList arraylist that stored the imformation of reserved room
	 * @throws IOException
	 */
	public void csvWrite(List<Reservation> resList) throws IOException {
		csvInit();
		try (Writer fw = new FileWriter(path, true)) {
			for (Reservation reservation : resList) {
				int numOfComma = 3 - reservation.getNumOfRoom();// we allows customers to order max 3 rooms once
				BufferedWriter bw = new BufferedWriter(fw);
				bw.append(String.valueOf(reservation.getRefNo())).append(",");
				bw.append(reservation.getName()).append(",");
				bw.append(reservation.getResType()).append(",");
				bw.append(reservation.getCheckIn().toString()).append(",");
				bw.append(reservation.getCheckOut().toString()).append(",");
				bw.append(String.valueOf(reservation.getNumOfRoom())).append(",");
				bw.append(reservation.getRoomList().toTextOutput());
				for (int i = 0; i < numOfComma; i++) {
					bw.append(",").append(",");
				}
				bw.append(String.valueOf(reservation.getTotalCost()));
				bw.append("\n");
				bw.flush();
			}
		}
	}
}
