package interfaces;

import java.util.ArrayList;

import other.Permission;

public interface UserManagement {
		
		public boolean authenticateAccount(String user, String pass);

		public void createAccount(String givenName, String surname, String username, 
				String password, String email, String group) ;

		public String getDetails(String username);

		public boolean setPassword(String username, String password) ;
		
		public ArrayList<String> getAuthorizationGroup(String username) ;
		
		/*
		 * Will return some Permissions that has a name eg Assignment 1 and some permissions
		 * with it in an array eg [Permission.WRITE, Permission.READ]
		 */
		public ArrayList<Permission> getPermission (String user);
		

}
