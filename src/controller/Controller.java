package controller;


import interfaces.UserManagement;

import java.io.File;
import java.io.IOException;

import apis.GoogleDrive;
import apis.Stormpath;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.stormpath.sdk.client.*;
import com.stormpath.sdk.tenant.*;
import com.stormpath.sdk.application.*;
import com.stormpath.sdk.account.*;
import com.stormpath.sdk.directory.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;


import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.client.ApiKey;
import com.stormpath.sdk.client.ApiKeys;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.directory.CustomData;


/**
 * Servlet implementation class Controller
 */
@WebServlet("/controller")
@MultipartConfig(
		fileSizeThreshold=1024*1024*10, // 10MB
		maxFileSize=1024*1024*20,      // 20MB
		maxRequestSize=1024*1024*50)   // 50MB
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(Controller.class.getName());

	private UserManagement stormpath;
	private GoogleDrive googleDrive;
	private static final String SAVE_DIR = "uploads";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Controller() {
		super();
		stormpath = new Stormpath();
		googleDrive = new GoogleDrive();

		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println(request.getParameter("code"));
		// TODO not sure why it tries to access driveready here
		try{
			googleDrive.initDrive(request.getParameter("code"), "http://localhost:8080/COMP9323/controller");
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+"driveready.jsp");
			dispatcher.forward(request, response);
		}catch(NullPointerException e){
			// Default go to login page or welcome page if you are logged in
			HttpSession session = request.getSession();
			String user = (String)session.getAttribute("user");
			String forwardPage="";
			if( user == null){
				forwardPage = login(request, response, session);
			}else{
				forwardPage = "WEB-INF/welcome.jsp";
			}
			System.out.println("doGet: user=" + user + " " + new Date(session.getLastAccessedTime()));
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
			dispatcher.forward(request, response);

		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String forwardPage = "";
		String action = request.getParameter("action");
		HttpSession session = request.getSession(true);
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart){
			forwardPage = uploadFile(request, response, session);
		}else{
			
			if(action==null){
				logger.info("invalid action");

				System.out.println(request.getParameterNames());
			}else{
				if (action.equals("login")){
					forwardPage = login(request, response, session);
				}else if (action.equals("register")){
					forwardPage = register(request, response, session);
				}else if (action.equals("create_account")){
					forwardPage = createAccount(request, response, session);
				}else if (action.equals("get_details")){
					forwardPage = getDetails(request, response, session);
				}else if (action.equals("set_password")){
					forwardPage = setPassword(request, response, session);
				}
			}
		}


		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
		dispatcher.forward(request, response);
	}

	private String uploadFile(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		//String forwardPage = null;
		if( session.getAttribute("user") == null)
		{
			//forwardPage = "login.jsp";
			return "login.jsp";
		}
		else
		{
			
		// gets absolute path of the web application
		String appPath = request.getServletContext().getRealPath("");
		// constructs path of the directory to save uploaded file
		String savePath = appPath + File.separator + SAVE_DIR;

		// creates the save directory if it does not exists
		File fileSaveDir = new File(savePath);
		if (!fileSaveDir.exists()) {
			fileSaveDir.mkdir();
		}

		try {
			for (Part part : request.getParts()) {
				String fileName = extractFileName(part);
				if (fileName != null){
					System.out.println("Saving to " + savePath + File.separator + fileName);
					part.write(savePath + File.separator + fileName);
					System.out.println(savePath + File.separator + fileName);
					com.google.api.services.drive.model.File body = new com.google.api.services.drive.model.File();
					body.setTitle(extractFileName(part));
					body.setDescription("A test document");
					body.setMimeType(part.getContentType());

					java.io.File fileContent = new File(savePath + File.separator + fileName);;
					FileContent mediaContent = new FileContent("text/plain", fileContent);
					

					googleDrive.send(body, mediaContent);
					fileContent.delete();
				}


			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




		System.out.println("Done!");
		request.setAttribute("message", "Successfully Uploaded File");
		return "WEB-INF/welcome.jsp";
	}}

	/**
	 * Extracts file name from HTTP header content-disposition
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				String whole = s.substring(s.indexOf("=") + 2, s.length()-1);
				String[] split = whole.replace('\\', '/').split("/");
				return split[split.length-1];
			}
		}
		return null;
	}

	private String setPassword(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		if( session.getAttribute("user") == null){
			return "login.jsp";
		}
		else{
		if (stormpath.setPassword(request.getParameter("username"), request.getParameter("password"))){
			request.setAttribute("message", "successfully changed password");
		}else{
			request.setAttribute("message", "could not find matching account");
		}
		return "setpassword.jsp";
	}}

	private String getDetails(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		request.setAttribute("message", stormpath.getDetails(request.getParameter("username")));
		return "getdetails.jsp";
	}

	private String register(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		return "createaccount.jsp";
	}

	private String login(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		String forwardPage = null;
		if( session.getAttribute("user") == null){
			if (stormpath.authenticateAccount(request.getParameter("username"), request.getParameter("password"))){
				forwardPage = "index.jsp";
				session.setAttribute("user", request.getParameter("username"));
				session.setAttribute("group", stormpath.getAuthorizationGroup(request.getParameter("username")));
			}else{
				forwardPage = "login.jsp";
				request.setAttribute("message", "login attempt failed.");
			}
		}else{
			forwardPage ="WEB-INF/welcome.jsp";
			System.out.println("login: user = " + session.getAttribute("user"));
		}

		return forwardPage;
	}

	private String createAccount(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub

		String forwardpage = null;

		//Set the account properties
		String givenName = request.getParameter("given_name");
		String surname = request.getParameter("surname");
		String userName = request.getParameter("email"); //optional, defaults to email if unset
		String email = request.getParameter("email");
		String password = request.getParameter("password");
		String password2 = request.getParameter("password2");
		if (password.equals(password2)){
			stormpath.createAccount(givenName, surname, userName, password, email, "Student");
			request.setAttribute("message", "Registration Successful!");
			forwardpage = "login.jsp";
		}else{
			forwardpage = "createaccount.jsp";
		}


		//Create the account using the existing Application object


		return "login.jsp";
	}

}