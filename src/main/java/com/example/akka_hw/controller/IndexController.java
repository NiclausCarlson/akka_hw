package com.example.akka_hw.controller;

import com.example.akka_hw.actors.Supervisor;
import com.example.akka_hw.stub_server.Response;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class IndexController {
    @RequestMapping(value = "/get-result", method = RequestMethod.POST)
    public String getResult(@ModelAttribute("request") String request, Model model) {
        Supervisor supervisor = new Supervisor(request, 0, 0, 0);
        var result = supervisor.get();
        model.addAttribute("aggregation", result);
        model.addAttribute("result", new Response());

        return "index";
    }

}
