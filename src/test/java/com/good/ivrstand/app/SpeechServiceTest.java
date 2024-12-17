package com.good.ivrstand.app;

import com.good.ivrstand.app.service.SpeechService;
import com.good.ivrstand.app.service.externinterfaces.FlaskApiTtsService;
import com.good.ivrstand.app.service.externinterfaces.S3Service;
import com.good.ivrstand.exception.FileDuplicateException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SpeechServiceTest {

    @Mock
    private S3Service s3Service;

    @Mock
    private FlaskApiTtsService flaskApiTtsService;

    @InjectMocks
    private SpeechService speechService;

    @Test
    void testSplitDescription() {
        String inputText = "{\"description\":\\n\\nThis is line 1\\n\\n\\icon1This is line 2\"}";
        String[] expectedOutput = {"This is line 1", "This is line 2"};

        String[] result = speechService.splitDescription(inputText);

        assertArrayEquals(expectedOutput, result);
    }

    @Test
    void testGenerateAudio_Success() throws IOException, FileDuplicateException {
        String text = "Hello, world!";
        byte[] audioBytes = {1, 2, 3};
        MockMultipartFile mockFile = new MockMultipartFile("file", "audio.wav", "audio/wav", audioBytes);
        String expectedUrl = "https://s3.example.com/audio/audio.wav";

        when(flaskApiTtsService.generateSpeech(text)).thenReturn(audioBytes);
        when(s3Service.uploadFile(any(MockMultipartFile.class), eq("audio"))).thenReturn(expectedUrl);

        String resultUrl = speechService.generateAudio(text);

        assertEquals(expectedUrl, resultUrl);
    }

    @Test
    void testGenerateAudioEmptyText() throws IOException, FileDuplicateException {
        String text = "";

        String result = speechService.generateAudio(text);

        assertEquals("", result);
        verifyNoInteractions(flaskApiTtsService, s3Service);
    }
}
