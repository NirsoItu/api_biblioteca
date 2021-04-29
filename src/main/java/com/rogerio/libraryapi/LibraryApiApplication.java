package com.rogerio.libraryapi;

import com.rogerio.libraryapi.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class LibraryApiApplication {

	/*

	@Autowired
	private EmailService emailService;


	@Bean
	public CommandLineRunner commandLineRunner(){

		return args -> {
			List<String> emails = Arrays.asList("6d57b91e62-fa95a4@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de emails" ,emails);
			System.out.println("Emails enviados com sucesso !");
		};
	}
	 */

	@Bean
	public ModelMapper ModelMapper(){
		return new ModelMapper();
	}

	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
