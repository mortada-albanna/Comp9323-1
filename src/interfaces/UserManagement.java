package interfaces;

import java.util.ArrayList;

import other.Permission;

/** An interface that is used for UserManagement APIs
 * 
 * @author Ervin
 *
 */
public interface UserManagement {
		
		/** Returns true if the correct password is correct for the account given account 
		 * 
		 * @param user The username of the account
		 * @param pass The Password of the account
		 * @return boolean true if the credentials are correct
		 */
		public boolean authenticateAccount(String user, String pass);

		/** Creates an account
		 * 
		 * @param givenName The given name of the account holder
		 * @param surname The surname of the account holder
		 * @param username The username of the account holder (defaults to email if null)
		 * @param password The Password of the account
		 * @param email The email adress of the account holder, cannot be null
		 * @param group The authorization group that you want the account to belong to (can be null)
		 */
		public void createAccount(String givenName, String surname, String username, 
				String password, String email, String group) ;

		/** Method to get some details of an account in a string
		 * 
		 * @param username username of the account
		 * @return String returns a message string with information eg. Username: John </br> Group: Teacher
		 */
		public String getDetails(String username);

		/** Method to set the password in an account
		 * 
		 * @param username Username of the account
		 * @param password New Password
		 * @return boolean
		 */
		public boolean setPassword(String username, String password) ;
		
		/** Returns an ArrayList of the names of authorization groups
		 * 
		 * @param username The Username of the account
		 * @return ArrayList<String> 
		 */
		public ArrayList<String> getAuthorizationGroup(String username) ;
		
		/** Will return some Permissions that has a name eg Assignment 1 and some permissions
		 * with it in an array eg [Permission.WRITE, Permission.READ]
		 * 
		 * @param user the user you are querying
		 * @return ArrayList<Permission> a list of permissions
		 */
		public ArrayList<Permission> getPermission (String user);
		

}
