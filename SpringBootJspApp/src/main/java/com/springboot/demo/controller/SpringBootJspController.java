package com.springboot.demo.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
//@RequestMapping("/api")
public class SpringBootJspController {
	
	@Autowired
	private Environment env;
	
	@GetMapping("/public/health")
	public String getHealthApi() {
		return new String("Spring Boot Jsp Application is running");
	}
	
	@GetMapping("/test")
	public String test() {
		return "Hello";
	}
	
	@GetMapping("/display")
	public ModelAndView getmessageDisplay() {
		return new ModelAndView("display","message","Welcome to Spring Boot Jsp Application");
	}
	
	@GetMapping("/hello")
	public String sayhello(@RequestParam(name = "name", required = false, defaultValue = "World!") String name, Model model) {
		return "Hello "+name;
	}
	
	@Scheduled(cron = "${JOBSCHEDULER}",zone = "Asia/Kolkata")
	@Retryable(value = {Exception.class}, maxAttempts = 3,backoff = @Backoff(delay = 60000*30))
	public void executeJobScheduler() {
		if(env.getProperty("JOBFLAG").equalsIgnoreCase("YES"))
			System.out.println("welcome to job scheduler "+new Date());
	}
	

}
