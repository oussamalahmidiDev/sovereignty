package com.oussama.sovereignty.application.ports.in;

import com.oussama.sovereignty.application.common.StreamCallback;

import java.util.UUID;

public interface AskQuestionUseCase {
    Answer ask(UUID chatId, String question);
    UUID streamAnswer(UUID chatId, String question, StreamCallback callback);

    record Answer(UUID chatId, String response) {
    }
}
