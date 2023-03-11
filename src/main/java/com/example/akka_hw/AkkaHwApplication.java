package com.example.akka_hw;

import com.example.akka_hw.stub_server.ResponseGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class AkkaHwApplication {
    private static void initGenerator(){
        ResponseGenerator init = new ResponseGenerator(Collections.emptyList());
    }
    public static void main(String[] args) {
        initGenerator();
        SpringApplication.run(AkkaHwApplication.class, args);
    }

}
