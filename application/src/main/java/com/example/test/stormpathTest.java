package com.example.test;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.stormpath.sdk.client.*;
import com.stormpath.sdk.tenant.*;
import com.stormpath.sdk.application.*;
import com.stormpath.sdk.account.*;
import com.stormpath.sdk.application.*;
import com.stormpath.sdk.directory.*;
import com.stormpath.sdk.group.Group;
import com.stormpath.sdk.group.GroupList;
import com.stormpath.sdk.group.GroupMembership;
import com.stormpath.sdk.group.GroupMembershipList;
import com.stormpath.sdk.group.Groups;



/**
 * Servlet implementation class stormpathTest
 */
public class stormpathTest extends HttpServlet {
	private static final long serialVersionUID = 1L;
	ApiKey apiKey = ApiKeys.builder().setFileLocation("c:/apiKey.properties").build();
	Client client = Clients.builder().setApiKey(apiKey).build();

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		PrintWriter out = response.getWriter();
		String applicationHref = "https://api.stormpath.com/v1/applications/3RgBXl2uHkMq9IxPudfBMV";
		Application application = client.getResource(applicationHref, Application.class);
		//String href = "https://api.stormpath.com/v1/accounts/7PfjDWdUoX5UlgTrAig2I3";	
		//Account account = client.getResource(href, Account.class);
		AccountCriteria criteria = Accounts.where(Accounts.givenName().eqIgnoreCase("john"));
		AccountList accounts = application.getAccounts(criteria);
		Account account=null;
		out.println("<html>");
		out.println("<head><title> Strompath Test </title></head>");
		out.println("<body>");
		
		
		for(Account acc : accounts) {
		    account = acc;
		    String fullname= account.getFullName();
			
			
			out.println(fullname + "  \n   ");
		    
		}
		
		CustomData customData = account.getCustomData();
		customData.put("favoriteColor", "white");
		customData.put("secretnumber", "12234-8403");
		
		out.println("<p>_________________________________________________________</p>");
		out.println(account.getCustomData().get("secretnumber"));
		out.println("<p>____________________________________________________________</p>");
		
		
		GroupList groups = application.getGroups();
		for( Group group : groups) {
			out.println('\n');
			out.println(group.getName());
			out.println("<p>____________________________________________________________</p>");
		    //System.out.println(group.getName());
			out.println(group.getAccounts());
		}
		out.println("<p>____________________________________________________________</p>");
		
		GroupMembershipList groupMemberships = account.getGroupMemberships();
		for(GroupMembership gms : groupMemberships) {
		    out.println(gms.getAccount().getFullName());
		    out.println(gms.getGroup().getName()+ " \n  ");
		    
		}
		
		out.println("<p>___________________________________________________________</p>");

		//out.println(account.getGroupMemberships());
		out.println("</body></html>");
		
		Group group1 = client.getResource("https://api.stormpath.com/v1/groups/51Ay6UgQiHtjC1z66ZnKo3", Group.class);
		CustomData Data = group1.getCustomData();
		//Data.put("permission1", "secret");
		//Data.save();
		
		out.println("<p>___________________________________________________________</p>");
		out.println(Data.get("permission1"));
		
		
		
		
	}

}
