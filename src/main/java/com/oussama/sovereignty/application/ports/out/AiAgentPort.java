package com.oussama.sovereignty.application.ports.out;

import java.util.List;

public interface AiAgentPort {
    String askQuestion(String userPrompt, List<String> context);
}
