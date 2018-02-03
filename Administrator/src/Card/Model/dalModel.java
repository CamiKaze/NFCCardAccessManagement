package Card.Model;

import org.javalite.http.Get;
import org.javalite.http.Http;
import org.javalite.http.Post;

import javafx.scene.image.ImageView;

public class dalModel {
	//ToDo: We want methods detailing the various commands of a Rest API. e.g. Post, Get, Patch, etc.
	public void Post(String Name, String Surname, String CellNum,
			String Amount, String VIP, String AddedBy, String IPAddr, ImageView ProPic){
		System.out.println(Name + " " + Surname + " " + CellNum + " " + Amount + " " + VIP);

		
		Post post = Http.post("https://api.letmedev.com/club/merchandise").header("Accept", "application/json")
				.header("Content-Type", "application/json")
				.header("X-API-KEY", "asd123")
                .params("item_name", "John", "item_type", "Doe");
				System.out.println(post.text());
	}
	
	public void Get(){
		Get get = Http.get("https://api.letmedev.com/club/merchandise").header("Accept", "application/json")
				.header("Content-Type", "application/json")
    	 		.header("X-API-KEY", "asd123");
		
		System.out.println(get.text());
		System.out.println(get.headers());
		System.out.println(get.responseCode());
	}
}