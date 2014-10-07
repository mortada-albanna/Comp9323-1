package apis;

import other.Constants;
import other.FileDownloadLink;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Get;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.oauth2.*;
import com.google.api.services.oauth2.Oauth2.Userinfo;
import com.google.api.services.oauth2.model.Userinfoplus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;




// ...

public class GoogleDrive {

	// Path to client_secrets.json which should contain a JSON document such as:
	//   {
	//     "web": {
	//       "client_id": "[[YOUR_CLIENT_ID]]",
	//       "client_secret": "[[YOUR_CLIENT_SECRET]]",
	//       "auth_uri": "https://accounts.google.com/o/oauth2/auth",
	//       "token_uri": "https://accounts.google.com/o/oauth2/token"
	//     }
	//   }
	private static final String REDIRECT_URI = "http://localhost:8080/COMP9323/controller";
	private static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive.file",
			"https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile");
	private Drive drive = null;

	public GoogleDrive(){

	}

	private static GoogleAuthorizationCodeFlow flow = null;

	/**
	 * Exception thrown when an error occurred while retrieving credentials.
	 */
	public static class GetCredentialsException extends Exception {

		protected String authorizationUrl;

		/**
		 * Construct a GetCredentialsException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public GetCredentialsException(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * Set the authorization URL.
		 */
		public void setAuthorizationUrl(String authorizationUrl) {
			this.authorizationUrl = authorizationUrl;
		}

		/**
		 * @return the authorizationUrl
		 */
		public String getAuthorizationUrl() {
			return authorizationUrl;
		}
	}

	/**
	 * Exception thrown when a code exchange has failed.
	 */
	public static class CodeExchangeException extends GetCredentialsException {

		/**
		 * Construct a CodeExchangeException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public CodeExchangeException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Exception thrown when no refresh token has been found.
	 */
	public static class NoRefreshTokenException extends GetCredentialsException {

		/**
		 * Construct a NoRefreshTokenException.
		 *
		 * @param authorizationUrl The authorization URL to redirect the user to.
		 */
		public NoRefreshTokenException(String authorizationUrl) {
			super(authorizationUrl);
		}

	}

	/**
	 * Exception thrown when no user ID could be retrieved.
	 */
	private static class NoUserIdException extends Exception {
	}

	/**
	 * Retrieved stored credentials for the provided user ID.
	 *
	 * @param userId User's ID.
	 * @return Stored Credential if found, {@code null} otherwise.
	 */
	static Credential getStoredCredentials(String userId) {
		// TODO: Implement this method to work with your database. Instantiate a new
		// Credential instance with stored accessToken and refreshToken.
		Credential cred = new GoogleCredential();
		File file = new File ("credentials.txt");
		try {
			cred.setAccessToken(readRow(file));
			cred.setRefreshToken(readRow(file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return cred;
	}
	
	private static String readRow (File file) throws IOException{
		String row = "";
		FileInputStream stream = new FileInputStream(file);
		int in = stream.read();
		while (in != '\n'){
			row = row.concat(Integer.toString(in));
		}
		
		return row;
	}

	/**
	 * Store OAuth 2.0 credentials in the application's database.
	 *
	 * @param userId User's ID.
	 * @param credentials The OAuth 2.0 credentials to store.
	 */
	static void storeCredentials(String userId, Credential credentials) {
		// TODO: Implement this method to work with your database.
		// Store the credentials.getAccessToken() and credentials.getRefreshToken()
		// string values in your database.
		File file = new File("credentials.txt");
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			String accessToken = credentials.getAccessToken();
			String refreshToken = credentials.getRefreshToken();
			fos.write(accessToken.getBytes());
			fos.write("\n".getBytes());
			fos.write(refreshToken.getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Build an authorization flow and store it as a static class attribute.
	 *
	 * @return GoogleAuthorizationCodeFlow instance.
	 * @throws IOException Unable to load client_secrets.json.
	 */
	public static GoogleAuthorizationCodeFlow getFlow() throws IOException {
		if (flow == null) {
			HttpTransport httpTransport = new NetHttpTransport();
			JacksonFactory jsonFactory = new JacksonFactory();
			Reader reader = new Reader() {
				FileInputStream fis;

				@Override
				public int read(char[] cbuf, int off, int len) throws IOException {
					// TODO Auto-generated method stub
					int value = -1;
					java.io.File file = new File(Constants.CLIENT_SECRET2_PATH); 
					fis = new FileInputStream(file);
					byte[] bites = new byte[len];
					try{
						value = fis.read(bites, off, len);
						for (int i = 0; i < len; i++){
							cbuf[i] = (char) bites[i];
						}
					}catch(NullPointerException e){
						e.printStackTrace();
					}catch(IOException e){
						e.printStackTrace();
					}
					fis.close();
					return value;
				}

				@Override
				public void close() throws IOException {
					// TODO Auto-generated method stub
				}
			};
			GoogleClientSecrets clientSecrets = 
					GoogleClientSecrets.load(jsonFactory,
							reader);
			flow =
					new GoogleAuthorizationCodeFlow.Builder(httpTransport, jsonFactory, clientSecrets, SCOPES)
			.setAccessType("offline").setApprovalPrompt("force").build();
		}
		return flow;
	}

	/**
	 * Exchange an authorization code for OAuth 2.0 credentials.
	 *
	 * @param authorizationCode Authorization code to exchange for OAuth 2.0
	 *        credentials.
	 * @return OAuth 2.0 credentials.
	 * @throws CodeExchangeException An error occurred.
	 */
	static Credential exchangeCode(String authorizationCode)
			throws CodeExchangeException {
		try {
			GoogleAuthorizationCodeFlow flow = getFlow();
			GoogleTokenResponse response =
					flow.newTokenRequest(authorizationCode).setRedirectUri(REDIRECT_URI).execute();
			return flow.createAndStoreCredential(response, null);
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
			throw new CodeExchangeException(null);
		}
	}

	/**
	 * Send a request to the UserInfo API to retrieve the user's information.
	 *
	 * @param credentials OAuth 2.0 credentials to authorize the request.
	 * @return User's information.
	 * @throws NoUserIdException An error occurred.
	 */
	static com.google.api.services.oauth2.model.Userinfoplus getUserInfo(Credential credentials)
			throws NoUserIdException {
		Oauth2 userInfoService =
				new Oauth2.Builder(new NetHttpTransport(), new JacksonFactory(), credentials).build();
		Userinfoplus userInfo = null;
		try {
			userInfo = userInfoService.userinfo().get().execute();
		} catch (IOException e) {
			System.err.println("An error occurred: " + e);
		}
		if (userInfo != null && userInfo.getId() != null) {
			return userInfo;
		} else {
			throw new NoUserIdException();
		}
	}

	/**
	 * Retrieve the authorization URL.
	 *
	 * @param emailAddress User's e-mail address.
	 * @param state State for the authorization URL.
	 * @return Authorization URL to redirect the user to.
	 * @throws IOException Unable to load client_secrets.json.
	 */
	public static String getAuthorizationUrl(String emailAddress, String state) throws IOException {
		GoogleAuthorizationCodeRequestUrl urlBuilder =
				getFlow().newAuthorizationUrl().setRedirectUri(REDIRECT_URI).setState(state);
		urlBuilder.set("user_id", emailAddress);
		return urlBuilder.build();
	}

	/**
	 * Retrieve credentials using the provided authorization code.
	 *
	 * This function exchanges the authorization code for an access token and
	 * queries the UserInfo API to retrieve the user's e-mail address. If a
	 * refresh token has been retrieved along with an access token, it is stored
	 * in the application database using the user's e-mail address as key. If no
	 * refresh token has been retrieved, the function checks in the application
	 * database for one and returns it if found or throws a NoRefreshTokenException
	 * with the authorization URL to redirect the user to.
	 *
	 * @param authorizationCode Authorization code to use to retrieve an access
	 *        token.
	 * @param state State to set to the authorization URL in case of error.
	 * @return OAuth 2.0 credentials instance containing an access and refresh
	 *         token.
	 * @throws NoRefreshTokenException No refresh token could be retrieved from
	 *         the available sources.
	 * @throws IOException Unable to load client_secrets.json.
	 */
	private static Credential getCredentials(String authorizationCode, String state)
			throws CodeExchangeException, NoRefreshTokenException, IOException {
		String emailAddress = "";
		try {
			Credential credentials = exchangeCode(authorizationCode);
			Userinfoplus userInfo = getUserInfo(credentials);
			String userId = userInfo.getId();
			emailAddress = userInfo.getEmail();
			if (credentials.getRefreshToken() != null) {
				storeCredentials(userId, credentials);
				return credentials;
			} else {
				credentials = getStoredCredentials(userId);
				if (credentials != null && credentials.getRefreshToken() != null) {
					return credentials;
				}
			}
		} catch (CodeExchangeException e) {
			e.printStackTrace();
			// Drive apps should try to retrieve the user and credentials for the current
			// session.
			// If none is available, redirect the user to the authorization URL.
			e.setAuthorizationUrl(getAuthorizationUrl(emailAddress, state));
			throw e;
		} catch (NoUserIdException e) {
			e.printStackTrace();
		}
		// No refresh token has been retrieved.
		String authorizationUrl = getAuthorizationUrl(emailAddress, state);
		throw new NoRefreshTokenException(authorizationUrl);
	}

	/**
	 * Build a Drive service object.
	 *
	 * @param credentials OAuth 2.0 credentials.
	 * @return Drive service object.
	 */
	private static Drive buildService(Credential credentials) {
		HttpTransport httpTransport = new NetHttpTransport();
		JacksonFactory jsonFactory = new JacksonFactory();

		return new Drive.Builder(httpTransport, jsonFactory, credentials)
		.build();
	}

	/**
	 * Print a file's metadata.
	 *
	 * @param service Drive API service instance.
	 * @param fileId ID of the file to print metadata for.
	 */
	static void printFile(Drive service, String fileId) {
		try {
			com.google.api.services.drive.model.File file = service.files().get(fileId).execute();

			System.out.println("Title: " + file.getTitle());
			System.out.println("Description: " + file.getDescription());
			System.out.println("MIME type: " + file.getMimeType());
		} catch (HttpResponseException e) {
			if (e.getStatusCode() == 401) {
				// Credentials have been revoked.
				// TODO: Redirect the user to the authorization URL.
				throw new UnsupportedOperationException();
			}
		} catch (IOException e) {
			System.out.println("An error occurred: " + e);
		}
	}

	public void initDrive(String code, String redirectURL){
		Credential credentials;
		try {
			credentials = getCredentials(code, redirectURL);
			drive = buildService(credentials);
			System.out.println("Drive is now enabled");
		} catch (CodeExchangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoRefreshTokenException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void printFiles()throws IOException{
		List<com.google.api.services.drive.model.File> result = new ArrayList<com.google.api.services.drive.model.File>();
		Files.List request = drive.files().list();
		List<File> res = new ArrayList<File>();
		do{
			try {
				FileList files = request.execute();
				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}while( request.getPageToken() != null &&
				request.getPageToken().length()> 0);
		for(com.google.api.services.drive.model.File f: result){
			System.out.println("file " + f.getTitle() + " has id " + f.getId() + " link: " + f.getWebContentLink());
		}
		
	}
	
	
	public ArrayList<FileDownloadLink> getFileDownloadLinks()throws IOException{
		List<com.google.api.services.drive.model.File> result = new ArrayList<com.google.api.services.drive.model.File>();
		ArrayList<FileDownloadLink> links = new ArrayList<FileDownloadLink>();
		Files.List request = drive.files().list();
		List<File> res = new ArrayList<File>();
		do{
			try {
				FileList files = request.execute();
				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			}
		}while( request.getPageToken() != null &&
				request.getPageToken().length()> 0);
		for(com.google.api.services.drive.model.File f: result){
			links.add(new FileDownloadLink(f.getTitle(), f.getWebContentLink()));
		}
		
		return links;
		
	}

		

	public void send(com.google.api.services.drive.model.File body, FileContent mediaContent) throws IOException{
		drive.files().insert(body, mediaContent).execute();
	}
	
	/**
	   * Download a file's content.
	   * 
	   * @param service Drive API service instance.
	   * @param file Drive File instance.
	   * @return InputStream containing the file's content if successful,
	   *         {@code null} otherwise.
	   */
	  public InputStream downloadFile(com.google.api.services.drive.model.File file) {
	    if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
	      try {
	        HttpResponse resp =
	            drive.getRequestFactory().buildGetRequest(new GenericUrl(file.getDownloadUrl()))
	                .execute();
	        return resp.getContent();
	      } catch (IOException e) {
	        // An error occurred.
	        e.printStackTrace();
	        return null;
	      }
	    } else {
	      // The file doesn't have any content stored on Drive.
	      return null;
	    }
	  }
	  
	  /**taken from http://stackoverflow.com/questions/18150073/how-to-create-folders-in-google-drive-without-duplicate
	     * 
	     * @param service google drive instance
	     * @param title the title (name) of the folder (the one you search for)
	     * @param parentId the parent Id of this folder (use root) if the folder is in the main directory of google drive
	     * @return google drive file object 
	     * @throws IOException
	     */
	    private com.google.api.services.drive.model.File getExistsFolder(String title,String parentId) throws IOException 
	    {
	        Drive.Files.List request;
	        request = drive.files().list();
	        String query = "mimeType='application/vnd.google-apps.folder' AND trashed=false AND title='" + title + "' AND '" + parentId + "' in parents";
	        System.out.println("isFolderExists(): Query= " + query);
	        request = request.setQ(query);
	        FileList files = request.execute();
	        System.out.println("isFolderExists(): List Size =" + files.getItems().size());
	        if (files.getItems().size() == 0) //if the size is zero, then the folder doesn't exist
	            return null;
	        else
	            //since google drive allows to have multiple folders with the same title (name)
	            //we select the first file in the list to return
	            return files.getItems().get(0);
	    }
	  
	  /**taken from http://stackoverflow.com/questions/18150073/how-to-create-folders-in-google-drive-without-duplicate
	   * 
	   * @param service google drive instance
	   * @param title the folder's title
	   * @param listParentReference the list of parents references where you want the folder to be created, 
	   * if you have more than one parent references, then a folder will be created in each one of them  
	   * @return google drive file object   
	   * @throws IOException
	   */
	  private com.google.api.services.drive.model.File createFolder(String title,List<ParentReference> listParentReference) throws IOException
	  {
		  com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
	      body.setTitle(title);
	      body.setParents(listParentReference);
	      body.setMimeType("application/vnd.google-apps.folder");
	      com.google.api.services.drive.model.File file = drive.files().insert(body).execute(); 
	      return file;

	  }
	  
	  /**taken from http://stackoverflow.com/questions/18150073/how-to-create-folders-in-google-drive-without-duplicate
	   * 
	   * @param service google drive instance
	   * @param titles list of folders titles 
	   * i.e. if your path like this folder1/folder2/folder3 then pass them in this order createFoldersPath(service, folder1, folder2, folder3)
	   * @return parent reference of the last added folder in case you want to use it to create a file inside this folder.
	   * @throws IOException
	   */
	  private List<ParentReference> createFoldersPath(String...titles) throws IOException
	  {
	      List<ParentReference> listParentReference = new ArrayList<ParentReference>();
	      com.google.api.services.drive.model.File file = null;
	      for(int i=0;i<titles.length;i++)
	      {
	          file = getExistsFolder(titles[i], (file==null)?"root":file.getId());
	          if (file == null)
	          {
	              file = createFolder(titles[i], listParentReference);
	          }
	          listParentReference.clear();
	          listParentReference.add(new ParentReference().setId(file.getId()));
	      }
	      return listParentReference;
	  }
}