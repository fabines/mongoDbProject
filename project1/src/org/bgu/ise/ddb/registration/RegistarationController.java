/**
 * 
 */
package org.bgu.ise.ddb.registration;



import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

import org.bgu.ise.ddb.ParentController;
import org.bgu.ise.ddb.User;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.MongoClient;
import static com.mongodb.client.model.Filters.*;
import java.util.ArrayList;


/**
 * @author Alex
 *
 */
@RestController
@RequestMapping(value = "/registration")
public class RegistarationController extends ParentController{
	
	

	private MongoClient mongoClient = null;
	
	public MongoDatabase connectToDB() {
		mongoClient = null;
		mongoClient = new MongoClient( "localhost" , 27017 );
		MongoDatabase db = mongoClient.getDatabase("project");
		return db;
	}
	
	

	/**
	 * The function checks if the username exist,
	 * in case of positive answer HttpStatus in HttpServletResponse should be set to HttpStatus.CONFLICT,
	 * else insert the user to the system  and set to HttpStatus in HttpServletResponse HttpStatus.OK
	 * @param username
	 * @param password
	 * @param firstName
	 * @param lastName
	 * @param response
	 */
	@RequestMapping(value = "register_new_customer", method={RequestMethod.POST})
	public void registerNewUser(@RequestParam("username") String username,
			@RequestParam("password")    String password,
			@RequestParam("firstName")   String firstName,
			@RequestParam("lastName")  String lastName,
			HttpServletResponse response) {
		System.out.println(username+" "+password+" "+lastName+" "+firstName);
		//:TODO your implementation
		try {
			if (isExistUser(username)) {
				HttpStatus status = HttpStatus.CONFLICT;
				response.setStatus(status.value());
			}else {
				MongoDatabase db = connectToDB();
				MongoCollection<Document>  dbCollection = db.getCollection("users");	
				Document user = new Document();
				user.put("UserName", username);
				user.put("Password", password);
				user.put("FirstName", firstName);
				user.put("LastName", lastName);
				user.put("date", new Date());
				dbCollection.insertOne(user);
				HttpStatus status = HttpStatus.OK;
				response.setStatus(status.value());
				mongoClient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			HttpStatus status = HttpStatus.CONFLICT;
			response.setStatus(status.value());
			mongoClient.close();
		} 
	}
	
	/**
	 * The function returns true if the received username exist in the system otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "is_exist_user", method={RequestMethod.GET})
	public boolean isExistUser(@RequestParam("username") String username) throws IOException{
		System.out.println(username);
		boolean result = false;
		//:TODO your implementation
		try {
		MongoDatabase db = connectToDB();
		MongoCollection<Document>  dbCollection = db.getCollection("users");
		Document user =dbCollection.find(eq("UserName", username)).first();
		if(!user.isEmpty())
			result=true;
		}catch(Exception e) {
			e.printStackTrace();
			mongoClient.close();
		}
		return result;
		
	}
	
	/**
	 * The function returns true if the received username and password match a system storage entry, otherwise false
	 * @param username
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "validate_user", method={RequestMethod.POST})
	public boolean validateUser(@RequestParam("username") String username,
			@RequestParam("password")    String password) throws IOException{
		System.out.println(username+" "+password);
		boolean result = false;
		//:TODO your implementation
		try {
			MongoDatabase db = connectToDB();
			MongoCollection<Document>  dbCollection = db.getCollection("users");
			Document user =dbCollection.find(and(eq("UserName", username),eq("Password",password))).first();
			if(user !=null)
				result=true;
			mongoClient.close();
			}catch(Exception e) {
				e.printStackTrace();
				mongoClient.close();
			}
		
		return result;
		
	}
	
	/**
	 * The function retrieves number of the registered users in the past n days
	 * @param days
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "get_number_of_registred_users", method={RequestMethod.GET})
	public int getNumberOfRegistredUsers(@RequestParam("days") int days) throws IOException{
		System.out.println(days+"");
		int result = 0;
		//:TODO your implementation
		MongoDatabase db = connectToDB();
		MongoCollection<Document>  dbCollection = db.getCollection("users");
		for (Document cur : 
			dbCollection.find(gte("date",new Date(new Date().getTime()-(days*24*60*60*1000))))) {
		    result++;
		}
		//dbCollection.find(gte("date",new Date(new Date().getTime()-(days*24*60*60*1000))));
		mongoClient.close();
		return result;
	}

	/**
	 * The function retrieves all the users
	 * @return
	 */
	@RequestMapping(value = "get_all_users",headers="Accept=*/*", method={RequestMethod.GET},produces="application/json")
	@ResponseBody
	@org.codehaus.jackson.map.annotate.JsonView(User.class)
	public  User[] getAllUsers(){
		//:TODO your implementation
		MongoDatabase db = connectToDB();
		MongoCollection<Document>  dbCollection = db.getCollection("users");
		ArrayList<User> users= new ArrayList<User>();
		for (Document cur : dbCollection.find()) {
		   String username=cur.getString("UserName");
		   String firstName=cur.getString("FirstName");
		   String lastName=cur.getString("LastName");
		   User user= new User(username,firstName,lastName);
		   users.add(user);
		}
		User [] usersList = new User[users.size()];
		usersList = users.toArray(usersList);
		mongoClient.close();
		return usersList;
	}

}
