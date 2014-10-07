package authorization;


import interfaces.UserManagement;

import java.io.File;
import java.io.IOException;

import other.Constants;

import com.stormpath.sdk.client.ApiKey;
import com.stormpath.sdk.client.ApiKeys;
import com.stormpath.sdk.client.AuthenticationScheme;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.client.Clients;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.sdk.group.Group;

import apis.Stormpath;
import controller.Controller;


class Authz {

	public boolean Authz(String username, String action) {
		String path = Constants.APIKEY_PATH;
		Client client = null;

		@SuppressWarnings("deprecation")
		ApiKey apiKey = ApiKeys.builder().setFileLocation(path).build();
		//this.client = Clients.builder().setApiKey(apiKey).build();
		client = Clients.builder().setApiKey(apiKey)
			    .setAuthenticationScheme(AuthenticationScheme.BASIC)
			    .build();


		String temp=null;
		Group group = client.instantiate(Group.class);
		CustomData customData = group.getCustomData();
		temp=(String) customData.get("permission");
		System.out.println(temp);
		return false;
	}

}
