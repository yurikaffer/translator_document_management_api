package com.bureauworks.translator_document_management.service;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.json.JSONObject;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
public class OpenAIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Pattern LANGUAGE_CODE_PATTERN = Pattern.compile("^[a-z]{2}-[a-z]{2}$");

    public String detectLanguage(String content) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a professional and objective language translator.");

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", "Detect the language of the following content and return me only your language " +
                "code and country code in format xx-xx: " + content);

        JSONArray messages = new JSONArray();
        messages.put(systemMessage);
        messages.put(userMessage);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 5);

        HttpEntity<String> entity = new HttpEntity<>(requestBody.toString(), headers);

        ResponseEntity<String> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, String.class);

        JSONObject responseJson = new JSONObject(response.getBody());
        String languageCode = responseJson.getJSONArray("choices").getJSONObject(0)
                .getJSONObject("message").getString("content").trim().toLowerCase();

        // Validate the format of the response
        Matcher matcher = LANGUAGE_CODE_PATTERN.matcher(languageCode);
        if (matcher.matches()) {
            return languageCode;
        } else {
            return "";
        }
    }
}