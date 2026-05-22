package com.oussama.sovereignty.application.ports.out;

import com.oussama.sovereignty.application.common.StreamCallback;
import java.util.List;

public interface AiAgentPort {
    String askQuestion(String userPrompt, List<String> context);
    void streamAnswer(String userPrompt, List<String> context, StreamCallback callback);
}
