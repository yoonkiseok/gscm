package com.tkg.gscm.sample.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class BasicController {
    @GetMapping("/basic")
    public ResponseEntity<Object> hello(Model model) throws Exception{
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("key", "Hello");

        return ResponseEntity.status(HttpStatus.OK).body(hashMap);
    }
}