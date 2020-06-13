/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.time.Duration;
/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("---------");
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	//TODO: ADD SHA-256 to password	
	public static void AddUser(Ticketmaster esql){//1
		try{
			try{
				System.out.print("Enter User Email: ");
				BufferedReader inp_email = new BufferedReader (new InputStreamReader(System.in));
				String email = inp_email.readLine();
				//Check Email in database
				String query = "SELECT * FROM users WHERE email=\'" + email + "\'";
				List<List<String>> email_check = esql.executeQueryAndReturnResult(query);
				if(email_check.size() > 0){
					System.out.println("ERROR: Email already in database");
					return;
				}
				System.out.print("Enter First Name: ");
				BufferedReader inp_fname = new BufferedReader (new InputStreamReader(System.in));
				String first_name = inp_fname.readLine();
				System.out.print("Enter Last Name: ");
				BufferedReader inp_lname = new BufferedReader (new InputStreamReader(System.in));
				String last_name = inp_lname.readLine();
				System.out.print("Enter Phone Number: ");
				BufferedReader inp_pnumber = new BufferedReader (new InputStreamReader(System.in));
				String phone_number = inp_pnumber.readLine();
				System.out.print("Enter Password: ");
				BufferedReader inp_password = new BufferedReader (new InputStreamReader(System.in));
				String password = inp_password.readLine();
		
				query = "INSERT INTO users(email,lname,fname,phone,pwd) VALUES(\'"+email+"\',\'"+last_name+"\',\'"+first_name+"\',\'"+phone_number+"\',\'"+password+"\');";
				esql.executeUpdate(query);	
			}
			catch(SQLException e){
				System.out.println("Error trying to add user to database");
				System.out.println(e);
			}
				
		}
		catch(IOException e){
			System.out.println("Error trying to create user, most likely issue with reading input lines");
		}
		

	}
