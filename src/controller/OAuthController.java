package controller;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import apis.GoogleDrive;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.GenericUrl;

/**
 * Servlet implementation class Controller
 */
@WebServlet("/OAuthController")
public class OAuthController extends AbstractAuthorizationCodeServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OAuthController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

	@Override
	protected String getRedirectUri(HttpServletRequest req)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		GenericUrl url = new GenericUrl(req.getRequestURL().toString());
	    url.setRawPath("/COMP9323/controller");
	    return url.build();
	}

	@Override
	protected String getUserId(HttpServletRequest arg0)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException,
			IOException {
		// TODO Auto-generated method stub
		return GoogleDrive.getFlow();
	}

	
}