package apis;
import interfaces.UserManagement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

import other.Constants;
import other.Permission;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.application.AccountStoreMapping;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.client.ApiKey;
import com.stormpath.sdk.client.ApiKeys;
import com.stormpath.sdk.client.AuthenticationScheme;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.directory.Directory;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import com.stormpath.sdk.group.GroupStatus;
import com.stormpath.sdk.resource.ResourceException;

import controller.Controller;
/** Class to access the Stormpath API
 * 
 * @author Ervin
 *
 */
public class Stormpath implements UserManagement{
	/**
	 * Error logger
	 */
	static Logger logger = Logger.getLogger(Controller.class.getName());
	/**
	 * API client
	 */
	private Client client = null;
	/**
	 * API application
	 */
	private Application application = null;
	/**
	 * API directory
	 */
	private Directory directory = null;
	/**
	 * Student group constant
	 */
	public static final String StudentGroup = "Student";
	/**
	 * Teacher group constant
	 */
	public static final String TeacherGroup = "Teacher";
	
	/** initializes the stormpath api as well as the application, directory and groups that are needed.
	 * Uses a Constants class to determine where the apikey is.
	 * You need to create a class called Constants with a field called APIKEY_PATH which contains a path to your apiKey.properties
	 */
	public Stormpath(){
		
		String path = Constants.APIKEY_PATH;
		logger.info(path);

		@SuppressWarnings("deprecation")
		ApiKey apiKey = ApiKeys.builder().setFileLocation(path).build();
		client = Clients.builder().setApiKey(apiKey)
			    .setAuthenticationScheme(AuthenticationScheme.BASIC)
			    .build();


		initApplication();
		initDirectory();
		initAuthorizationGroups();

	}
	
	public boolean authenticateAccount(String user, String pass){
		boolean authenticated = false;
		try {
		    UsernamePasswordRequest authenticationRequest = new UsernamePasswordRequest(user, pass);
		    AuthenticationResult result = application.authenticateAccount(authenticationRequest);
		    Account account = result.getAccount();
		    if (account != null){
		    	authenticated = true;
		    }
		} catch (ResourceException ex) {
		    System.out.println(ex.getStatus()); // Will output: 400
		    System.out.println(ex.getCode()); // Will output: 400
		    System.out.println(ex.getMessage()); // Will output: "Invalid username or password."
		    System.out.println(ex.getDeveloperMessage()); // Will output: "Invalid username or password."
		    System.out.println(ex.getMoreInfo()); // Will output: "mailto:support@stormpath.com"
		}
		
		return authenticated;
	}
	
	/** Initializes the default directory that the accounts get created into
	 * 
	 */
	private void initDirectory() {
		boolean found;
		Directory accountStore = client.instantiate(Directory.class);

		Directory currentDir = null;
		Iterator<Directory> directories= client.getDirectories().iterator();
		found = false;
		while (directories.hasNext() && !found){
			currentDir = (Directory) directories.next();
			if (currentDir.getName().equals("default")){
				found = true;
			}
		}

		if (found){
			this.directory = currentDir;
		}else{
			accountStore.setName("default");
			client.createDirectory(accountStore);

			AccountStoreMapping accountStoreMapping = client.instantiate(AccountStoreMapping.class)
					.setAccountStore(accountStore) // this could be an existing group or a directory
					.setApplication(application)
					.setDefaultAccountStore(Boolean.TRUE)
					.setDefaultGroupStore(Boolean.FALSE)
					.setListIndex(0);

			application.createAccountStoreMapping(accountStoreMapping);
		}
	}

	/** Initializes the application
	 * 
	 */
	private void initApplication() {
		Iterator<Application> applications = client.getApplications().iterator();
		Application current = null;
		boolean found = false;
		while (applications.hasNext() && !found){
			current = (Application) applications.next();
			logger.info(current.getName());
			if (current.getName().equals("Give")){
				found = true;
			}
		}

		if (!found){
			logger.info("App not found, creating new one");
			Application application = client.instantiate(Application.class);
			application.setName("Give"); //must be unique among your other apps
			application = client.createApplication(
					Applications.newCreateRequestFor(application).createDirectory().build()
					);
		}else{
			logger.info("App already exists");

			application = current;
		}
	}


	public String getDetails(String username) {
		Account current = searchAccount(username);
		
		return "Username: " + current.getUsername()+ "</br>Password: ";
	}

	/** Utility function that returns an account object given the account username
	 * 
	 * @param username The username of the account
	 * @return Account The account object
	 */
	private Account searchAccount(String username) {
		boolean found = false;
		Iterator<Account> accounts = application.getAccounts().iterator();
		Account current = null;
		
		while(accounts.hasNext() && !found){
			current = (Account) accounts.next();
			if (current.getEmail().equals(username)){
				found = true;
			}
		}
		return current;
	}

	@Override
	public boolean setPassword(String username, String password) {
		boolean found = false;
		Account current = searchAccount(username);
		if (current != null){
			current.setPassword(password).save();
			found = true;
		}

		return found;
	}

	@Override
	public void createAccount(String givenName, String surname, String username, 
			String password, String email, String group) {
		Account newAccount = client.instantiate(Account.class);
		newAccount.setGivenName(givenName);
		newAccount.setSurname(surname);
		newAccount.setUsername(username);
		newAccount.setPassword(password);
		newAccount.setEmail(email);
		
		application.createAccount(newAccount);
		addToGroup(newAccount, group);
		
	}
	
	/** Initializes Authorization groups
	 * 
	 */
	private void initAuthorizationGroups() {
		boolean found = false;
		GroupList groups = application.getGroups();
		for(Group group : groups) {
			System.out.println(group.getName());
			if (group.getName().equals(StudentGroup) || group.getName().equals(TeacherGroup)){
				found = true;
			}
		}
		if (!found){
			createGroup(StudentGroup, "authorization group for students");
			createGroup(TeacherGroup, "authorization group for teachers");
		}

	}
	
	/** Adds an account to a group with just the name of the account
	 * 
	 * @param acc The name of the account
	 * @param groupName The name of the group
	 */
	public void addToGroup(String acc, String groupName){
		AccountList accounts = application.getAccounts();
		for(Account account : accounts) {
			if (account.getUsername().equals(acc)){
				addToGroup(account, groupName);
			}
		}
	}
	
	/** Adds an account to a group
	 * 
	 * @param acc an account object to add to the group
	 * @param groupName the name of the group
	 */
	public void addToGroup(Account acc, String groupName){
		GroupList groups = application.getGroups();
		for(Group group : groups) {
			System.out.println(group.getName());
			if (group.getName().equals(groupName)){
				acc.addGroup(group);
				System.out.println("Successfully added to group");
			}else{
				System.out.println(group.getName() + " does not match " + groupName);
				
			}
		}
		
	}
	
	/** Creates a general group eg. an authorization group
	 * 
	 * @param groupName the name of the group
	 * @param description a general description of the group
	 */
	private void createGroup(String groupName, String description){
		Group group = client.instantiate(Group.class)
				.setName(groupName)
				.setDescription(description)
				.setStatus(GroupStatus.ENABLED);

		application.createGroup(group);

	}

	@Override
	public ArrayList<String> getAuthorizationGroup(String username) {
		ArrayList<String> groupList = new ArrayList<String>();
		
		for (Group group:searchAccount(username).getGroups()){
			groupList.add(group.getName());
		}
		return groupList;
	}

	@Override
	public ArrayList<Permission> getPermission(String user) {
		// TODO Auto-generated method stub
		return null;
	}

}