//TODO: Allow users to pick seats	
	public static void AddBooking(Ticketmaster esql){//2
		try{
			String email = null;
			String num_seats = null;
			String sid = null;
			String status = null;
			String bid = null;
			String query = null;
			String show_time_id = null;
			List<List<String>> available_showseats = null;
			//Grab DateTime
			String dateTime_format = "yyy-MM-dd HH:mm:ssX";
			Date date = Calendar.getInstance().getTime();
			DateFormat dateFormat = new SimpleDateFormat(dateTime_format);
			String strDate = dateFormat.format(date);
			System.out.println(strDate);

			while(true){
				System.out.println("Enter User Email (Enter q to Return to Main Menu)");
				BufferedReader inp_email = new BufferedReader(new InputStreamReader(System.in));
				email = inp_email.readLine();
				if(email.equals("q")) return;
				query = "SELECT * FROM users WHERE email = "+"\'"+ email + "\';";
				try{
					int num_results = esql.executeQuery(query);
					if(num_results == 1) break;
					else System.out.println("Invalid User Email, Please Try Again");	
				}	
				catch(SQLException e){
					System.out.println("Error executing query");	
				}	
			}

			try{
				//Bring Movie List			
				query = "SELECT * FROM movies;";
				List<List<String>> movie_list = esql.executeQueryAndReturnResult(query);
				movie_list.forEach(System.out::println);
				//Movie Selection
				System.out.println("Enter Movie ID");
				BufferedReader inp_movie_choice = new BufferedReader(new InputStreamReader(System.in));
				String movie_choice = inp_movie_choice.readLine();
				try{
					//Confirm movie with user
					query = "SELECT movies.title FROM movies WHERE mvid="+movie_choice;
					movie_list = esql.executeQueryAndReturnResult(query);
					System.out.println("Movie Chosen: " + movie_list.get(0).get(0));
					//Available Times
					query = "SELECT * FROM shows WHERE mvid="+movie_choice;
					List<List<String>> show_list = esql.executeQueryAndReturnResult(query);
					System.out.println("Available Show Times");
					System.out.println("[sid | mvid | sdate | sttime | edtime]");
					show_list.forEach(System.out::println);
					//Select a show time
					System.out.println("Select a Show Time ID");
					BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
					show_time_id = inp.readLine();
					query = "SELECT * FROM showseats WHERE bid is NULL AND sid= "+ show_time_id;
					//Grab number of seats
					available_showseats = esql.executeQueryAndReturnResult(query);
					System.out.println("Enter number of seats, Num Available: "+available_showseats.size());
					inp = new BufferedReader(new InputStreamReader(System.in));
					num_seats =  inp.readLine();
					if(Integer.parseInt(num_seats) > available_showseats.size()){
						System.out.println("Not enough seats");
						return;	
					}		
					
				}
				catch(SQLException e){
					System.out.println(e);
					return;
				}
			}
			catch(SQLException e){
				System.out.println("Error grabbing movies");
			}
			while(true){
				System.out.println("Enter status (Paid or Pending):");
				BufferedReader inp_status = new BufferedReader(new InputStreamReader(System.in));
				status = inp_status.readLine();
				if(status.equals("Paid") || status.equals("Pending")) break;
				else System.out.println("Invalid Status");
			}
			//fetch bid
			query = "SELECT MAX(bookings.bid) FROM bookings;";
			try{
				
				//Create Booking
				List<List<String>> bid_list = esql.executeQueryAndReturnResult(query);
				int next_bid = Integer.parseInt(bid_list.get(0).get(0)) + 1;
				//Update seats with bid until we reach num of seats
				
				query = "INSERT into bookings(bid,status,bdatetime,seats,sid,email) VALUES ('"+ Integer.toString(next_bid) +"','"+ status + "','" + strDate + "','" + num_seats + "','" + show_time_id +"','" + email +"')";		
				esql.executeUpdate(query);
				for(int i=0; i < Integer.parseInt(num_seats);i++){
					query = "UPDATE showseats SET bid =" + Integer.toString(next_bid) + "WHERE ssid ="+available_showseats.get(i).get(0);
					esql.executeUpdate(query);
				}
				System.out.println("Booking Complete!");
			}
			catch(SQLException e){
				System.out.println(e);	
			}

		}
		catch(IOException e){
			System.out.println("Error trying to add booking");
		}	
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		String title = null;
		String query = null;
		String description = null;
		String rdate = null;
		String country = null;
		String duration = null;
		String lang = null;
		String genre = null;
		String next_mvid = null;
		String selected_start_time = null;
		Date start_time = null;
		Date end_time = null;
		try{
			BufferedReader inp = null;
			System.out.println("Add a New Movie");
			System.out.println("Add Title of the Movie");
			inp = new BufferedReader(new InputStreamReader(System.in));
			title = inp.readLine();
			
			query = "SELECT * FROM movies WHERE title = '" + title + "'";
			List<List<String>> title_check = esql.executeQueryAndReturnResult(query);	
			if(title_check.size() > 0 ){
				System.out.println("ERROR: Movie is already in database");
				return;
			}
			System.out.println("Enter release date: (FORMAT yyyy-MM-dd)");
			inp = new BufferedReader(new InputStreamReader(System.in));
			rdate = inp.readLine();
			//release date check
			DateFormat release_date_check = new SimpleDateFormat("yyyy-MM-dd");
			release_date_check.setLenient(false);
			try{
				release_date_check.parse(rdate);
			}	
			catch(Exception e){
				System.out.println("Error: Invalid date");
				return;
			}
			System.out.println("Enter description:");
			inp = new BufferedReader(new InputStreamReader(System.in));
			description = inp.readLine();
			
			System.out.println("Enter enter country:");
			inp = new BufferedReader(new InputStreamReader(System.in));
			country = inp.readLine();
			
			System.out.println("Enter duration:");
			inp = new BufferedReader(new InputStreamReader(System.in));
			duration = inp.readLine();
			try{
				int check = Integer.parseInt(duration);	
			}	
			catch(Exception e){
				System.out.println("Invalid duration");
				return;
			}
			System.out.println("Enter lang:");
			inp = new BufferedReader(new InputStreamReader(System.in));
			lang = inp.readLine();

			System.out.println("Enter genre:");
			inp = new BufferedReader(new InputStreamReader(System.in));
			genre = inp.readLine();
			
			query = "SELECT MAX(mvid) FROM movies";
			List<List<String>> max_mvid_list = esql.executeQueryAndReturnResult(query);
			next_mvid = Integer.toString(Integer.parseInt(max_mvid_list.get(0).get(0)) + 1);
							
			query = "INSERT into movies(mvid,title,rdate,country,description,duration,lang,genre) VALUES ('" + next_mvid + "','" + title + "','" + rdate + "','" + country + "','" + description+ "','" + duration + "','" + lang + "','" + genre + "')";
			esql.executeUpdate(query);	
		
			//Add show
			query = "SELECT * FROM theaters";
			List<List<String>> theater_list = esql.executeQueryAndReturnResult(query);
			theater_list.forEach(System.out::println);
			System.out.println("Select Theater ID:");
			inp= new BufferedReader(new InputStreamReader(System.in));
			String tid =  inp.readLine();
			//Input start time and then calculate end time, then we have to check theater id for shows playing at that time.
			System.out.println("===Current Shows Playing===");
			query = "SELECT title,sdate,sttime,edtime FROM shows S, plays P, movies M WHERE P.sid = S.sid AND S.mvid = M.mvid AND tid=" + tid;
			List<List<String>> current_shows = esql.executeQueryAndReturnResult(query);
			current_shows.forEach(System.out::println);
			System.out.println("Enter Start Date (FORMAT: yyyy-MM-dd)");
			inp = new BufferedReader(new InputStreamReader(System.in));
			String show_date = inp.readLine();	
			
			DateFormat show_time_check = new SimpleDateFormat("HH:mm:ss");
			show_time_check.setLenient(false);
			DateFormat show_date_check = new SimpleDateFormat("yyyy-MM-dd");
			show_date_check.setLenient(false);
			boolean time_conflict_check = true;
			try{
				show_date_check.parse(show_date);
				while(time_conflict_check == true){
					//Check if times for date are overlapping
					time_conflict_check = false;
					query = "SELECT sttime,edtime FROM shows S, plays P WHERE  S.sid = P.sid AND tid=" + tid + " AND sdate='" + show_date +"'";
					List<List<String>> date_check_list = esql.executeQueryAndReturnResult(query);
					date_check_list.forEach(System.out::println);
					System.out.println("Enter start time for movie (FORMAT: HH:mm:ss)");
					inp = new BufferedReader (new InputStreamReader(System.in));
					selected_start_time = inp.readLine();
					start_time = show_time_check.parse(selected_start_time);	
					end_time = Date.from(start_time.toInstant().plus(Duration.ofSeconds(Integer.parseInt(duration))));
						
					for(int i = 0; i<date_check_list.size(); i++){
						Date list_start = show_time_check.parse(date_check_list.get(i).get(0));
						Date list_end = show_time_check.parse(date_check_list.get(i).get(1));
						
						if(!(start_time.after(list_end) || end_time.before(list_start))){
							System.out.println("Conflicting times, another movie is playing at the same time");
							time_conflict_check = true;
						}
					}
				}
				//Get data ready to insert into shows
				String endtime_str = show_time_check.format(end_time);
				//Grab max show id
				query = "SELECT MAX(sid) FROM shows";
				int next_sid = Integer.parseInt(esql.executeQueryAndReturnResult(query).get(0).get(0)) + 1;	
				query = "INSERT into shows(sid,mvid,sdate,sttime,edtime) VALUES('" + Integer.toString(next_sid) + "','" + next_mvid + "','" + show_date + "','" + selected_start_time + "','" + endtime_str + "')";
				esql.executeUpdate(query);
				//Insert into table plays as well
				query = "INSERT into plays(sid,tid) VALUES('" + Integer.toString(next_sid) + "','" + tid + "')";
				esql.executeUpdate(query);	
				System.out.println("Show Added"); 
			}	
			catch(Exception e){
				System.out.println("Error: Invalid date");
				System.out.println(e);
				return;
			}

			

		}
		catch(Exception e){
			System.out.println(e);	
		}
					
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
	String query = null;
		try{
			query = "UPDATE bookings SET status = 'Cancelled' WHERE status = 'Pending'";
			esql.executeUpdate(query);	
			System.out.println("All pending bookings have been successfully cancelled."); 	
		}
		catch(SQLException e){
			System.out.println(e);	
		}
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
	String query = null;
	String changedSeat = null;
	boolean foundSeat = false;
		try{
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter a bookings ID to change seating information");
			String bid = inp.readLine();
			query = "SELECT * FROM bookings WHERE bid = '" + bid + "'";
			List<List<String>> bid_check = esql.executeQueryAndReturnResult(query);
			if(bid_check.size() == 0){
				System.out.println("Invalid booking ID");
				return;
			}
			//Grab seating info
			query = "SELECT sno FROM showseats S, cinemaseats C WHERE S.csid = C.csid AND bid = '" + bid + "' ORDER BY C.sno ASC";			
			List<List<String>> seat_no_list = esql.executeQueryAndReturnResult(query);
			System.out.println("Your Seat Numbers");
			seat_no_list.forEach(System.out::println);
			System.out.println("Enter the seat number you currently have reserved that you would like to change");
			inp = new BufferedReader(new InputStreamReader(System.in));
			String origSeat = inp.readLine();
			for(int i = 0; i < seat_no_list.size(); i++){
				if(seat_no_list.get(i).get(0).equals(origSeat)){
					foundSeat = true;
					break;
				}	
			}
			
			if(!foundSeat)
			{
				System.out.println("Invalid seat ID");
				return;					
			}
			
			query = "SELECT S.price,S.sid FROM showseats S, cinemaseats C WHERE C.sno = '" + origSeat + "' AND  S.csid = C.csid AND bid = '" + bid + "'" ;			
			List<List<String>> origSeatPrice = esql.executeQueryAndReturnResult(query);


			System.out.println("Enter the seat number you would like your current seat " + origSeat + " to be replaced by. You must choose a seat that is available and the same price as your old one. (" + origSeatPrice.get(0).get(0)+ " dollars)");
			query = "SELECT C.sno, S.price FROM showseats S, cinemaseats C WHERE S.csid = C.csid AND  S.sid = '" + origSeatPrice.get(0).get(1) + "' AND bid is null";
			List<List<String>> avail_seats = esql.executeQueryAndReturnResult(query);
			if(avail_seats.size() == 0){
				System.out.println("Sorry there are no seats available at the same price!");
				return;
			}
			System.out.println("Seat No. | Price");
			avail_seats.forEach(System.out::println); 
			inp = new BufferedReader(new InputStreamReader(System.in));
			String replacementSeat = inp.readLine();
			query = "SELECT C.sno FROM showseats S, cinemaseats C WHERE S.sid = '" + origSeatPrice.get(0).get(1) +"' AND  C.sno = '" + replacementSeat + "' AND S.csid = C.csid AND S.bid is NULL AND S.price = '" + origSeatPrice.get(0).get(0) + "'";						
			List<List<String>> isSeatAvailList = esql.executeQueryAndReturnResult(query);
			if(isSeatAvailList.size() == 0){
				System.out.println("Invalid seat number. You must choose a seat that is available and the same price as your old one.");
				return;
			}
			
			query = "UPDATE showseats S SET bid = '" + bid + "' WHERE S.csid = (SELECT C.csid FROM cinemaseats C, showseats S WHERE C.sno = '" + replacementSeat + "' AND S.sid ='" + origSeatPrice.get(0).get(1) + "' AND C.csid = S.csid)";
			esql.executeUpdate(query);
			System.out.println("CHECK");
			query = "UPDATE showseats S SET bid = NULL WHERE S.csid = (SELECT C.csid FROM cinemaseats C, showseats S WHERE C.sno = '" + origSeat + "' AND S.sid = '" + origSeatPrice.get(0).get(1) + "' AND C.csid = S.csid)";
			esql.executeUpdate(query);
			
			
			System.out.println("Your seat number " + origSeat + " has been successfully changed to seat number " + replacementSeat + ".");

			
		}
		catch(Exception e){
			System.out.println(e);
		}	
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
	String query = null;
		try{
			BufferedReader inp = new BufferedReader (new InputStreamReader(System.in));
			System.out.println("Enter a bookings ID to cancel.");
			String bid = inp.readLine();
			query = "SELECT * FROM bookings WHERE bid = '" + bid + "'";
			List<List<String>> bid_check = esql.executeQueryAndReturnResult(query);
			if(bid_check.size() == 0){
				System.out.println("Invalid booking ID");
				return;
			}

			query = "UPDATE bookings SET status = 'Cancelled' WHERE bid = '" + bid + "'";
			esql.executeUpdate(query);
			query = "UPDATE showseats SET bid = NULL WHERE bid = '" + bid + "'";
			esql.executeUpdate(query);
			query = "DELETE FROM payments WHERE bid = '" + bid + "'";
			esql.executeUpdate(query);
			System.out.println("Bookings ID " + bid + " has been successfully cancelled."); 	
		}
		catch(Exception e){
			System.out.println(e);	
		}

	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
	String query = null;
		try{
			query = "REMOVE FROM bookings WHERE status = 'Cancelled'";
			esql.executeUpdate(query);	
			System.out.println("All cancelled bookings have been successfully removed."); 	
		}
		catch(SQLException e){
			System.out.println(e);	
		}
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		//Grab Date
		try{
			System.out.println("Enter Date: (FORMAT yyyy-MM-dd)");
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
			String date = inp.readLine();
			String query = null;

			DateFormat show_date_check = new SimpleDateFormat("yyyy-MM-dd");
			show_date_check.setLenient(false);
			show_date_check.parse(date);
			
			query = "SELECT X.cid, X.cname, X.tnum, C.city_name FROM cinemas X, cities C WHERE X.city_id = C.city_id";
			List<List<String>> cinema_list = esql.executeQueryAndReturnResult(query);
			System.out.println("Cinema ID | Cinema Name | Num Theaters | City");
			cinema_list.forEach(System.out::println);
			System.out.println("Enter a Cinema ID");
			inp = new BufferedReader(new InputStreamReader(System.in));

			String cinema_id = inp.readLine();
			query = "SELECT * FROM theaters WHERE cid=" + cinema_id;
			List<List<String>> theater_list = esql.executeQueryAndReturnResult(query);

			for(int i = 0; i < theater_list.size(); i++){
				query = "DELETE FROM plays P USING shows S, theaters T WHERE S.sdate='"+ date +"' AND P.sid = S.sid AND P.tid=T.tid AND T.tid = " + theater_list.get(i).get(0);
				esql.executeUpdate(query);
			}
			
			System.out.println("Successfully removed all shows in a cinema playing on the date of " + date);

		  }
			catch(Exception e){
				System.out.println(e);
			}
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		String query = "SELECT X.cid, X.cname, X.tnum, C.city_name FROM cinemas X, cities C WHERE X.city_id = C.city_id";
		String cinema_id;
		try{
			List<List<String>> cinema_list = esql.executeQueryAndReturnResult(query);
			System.out.println("Cinema ID | Cinema Name | Num Theaters | City");
			cinema_list.forEach(System.out::println);
			System.out.println("Enter a Cinema ID");
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));

			cinema_id = inp.readLine();
			query = "SELECT * FROM theaters WHERE cid=" + cinema_id;
			List<List<String>> theater_list = esql.executeQueryAndReturnResult(query);
			
			query = "SELECT mvid, title FROM movies";
			System.out.println("movie id | title");
			List<List<String>> movie_list = esql.executeQueryAndReturnResult(query);
			movie_list.forEach(System.out::println);
			System.out.println("Select a movie id: ");
			inp = new BufferedReader(new InputStreamReader(System.in));
			String selected_mvid  = inp.readLine();
			
			query = "SELECT * FROM movies WHERE mvid=" + selected_mvid;
			List<List<String>> mvid_check = esql.executeQueryAndReturnResult(query);
			if(mvid_check.size() == 0){
				System.out.println("Invalid mvid");
				return;
			}
			
			System.out.println("Theater ID | Theater Name | Start Date | Start Time | End Time | Movie Title");
			for(int i = 0; i < theater_list.size(); i++){
				query = "SELECT P.tid,T.tname,S.sdate,S.sttime,S.edtime,M.title FROM plays P, shows S, movies M, theaters T WHERE S.mvid = M.mvid AND P.sid = S.sid AND P.tid=T.tid AND T.tid = " + theater_list.get(i).get(0) + "AND M.mvid = " + selected_mvid;
				List<List<String>> temp = esql.executeQueryAndReturnResult(query);
				temp.forEach(System.out::println);
			}
			System.out.println("Done listing all theaters in a cinema playing a given show");
		}
		catch(Exception e){
			System.out.println(e);
		}
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//Grab Date
		try{
		System.out.println("Enter Date: (FORMAT yyyy-MM-dd)");
		BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
		String date = inp.readLine();
		String query = null;

		DateFormat show_date_check = new SimpleDateFormat("yyyy-MM-dd");
		show_date_check.setLenient(false);
		show_date_check.parse(date);
		System.out.println("Enter Start Time (FORMAT HH:mm:ss)");
		inp = new BufferedReader(new InputStreamReader(System.in));	
		String sttime = inp.readLine();
		DateFormat start_time_check = new SimpleDateFormat("HH:mm:ss");		
		start_time_check.parse(sttime);
		query = "SELECT C.cname, T.tname,S.sdate,S.sttime,S.edtime,M.title FROM shows S , movies M,plays P, theaters T, cinemas C WHERE S.sdate='"+ date +"' AND S.sttime='" + sttime +"' AND S.mvid=M.mvid AND P.sid = S.sid AND T.cid = C.cid AND P.tid = T.tid";
		List<List<String>> shows = esql.executeQueryAndReturnResult(query);
		System.out.println("Cinema | Theater | Start Date | Start Time | End Time | Movie Title");
		shows.forEach(System.out::println);			
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		try{
			//
			String query = "SELECT title, rdate FROM movies WHERE rdate >= '2010-01-01' AND title LIKE '%Love%'";
			List<List<String>> query_result = esql.executeQueryAndReturnResult(query);
			System.out.println("Movie Title | Release Date");
			query_result.forEach(System.out::println);	
			
			query = "SELECT title, rdate FROM movies WHERE rdate >= '2010-01-01' AND title LIKE '%love%'";
			query_result = esql.executeQueryAndReturnResult(query);
			query_result.forEach(System.out::println);	
		}
		catch(Exception e){
			System.out.println(e);
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		try{
			String query = "SELECT fname,lname,U.email FROM users U, (SELECT * FROM bookings WHERE status = 'Pending') X WHERE X.email = U.email";
			List<List<String>> query_result = esql.executeQueryAndReturnResult(query);
			System.out.println("First Name | Last Name | Email ");
			query_result.forEach(System.out::println);
			System.out.println("Done Printing Bookings");
		}
		catch(Exception e){
			System.out.println(e);	
		}
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		try{
			//Select Movie
			String query = null;
			List<List<String>> query_result = esql.executeQueryAndReturnResult("SELECT mvid,title FROM movies");
			System.out.println("Movie ID | Movie Title");
			query_result.forEach(System.out::println);
			System.out.println("Select a Movie ID");
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
			String mvid = inp.readLine();
			try{
				query = "SELECT * FROM movies WHERE mvid='" + mvid + "'"; 
				List<List<String>> selected_query = esql.executeQueryAndReturnResult(query);
				if(selected_query.size() == 0){
					System.out.println("ERROR: No movies with movie id found");
					return;
				}
				//Select Cinema
				System.out.println("Select Cinema");
				query = "SELECT C1.cid,C1.cname,C2.city_name,C2.city_state,C2.zip_code FROM cinemas C1, cities C2 WHERE C1.city_id = C2.city_id";
				List<List<String>> cinema_list = esql.executeQueryAndReturnResult(query);
				System.out.println("Cinema ID | Cinema Name | City Name | City State | Zip Code");
				cinema_list.forEach(System.out::println);
				System.out.println("Select a cinema ID");
				inp = new BufferedReader(new InputStreamReader(System.in));
				String cid = inp.readLine();
						
				//Select Date Range
				System.out.println("Enter Start Search Date (FORMAT: yyyy-MM-dd)");
				inp = new BufferedReader(new InputStreamReader(System.in));
				String sdate= inp.readLine();
						
				DateFormat date_check = new SimpleDateFormat("yyyy-MM-dd");
				date_check.setLenient(false);
				try{
					date_check.parse(sdate);
				}	
				catch(Exception e){
					System.out.println("Error: Invalid date");
					return;
				}
				
				System.out.println("Enter End Search Date (FORMAT: yyyy-MM-dd)");
				inp = new BufferedReader(new InputStreamReader(System.in));
				String edate= inp.readLine();
				try{
					date_check.parse(edate);
				}	
				catch(Exception e){
					System.out.println("Error: Invalid date");
					return;
				}
				query = "SELECT C1.cname,C2.city_name FROM cinemas C1, cities C2 WHERE C1.city_id= C2.city_id AND C1.cid = '" + cid + "'";
				List<List<String>>  cinema_display = esql.executeQueryAndReturnResult
(query);
				System.out.println("Movies at " + cinema_display.get(0).get(0) + " in " + cinema_display.get(0).get(1));
				
				query = "SELECT M.title, S.sdate,S.sttime,S.edtime,T.tid,T.tname FROM theaters T, plays P, shows S, movies M WHERE T.cid = '" + cid + "' AND S.mvid = M.mvid AND P.sid = S.sid AND T.tid = P.tid and S.sdate BETWEEN '" + sdate + "' AND '" + edate + "' AND M.mvid = '" + mvid + "'";
				List<List<String>> shows_list = esql.executeQueryAndReturnResult(query);
				System.out.println("Movie Title | Show Date | Start Time | End Time | Theater ID  | Theater name");		
				shows_list.forEach(System.out::println);				
				System.out.println("Done Printing Shows");
			}
			catch(SQLException e){
				System.out.println("Invalid Movie ID");
				System.out.println(e);
				return;
			}	
		}
		catch(Exception e){
			System.out.println(e);
		}
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		try{
			System.out.println("Enter User Email");
			BufferedReader inp = new BufferedReader(new InputStreamReader(System.in));
			String email = inp.readLine();
			
			String query = "SELECT * FROM users WHERE email = '" + email + "'";
			List<List<String>> user_check = esql.executeQueryAndReturnResult(query);
			if(user_check.size() == 0){
				System.out.println("User does not exist");
				return;
			}
			query = "SELECT B.bid, S.sdate,S.sttime, S.edtime, M.title, T.tname FROM bookings B, shows S, movies M, theaters T,plays P WHERE S.sid = P.sid AND P.tid = T.tid AND B.sid = S.sid AND S.mvid = M.mvid AND B.email = '" + email + "'";		
			List<List<String>> bookings_list = esql.executeQueryAndReturnResult(query);
			System.out.println(" Booking ID | Show Date | Start Time | End Time | Movie Title | Theater Name");
			bookings_list.forEach(System.out::println);	
			System.out.println("Enter a bookings ID to get seating information");
			inp = new BufferedReader (new InputStreamReader(System.in));
			String bid = inp.readLine();
			query = "SELECT * FROM bookings WHERE email = '" + email + "' AND bid = '" + bid + "'";
			List<List<String>> bid_check = esql.executeQueryAndReturnResult(query);
			if(bid_check.size() == 0){
				System.out.println("Invalid booking ID");
				return;
			}
			//Grab seating info
			query = "SELECT sno FROM showseats S, cinemaseats C WHERE S.csid = C.csid AND bid = '" + bid + "' ORDER BY C.sno ASC";			
			List<List<String>> seat_no_list = esql.executeQueryAndReturnResult(query);
			System.out.println("Your Seat Numbers");
			seat_no_list.forEach(System.out::println);
			System.out.println("Done Printing Seats");
		}
		catch(Exception e){
			System.out.println(e);
		}		
	}
	
}
