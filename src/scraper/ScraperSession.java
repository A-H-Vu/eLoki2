package scraper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;

import org.sqlite.core.Codes;

/**
 * Class responsible for managing all the data from
 * scraping a website, uses sqlite to store all the data
 * and to easily allow the scraper to resume a scraping job
 * without the risk of data loss
 * 
 * 
 * Note: the database is currently unversioned, any changes
 * made to the data format will likely break things between versions
 * TODO add versioning via PRAGMA user_version and apply migrations based on that
 * 
 * Note2: The scraper in general assumes that a page does not change and there is nothing in
 * the database design currently to handle these cases and scrape dynamic pages with changing urls
 * i.e. the frontpage/feed of social media sites that are constantly changing, or really any 
 * news/feed sort of section of a website //Aside maybe a time range for when a url was seen on a page?
 * @author Allen
 *
 */
public class ScraperSession implements AutoCloseable{
	//To handle most errors get code and use the Codes enum in sqlite-jdbc to determine what happened
	//TODO turn off autcommit and expose some of the commit stuff to upper layers to ensure consistency
	private Connection dbConnection;
	//name of the db for delete()
	private String dbName;
	public ScraperSession() throws SQLException {
		dbConnection = DriverManager.getConnection("jdbc:sqlite:inprogressScrape.db");
		createTables();
		dbConnection.setAutoCommit(false);
		this.dbName = "inprogressScrape.db";
	}
	public ScraperSession(String dbName) throws SQLException {
		dbConnection = DriverManager.getConnection("jdbc:sqlite:"+dbName);
		createTables();
		dbConnection.setAutoCommit(false);
		this.dbName = dbName;
	}
	
	/**
	 * Creates all the tables used by the scraper
	 * using the connection specified by dbConnection
	 * @throws SQLException If there were any sqlite errors while creating the tables
	 */
	private void createTables() throws SQLException {
		Statement stmt = dbConnection.createStatement();
		//if not exists on all tables so that sqlexception is not
		//thrown when all the tables already exist
		//Not the best solution, it is better to do sanity checks to make sure
		//the schema and version is what is expected or else things will break
		//if the data model changes
		
		//TODO at some point figure out foreign keys stuff, enabling it in sqlite
		//and adding it to the table definitions - should be relatively safe as
		//the version of sqlite packaged is fixed from the native binaries in the
		//sqlite-jdbc jar
		
		//Table to store all the filters used to check the urls, includes the baseUrl
		stmt.execute("CREATE TABLE IF NOT EXISTS FILTERS (TYPE TEXT, VALUE TEXT, UNIQUE(TYPE,VALUE))");
		//Table to store all the urls that have been seen and added to the queue at some point
		//Effectively acts as a set that is checked to see if a url exists within it
		//Urls stored should be more or less as it is found in the href resolving any relative urls
		//Two urls can point to the same final visited_url if redirects end up leading to the same page
		//Is later tied to an id in the visited table to enable some more complicated stuff
		//creating a directed graph of the urls
		stmt.execute("CREATE TABLE IF NOT EXISTS SEEN (URL TEXT UNIQUE NOT NULL, VISITED_ID INTEGER)");
		//Main table for storing all the scraped urls
		//Tracks depth as well as parent url 
		stmt.execute("CREATE TABLE IF NOT EXISTS VISITED"
						+ "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "
						+ "	URL TEXT NOT NULL UNIQUE, " //url in the address bar, after all redirects etc 
						+ "	DEPTH INTEGER, " //minimum number of clicks from the baseUrl
						+ "	TIMESTAMP INTEGER)"); //miliseconds from Epoch/System.currentTimeMillis()
		//Tracks relations between urls
		//To properly create a link between a certain url and a child
		//the URL should correspond to w/e value is stored in seen and thus can be
		//linked to a child id once the url has been visited
		stmt.execute("CREATE TABLE IF NOT EXISTS EDGES (PARENT_ID INTEGER, URL TEXT)");
		
		//table that is effectively a queue of the urls to check
		//The url should match w/e is in the seen table
		stmt.execute("CREATE TABLE IF NOT EXISTS QUEUE (URL TEXT UNIQUE, DEPTH INTEGER)");
	}
	
	
	private PreparedStatement seenStmt = null;
	/**
	 * A simple check to see if the specific url has been encountered by the scraper
	 * at any point in some href text.
	 * Urls that have been seen may not have been visited yet
	 * @param url
	 * @throws SQLException 
	 */
	public boolean hasSeen(String url) throws SQLException {
		if(seenStmt==null) {
			seenStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM SEEN WHERE URL = ?");
		}
		seenStmt.setString(1, url);
		try(ResultSet rs = seenStmt.executeQuery()){
			rs.next();
			return rs.getInt(1)>0;
		}catch(Exception e) {}
		return false;
		
	}
	private PreparedStatement visitedStmt = null;
	/**
	 * A simple check to see if some specific url has been visited by the scraper already
	 * @param url
	 * @return
	 * @throws SQLException 
	 */
	public boolean hasVisited(String url) throws SQLException {
		if(visitedStmt==null) {
			visitedStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM VISITED WHERE URL = ?");
		}
		visitedStmt.setString(1, url);
		try(ResultSet rs = visitedStmt.executeQuery()){
			rs.next();
			return rs.getInt(1)>0;
		}catch(Exception e) {}
		return false;
	}
	
	
	public boolean addVisited(QueueURL url) throws SQLException {
		return addVisited(url, System.currentTimeMillis());
	}
	private PreparedStatement addVisitedStmt = null;
	/**
	 * Adds a url to the visited table along with the time it was visited
	 * @param url
	 * @param timestamp
	 * @return
	 * @throws SQLException 
	 */
	public boolean addVisited(QueueURL url, long timestamp) throws SQLException {
		if(url==null) return false;
		if(addVisitedStmt==null) {
			addVisitedStmt = dbConnection.prepareStatement("INSERT INTO VISITED(URL, DEPTH, TIMESTAMP) VALUES(?,?,?)");
		}
		addVisitedStmt.setString(1, url.url);
		addVisitedStmt.setInt(2, url.depth);
		addVisitedStmt.setLong(3, timestamp);
		return addVisitedStmt.executeUpdate()>0;
	}
	
