package apis;

import java.util.Iterator;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.AccountStoreMapping;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.application.Applications;
import com.stormpath.sdk.authc.AuthenticationResult;
import com.stormpath.sdk.authc.UsernamePasswordRequest;
import com.stormpath.sdk.client.ApiKey;
import com.stormpath.sdk.client.ApiKeys;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.directory.Directory;
import com.stormpath.sdk.resource.ResourceException;

import controller.Controller;

import java.util.logging.Logger;

public class Stormpath {
	static Logger logger = Logger.getLogger(Controller.class.getName());
	private Client client = null;
	private Application application = null;
	private Directory directory = null;
	
	public Stormpath(){
		String path = "C:\\Users\\Ervin\\Downloads\\apiKey.properties";
		logger.info(path);

		@SuppressWarnings("deprecation")
		ApiKey apiKey = ApiKeys.builder().setFileLocation(path).build();
		this.client = Clients.builder().setApiKey(apiKey).build();


		initApplication();
		initDirectory();

	}
	
	public Account authenticateAccount(String user, String pass){
		Account account = null;
		try {
		    UsernamePasswordRequest authenticationRequest = new UsernamePasswordRequest(user, pass);
		    AuthenticationResult result = application.authenticateAccount(authenticationRequest);
		    account = result.getAccount();
		} catch (ResourceException ex) {
		    System.out.println(ex.getStatus()); // Will output: 400
		    System.out.println(ex.getCode()); // Will output: 400
		    System.out.println(ex.getMessage()); // Will output: "Invalid username or password."
		    System.out.println(ex.getDeveloperMessage()); // Will output: "Invalid username or password."
		    System.out.println(ex.getMoreInfo()); // Will output: "mailto:support@stormpath.com"
		}
		
		return account;
	}
	
	public Application getApplication(){
		return application;
	}
	
	public Directory getDirectory(){
		return directory;
	}

	private void initDirectory() {
		boolean found;
		Directory accountStore = client.instantiate(Directory.class);

		Directory currentDir = null;
		Iterator directories= client.getDirectories().iterator();
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

	private void initApplication() {
		Iterator applications = client.getApplications().iterator();
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

	public Client getClient() {
		// TODO Auto-generated method stub
		return client;
	}

	public void createAccount(Account newAccount) {
		// TODO Auto-generated method stub
		application.createAccount(newAccount);
	}

	public String getDetails(String username) {
		// TODO Auto-generated method stub
		Account current = searchAccount(username);
		
		return "Username: " + current.getUsername()+ "</br>Password: ";
	}

	private Account searchAccount(String username) {
		boolean found = false;
		Iterator accounts = application.getAccounts().iterator();
		Account current = null;
		
		while(accounts.hasNext() && !found){
			current = (Account) accounts.next();
			if (current.getEmail().equals(username)){
				found = true;
			}
		}
		return current;
	}

	public boolean setPassword(String username, String password) {
		// TODO Auto-generated method stub
		boolean found = false;
		Account current = searchAccount(username);
		if (current != null){
			current.setPassword(password).save();
			found = true;
		}

		return found;
	}
}
