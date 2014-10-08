package apis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;

/** A Class to access the google drive api. Some code snippets were taken from code samples provided by Google
 * 
 * @author Ervin
 *
 */
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
	/**
	 * Scopes that OAuth needs to ask permissions for
	 */
	private static final List<String> SCOPES = Arrays.asList(
			"https://www.googleapis.com/auth/drive.file",
			"https://www.googleapis.com/auth/userinfo.email",
			"https://www.googleapis.com/auth/userinfo.profile");
	/**
	 * Drive instance
	 */
	private Drive drive = null;

	public GoogleDrive(){

	}

	/**
	 * Google authorization flow
	 */
	private static GoogleAuthorizationCodeFlow flow = null;

	/**
	 * Exception thrown when an error occurred while retrieving credentials.
	 */
	public static class GetCredentialsException extends Exception {
		static final long serialVersionUID = 42L;
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
		static final long serialVersionUID = 42L;
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
		static final long serialVersionUID = 42L;
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
		static final long serialVersionUID = 42L;
	}

	/**
	 * Retrieved stored credentials for the provided user ID.
	 *
	 * @param userId User's ID.
	 * @return Stored Credential if found, {@code null} otherwise.
	 */
	static Credential getStoredCredentials(String userId) {
		// Credential instance with stored accessToken and refreshToken.
		Credential cred = new GoogleCredential();
		File file = new File ("credentials.txt");
		try {
			cred.setAccessToken(readRow(file));
			cred.setRefreshToken(readRow(file));
		} catch (IOException e) {
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
		stream.close();

		return row;
	}

	/**
	 * Store OAuth 2.0 credentials in the application's database.
	 *
	 * @param userId User's ID.
	 * @param credentials The OAuth 2.0 credentials to store.
	 */
	static void storeCredentials(String userId, Credential credentials) {
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Build an authorization flow and store it as a static class attribute.
	 * Requires the client secret path to be stored in constants class
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
					int value = -1;
					java.io.File file = new File(Constants.CLIENT_SECRET_PATH); 
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
					flow.newTokenRequest(authorizationCode).setRedirectUri(Constants.REDIRECT_URI).execute();
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
				getFlow().newAuthorizationUrl().setRedirectUri(Constants.REDIRECT_URI).setState(state);
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

	/** Connects to the drive once the Oauth code is given
	 * 
	 * @param code The code that is returned after logging in Google Drive
	 */
	public void initDrive(String code){
		Credential credentials;
		try {
			credentials = getCredentials(code, Constants.REDIRECT_URI);
			drive = buildService(credentials);
			System.out.println("Drive is now enabled");
		} catch (CodeExchangeException e) {
			e.printStackTrace();
		} catch (NoRefreshTokenException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	/** Returns an arraylist of all the download links in a drive
	 * 
	 * @return A List of FileDownloadLinks
	 * @throws IOException
	 */
	public ArrayList<FileDownloadLink> getFileDownloadLinks()throws IOException{
		List<com.google.api.services.drive.model.File> result = new ArrayList<com.google.api.services.drive.model.File>();
		ArrayList<FileDownloadLink> links = new ArrayList<FileDownloadLink>();
		Files.List request = drive.files().list();
		do{
			try {
				FileList files = request.execute();
				result.addAll(files.getItems());
				request.setPageToken(files.getNextPageToken());
			} catch (IOException e) {
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
}