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
			
			DateFormat show_date_check = new SimpleDateFormat("yyyy-MM-dd");
			show_date_check.setLenient(false);
			try{
				show_date_check.parse(show_date);
				//Check if times for date are overlapping
				query = "SELECT sttime,edtime FROM shows S, plays P WHERE  S.sid = P.sid AND tid=" + tid + " AND sdate='" + show_date +"'";
				List<List<String>> date_check_list = esql.executeQueryAndReturnResult(query);
				date_check_list.forEach(System.out::println);
				System.out.println("Enter start time for movie (FORMAT: HH:mm:ss)");
				inp = new BufferedReader (new InputStreamReader(System.in));
				String selected_start_time = inp.readLine();
				DateFormat show_time_check = new SimpleDateFormat("HH:mm:ss");
				show_time_check.setLenient(false);
				Date start_time = show_time_check.parse(selected_start_time);	
				Date end_time = Date.from(start_time.toInstant().plus(Duration.ofSeconds(Integer.parseInt(duration))));
					
				for(int i = 0; i<date_check_list.size(); i++){
					Date list_start = show_time_check.parse(date_check_list.get(i).get(0));
					Date list_end = show_time_check.parse(date_check_list.get(i).get(1));
					
					if(!(start_time.after(list_end) || end_time.before(list_start))){
						System.out.println("Conflicting times, another movie is playing at the same time");
						return;
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
		
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
		
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		
	}
	
	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
	
}
