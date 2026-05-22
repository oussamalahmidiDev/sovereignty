package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.application.common.StreamCallback;

public interface AskQuestionUseCase {
    String ask(String question);
    void streamAnswer(String question, StreamCallback callback);
}
