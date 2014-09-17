package interfaces;

public interface UserManagement {
		
		public boolean authenticateAccount(String user, String pass);
		



		public void createAccount(String username, String password, String email, String group) ;

		public String getDetails(String username);


		public boolean setPassword(String username, String password) ;

}
