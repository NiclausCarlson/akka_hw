package com.example.akka_hw.controller;

import com.example.akka_hw.actors.Supervisor;
import com.example.akka_hw.stub_server.Response;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {
    private List<Response> responses = new ArrayList<>();

    @RequestMapping(value = "/get-result", method = RequestMethod.POST)
    public String getResult(@RequestParam(name = "request") String request) {
        Supervisor supervisor = new Supervisor(request, 0, 0, 0);
        responses = supervisor.get();
        return "redirect:/main";
    }

    @GetMapping("/main")
    public String get(Model model) {
        model.addAttribute("aggregation", responses);
        model.addAttribute("result", new Response());
        return "index";
    }

    @GetMapping("/")
    public String getMapping() {
        return "redirect:/main";
    }
}
