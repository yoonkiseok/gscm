package com.tkg.gscm.sample.controller;

import com.tkg.gscm.message.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/message/work1")
    public ResponseEntity<String> getWork1Message() {
        String message = messageService.getMessage("work1.message");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/message/work2")
    public ResponseEntity<String> getWork2Message() {
        String message = messageService.getMessage("work2.message");
        return ResponseEntity.ok(message);
    }

    @GetMapping("/message/default")
    public ResponseEntity<String> getDefaultMessage() {
        String message = messageService.getMessage("default.message");
        return ResponseEntity.ok(message);
    }
}