	/**
	 * Returns an iterable over the visited urls
	 * 
	 * Note: May cause write locking as the resultset opened on the table
	 * will not be closed until it is garbage collected and the finalize method triggered
	 * 
	 * @return
	 */
	public Iterable<String> getVisitedUrls() {
		return new Iterable<String>() {
			@Override
			public Iterator<String> iterator() {
				try {
					ResultSet rs = dbConnection.createStatement().executeQuery("SELECT URL FROM VISITED");
					return new Iterator<String>() {
						//if the row the resultset is on has been read by next
						boolean read = true;
						boolean hnext = false;
						@Override
						public boolean hasNext() {
							if(read) {
								read = false;
								try {
									hnext = rs.next();
								} catch (SQLException e) {
									e.printStackTrace();
									hnext = false;
								}			
							}
							if(!hnext) {
								try {
									rs.close();
								} catch (SQLException e) {
									e.printStackTrace();
								}
							}
							return hnext;
						}

						@Override
						public String next() {
							try {
								if(read) {
									hnext = rs.next();
								}
								read = true;
								if(hnext) {
									String url = rs.getString(1);
									return url;
								}
								else {
									try {
										rs.close();
									} catch (SQLException e) {
										e.printStackTrace();
									}
									throw new IndexOutOfBoundsException();
								}
							}catch(Exception e) {
								e.printStackTrace();
								return "";
							}
						}

						//finalize to close the resultSet
						//not the best solution as gc does not occur immediately
						public final void finalize() {
							try {
								rs.close();
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}


					};
				}catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}
	
	
	
	/**
	 * Gets the baseUrl for the scrape session
	 * Returns an empty string if it does not exist in the database
	 * @throws SQLException 
	 */
	public String getBaseUrl() {
		try(ResultSet rs = dbConnection.createStatement().executeQuery("SELECT VALUE FROM FILTERS WHERE TYPE = 'BaseUrl'")){
			if(rs.next()) {
				String url = rs.getString(1);
				rs.close();
				return url;
			}
		}catch(Exception e) {};
		return "";
	}
	
	private PreparedStatement setBaseUrlStmt = null;
	/**
	 * Sets the baseUrl for the scraper session 
	 * @throws SQLException
	 * @return if the baseUrl was successfully updated
	 */
	public boolean setBaseUrl(String url) throws SQLException {
		if(setBaseUrlStmt==null) {
			setBaseUrlStmt = dbConnection.prepareStatement("INSERT INTO FILTERS (TYPE,VALUE) VALUES('BaseUrl',?)");
		}
		setBaseUrlStmt.setString(1, url);
		return setBaseUrlStmt.executeUpdate()>0;
	}
	
	/**
	 * Adds all prefix filters to the filters table if they don't already exist.
	 * 
	 * If there are any duplicates causing an sqliteconstraint error they will be skipped
	 * 
	 * If the prefixes array is null, mothing will be done
	 * @param prefixes
	 * @return
	 * @throws SQLException If there are any exceptions other than a constraint error
	 */
	public void addPrefixes(String[] prefixes) throws SQLException {
		if(prefixes==null) return;
		PreparedStatement insPrefixStmt = dbConnection.prepareStatement("INSERT INTO FILTERS(TYPE,VALUE) VALUES('Prefix',?)");
		for(String s:prefixes) {
			try {
				insPrefixStmt.setString(1, s);
				insPrefixStmt.execute();
			}catch(SQLException e) {
				//rethrow if it's not the expect constraint error for duplicated entries
				if(e.getErrorCode()!=Codes.SQLITE_CONSTRAINT) {
					throw e;
				}
			}
		}
	}
	
	public String[] getPrefixes() throws SQLException {
		ResultSet rs = dbConnection.createStatement().executeQuery("SELECT VALUE FROM FILTERS WHERE TYPE = 'Prefix'");
		ArrayList<String> prefixes = new ArrayList<String>();
		while(rs.next()) {
			prefixes.add(rs.getString(1));
		}
		return prefixes.toArray(new String[] {});
	}
	
	private PreparedStatement addSeenStmt = null;
	/**
	 * Adds the url to the seen table
	 * @param url url to add
	 * @return true if it was successfully added
	 * @throws SQLException
	 */
	public boolean addSeen(String url) throws SQLException {
		if(addSeenStmt==null) {
			addSeenStmt = dbConnection.prepareStatement("INSERT INTO SEEN (URL) VALUES (?)");
		}
		addSeenStmt.setString(1, url);
		return addSeenStmt.executeUpdate()>0;
	}
	
	
	private PreparedStatement insQueueStmt = null;
	/**
	 * Adds a url to the queue table.
	 * 
	 * Queue-like behavior comes from sqlite's default sorting by
	 * the hidden rowid column which 
	 * @return
	 * @throws SQLException 
	 */
	public boolean offerUrl(QueueURL url) throws SQLException {
		if(insQueueStmt==null) {
			insQueueStmt = dbConnection.prepareStatement("INSERT INTO QUEUE (URL, DEPTH) VALUES(?,?)");
		}
		insQueueStmt.setString(1, url.url);
		insQueueStmt.setInt(2, url.depth);
		return insQueueStmt.executeUpdate()>0;
	}
	
	/**
	 * Gets the size of the queue table
	 * @return 0 if there are any issues
	 */
	public int getQueueSize() {
		try (ResultSet rs = dbConnection.createStatement().executeQuery("SELECT COUNT(*) FROM QUEUE");){
			if(rs.next()) {
				return rs.getInt(1);
			}
		}
		catch(SQLException e) {}
		return 0;
	}
	
	/**
	 * Gets the next url in the queue
	 * @return null if there are none remaining
	 * @throws SQLException 
	 */
	public QueueURL getNextUrl() {
		try(ResultSet rs = dbConnection.createStatement().executeQuery("SELECT * FROM QUEUE")){
			if(rs.next()) {
				QueueURL url = new QueueURL(rs.getString("URL"),rs.getInt("DEPTH"));
				PreparedStatement delUrl = dbConnection.prepareStatement("DELETE FROM QUEUE WHERE URL = ?");
				delUrl.setString(1,url.url);
				delUrl.execute();
				return url;
			}
		}catch(Exception e) {}
		return null;
	}
	
	/**
	 * Commit any changes to the database if autocommit is off.
	 * 
	 * @throws SQLException 
	 */
	public void commit() throws SQLException {
		dbConnection.commit();
	}
	
	/**
	 * Sets the autocommit for the database connection, by default when
	 * a scraping session is initiated it is turned off
	 * @param autoCommit
	 */
	public void setAutoCommit(boolean autoCommit) {
		try {
			dbConnection.setAutoCommit(autoCommit);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Gets the autocommit status for the database connection, by default
	 * when a scraping session is initiated it is turned off
	 * @return
	 * @throws SQLException
	 */
	public boolean getAutoCommit() throws SQLException {
		return dbConnection.getAutoCommit();
	}
	
	/**
	 * Deletes the session, closing the connection the database file
	 */
	public void delete() {
		close();
		try {
			Files.delete(new File(dbName).toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/**
	 * Closes the database connection for the session
	 */
	public void close() {
		try {
			dbConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
