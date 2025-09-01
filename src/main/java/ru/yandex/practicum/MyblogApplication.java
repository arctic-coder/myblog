package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class MyblogApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyblogApplication.class, args);
	}

	@Bean //без него картинки не грузятся
	public org.springframework.web.multipart.MultipartResolver multipartResolver() {
		return new org.springframework.web.multipart.support.StandardServletMultipartResolver();
	}

}
