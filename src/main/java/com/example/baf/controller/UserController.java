package com.example.baf.controller;

import com.example.baf.model.User;
import com.example.baf.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView userList() {
        Iterable<User> users = userService.findAll();
        return new ModelAndView("tl/list", "users", users);
    }
}
