package com.anantakala.backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dummy")
@Slf4j
public class DummyController {

    @GetMapping()
    public String dummy() {
        log.info("=====Inside dummy() api=====");
        return "=====Inside dummy() api=====";
    }
}
