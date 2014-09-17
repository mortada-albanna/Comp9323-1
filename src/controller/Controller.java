package controller;


import interfaces.UserManagement;

import java.io.IOException;

import apis.Stormpath;

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
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

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
public class Controller extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(Controller.class.getName());

	private UserManagement stormpath;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Controller() {
		super();
		stormpath = new Stormpath();

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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String forwardPage = "";
		String action = request.getParameter("action");
		HttpSession session = request.getSession(true); 

		if(action==null){
			logger.info("invalid action");
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

		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/"+forwardPage);
		dispatcher.forward(request, response);
	}

	private String setPassword(HttpServletRequest request,
			HttpServletResponse response, HttpSession session) {
		// TODO Auto-generated method stub
		if (stormpath.setPassword(request.getParameter("username"), request.getParameter("password"))){
			request.setAttribute("message", "successfully changed password");
		}else{
			request.setAttribute("message", "could not find matching account");
		}
		return "setpassword.jsp";
	}

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
		// TODO Auto-generated method stub
		String forwardPage = null;
		if (stormpath.authenticateAccount(request.getParameter("username"), request.getParameter("password"))){
			forwardPage = "WEB-INF/welcome.jsp";
		}else{
			forwardPage = "login.jsp";
			request.setAttribute("message", "login attempt failed.");
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
