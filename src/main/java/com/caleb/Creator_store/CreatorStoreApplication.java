package com.caleb.Creator_store;


//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CreatorStoreApplication {

	
	public static void main(String[] args) {
	/* 	Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach((entry) -> System.setProperty(entry.getKey(), entry.getValue()));
		System.out.println("Database URL: " + System.getProperty("DATABASE_URL"));
System.out.println("Database username: " + System.getProperty("DATABASE_USERNAME"));
System.out.println("Password loaded: " +
        (System.getProperty("DATABASE_PASSWORD") != null));*/
	
		SpringApplication.run(CreatorStoreApplication.class, args);
	}

}
