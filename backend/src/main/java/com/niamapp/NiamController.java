package com.niamapp;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NiamController {

    private final List<String> names = List.of("Niam", "Mani", "Anim", "Main", "Amin");

    @GetMapping("/names")
    public List<String> getNames() {
        return names;
    }
}
