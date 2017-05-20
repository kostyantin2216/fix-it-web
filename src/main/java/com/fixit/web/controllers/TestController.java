package com.fixit.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class TestController {

	@RequestMapping("/test")
	public ModelAndView getTest(ModelAndView mv) {
		mv.addObject("firstVar", 27101989);
		mv.setViewName("test");
		return mv;
	}
	
}
