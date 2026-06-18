package com.role.net.gogather.controller;

import com.role.net.gogather.dto.chat.AiMentionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final ApplicationEventPublisher eventPublisher;

    public TestController(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @GetMapping
    public String test() {
        return "Hello, world!";
    }

    //🔴 NOVA ROTA DE TESTE DA IA
    @PostMapping("/ai-trigger")
    public ResponseEntity<String> triggerAi(
            @RequestParam Long groupId,
            @RequestParam String message) {

        // Simula o que o usuario faria no chat: cria o evento e manda pro AiService escutar
        eventPublisher.publishEvent(new AiMentionEvent(groupId, message));

        return ResponseEntity.ok("Evento disparado. A IA está processando a requisição, verifique o banco de dados."); //Aqui no db faz o SELECT sender_id, type, content FROM chat_messages;
    }
}
