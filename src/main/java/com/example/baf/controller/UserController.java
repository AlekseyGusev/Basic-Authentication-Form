package com.example.baf.controller;

import com.example.baf.model.User;
import com.example.baf.service.IUserService;
import com.example.baf.validation.EmailExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

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

    @GetMapping("/{id}")
    public ModelAndView viewUser(@PathVariable("id") User user) {
        return new ModelAndView("tl/view", "user", user);
    }

    @PostMapping
    public ModelAndView createUser(@Valid User user, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return new ModelAndView("tl/form", "formErrors", result.getAllErrors());
        }

        try {
            user.setEnabled(true);

            if (user.getId() == null) {
                userService.registerNewUser(user);
                redirectAttributes.addFlashAttribute("globalMessage", "Successfully created a new user");
            } else {
                userService.updateExistingUser(user);
                redirectAttributes.addFlashAttribute("globalMessage", "Successfully updated the user");
            }

        } catch (EmailExistsException e) {
            result.addError(new FieldError("user", "email", e.getMessage()));
            return new ModelAndView("tl/form", "user", user);
        }
        return new ModelAndView("redirect:/user/{user.id}", "user.id", user.getId());
    }

    @GetMapping("delete/{id}")
    public ModelAndView delete(@PathVariable("id") Long id) {
        userService.delete(id);
        return new ModelAndView("redirect:/");
    }

    @GetMapping("/modify/{id}")
    public ModelAndView modifyForm(@PathVariable("id") User user) {
        return new ModelAndView("tl/form", "user", user);
    }

    @GetMapping(params = "form")
    public String createForm(@ModelAttribute User user) {
        return "tl/form";
    }

}
