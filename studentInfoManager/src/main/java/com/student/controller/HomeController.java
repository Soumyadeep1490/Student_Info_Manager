package com.student.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.student.dao.UserRepository;
import com.student.entities.User;
import com.student.messages.Message;

@Controller
public class HomeController {

	
	@Autowired
	private UserRepository userRepository;

	@RequestMapping("/")
	public String home(Model model) {
		
		model.addAttribute("title", "Home - Student Information Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		
		model.addAttribute("title", "About - Student Information Manager");
		return "about";
	}
	
	@RequestMapping("/register")
	public String register(Model model) {
		
		model.addAttribute("title", "Register - Student Information Manager");
		
		model.addAttribute("user", new User());
		
		return "register";
	}
	
	// Handler for Register user
	@RequestMapping(value="/do_register", method=RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
		
		try {
			
			if(result.hasErrors()) {
				
				System.out.println("ERROR" + result.toString());
				model.addAttribute("user", user);
				return "register";
			}
			
			// set user role NORMAL
			user.setRole("NORMAL");
			
			// print user at ide console
			System.out.println("USER" + user);
			
			// store user data in DB
			this.userRepository.save(user);
			
			// show blank fields in the form
			model.addAttribute("user", new User());
			
			// Show success message
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
			
			return "register";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			// show inputed data in the form
			model.addAttribute("user", user);
			
			// show error message
			session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));
			
			return "register";
		}
		
	}
	
	@GetMapping("/signin")
	public String loginPage(Model model) {
		
		model.addAttribute("title", "Login - Student Information Manager");
		
		model.addAttribute("user", new User());
		
		return "login";
	}
	
	// Handler for NORMAL and ADMIN page
	@PostMapping("/user")
	public String loginSubmit(@ModelAttribute User user, Model model, HttpSession session) {
		
		model.addAttribute("user", user);
		
		
		User u = userRepository.getUserByUserName(user.getUserName().toString());
		
		if(u != null && user.getPassword().equals(u.getPassword())) {
			
			if(u.getRole().equals("ADMIN")) {
				
				model.addAttribute("title", "Admin User - Student Information Manager");
				
				model.addAttribute("users", userRepository.findAll());
				model.addAttribute("currentUser", userRepository.getUserByUserName(user.getUserName()));
				
				return "admin";
				
			}else if(u.getRole().equals("NORMAL")) {
				
				model.addAttribute("title", "Normal User - Student Information Manager");
				
				model.addAttribute("currentUser", userRepository.getUserByUserName(user.getUserName()));
				
				return "normal";
			}
			
		}else {
			
			session.setAttribute("message", new Message("Wrong Credentials", "alert-danger"));
			return "login";
		}
		session.setAttribute("message", new Message("Wrong Credentials", "alert-danger"));
		return "login";

	}
	
	// delete user
	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable("id") int id, Model model, HttpSession session) {
		
	    User user = userRepository.findById(id)
	    						  .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
	    
	    
	    userRepository.delete(user);
	    session.setAttribute("message", new Message("Successfully Deleted. Login again to see the changes", "alert-success"));
	    
	    return "redirect:/signin";
	}
	
	// Edit user form
	@GetMapping("/edit/{id}")
	public ModelAndView editUserPage(@PathVariable("id") int id) {
		
		ModelAndView view = new ModelAndView("edit-user-form");
		
		User user = userRepository.findById(id).get();
		
		view.addObject("user", user);
		
		return view;
		
	}
	
	// Handler for Edit user
	@PostMapping("/do_edit")
	public String editUser(@Valid @ModelAttribute("user") User user, BindingResult result, Model model, HttpSession session) {
		
		try {
			
			if(result.hasErrors()) {
				
				System.out.println("ERROR" + result.toString());
				model.addAttribute("user", user);
				return "login";
			}
			
			this.userRepository.save(user);

			session.setAttribute("message", new Message("Successfully Updated. Login again to see the changes", "alert-success"));
			
			return "login";
			
		} catch (Exception e) {
			
			e.printStackTrace();

			session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));
			
			return "login";
		}
		
	}
	
	
}
