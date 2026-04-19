package com.yashfi.job.job_apps.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yashfi.job.job_apps.model.AIAnalysis;
import com.yashfi.job.job_apps.model.Application;
import com.yashfi.job.job_apps.model.ChatConversation;
import com.yashfi.job.job_apps.model.Job;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.repository.AIAnalysisRepository;
import com.yashfi.job.job_apps.repository.ApplicationRepository;
import com.yashfi.job.job_apps.repository.ChatConversationRepository;
import com.yashfi.job.job_apps.repository.JobRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class AIService {

    @Autowired
    private AIAnalysisRepository aiAnalysisRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    private UserService userService;

    @Value("${cloudflare.account.id}")
    private String cloudflareAccountId;

    @Value("${cloudflare.api.token}")
    private String cloudflareApiToken;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Extract text from PDF resume
     */
    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Analyze resume using Cloudflare AI
     */
    public Map<String, Object> analyzeResume(MultipartFile resumeFile, String userEmail) throws IOException {
        String resumeText = extractTextFromPDF(resumeFile);

        String prompt = "You are an expert resume reviewer. Analyze this resume and provide:\n" +
                "1. KEY STRENGTHS (list 3-5 specific strengths)\n" +
                "2. AREAS FOR IMPROVEMENT (list 3-5 specific improvements)\n" +
                "3. OVERALL SCORE (give a score from 0-100)\n" +
                "4. RECOMMENDATIONS (provide 3-5 actionable suggestions)\n\n" +
                "Format your response clearly with these exact section headers.\n\n" +
                "RESUME TEXT:\n" + resumeText.substring(0, Math.min(3000, resumeText.length()));

        String aiResponse = null;
        try {
            aiResponse = callCloudflareAI(prompt);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Map<String, Object> analysis = new HashMap<>();
        analysis.put("resumeText", resumeText.substring(0, Math.min(500, resumeText.length())) + "...");
        analysis.put("analysis", aiResponse);
        analysis.put("score", extractScoreFromResponse(aiResponse));
        analysis.put("timestamp", new Date());

        return analysis;
    }

    /**
     * Calculate job match score
     */
    public Map<String, Object> calculateJobMatch(Long applicationId, String userEmail) {
        User user = userService.getCurrentUser(userEmail);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!application.getApplicant().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        Job job = application.getJob();

        String prompt = "You are an expert job matching AI. Analyze the match between this candidate and job:\n\n" +
                "JOB TITLE: " + job.getTitle() + "\n" +
                "JOB REQUIREMENTS: " + job.getRequirements() + "\n" +
                "JOB DESCRIPTION: " + job.getDescription() + "\n\n" +
                "CANDIDATE COVER LETTER: " + (application.getCoverLetter() != null ? application.getCoverLetter() : "Not provided") + "\n\n" +
                "Provide:\n" +
                "1. MATCH SCORE (0-100)\n" +
                "2. MATCHING SKILLS (list skills that match)\n" +
                "3. MISSING SKILLS (list skills the candidate lacks)\n" +
                "4. RECOMMENDATIONS (how to improve the match)\n\n" +
                "Format your response with these section headers.";

        try {
            String aiResponse = callCloudflareAI(prompt);
            int score = extractScoreFromResponse(aiResponse);

            AIAnalysis analysis = new AIAnalysis();
            analysis.setApplication(application);
            analysis.setAnalysisType(AIAnalysis.AnalysisType.JOB_MATCH);
            analysis.setAnalysisResult(aiResponse);
            analysis.setScore(score);
            aiAnalysisRepository.save(analysis);

            application.setMatchScore(score);
            applicationRepository.save(application);

            Map<String, Object> result = new HashMap<>();
            result.put("matchScore", score);
            result.put("analysis", aiResponse);
            result.put("applicationId", applicationId);

            return result;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to calculate job match: " + e.getMessage());
        }
    }

    /**
     * Interview practice chatbot
     */
    public Map<String, Object> chat(String message, Long jobId, String userEmail) {
        User user = userService.getCurrentUser(userEmail);

        ChatConversation conversation;
        String jobContext = "";

        if (jobId != null) {
            Job job = jobRepository.findById(jobId)
                    .orElseThrow(() -> new RuntimeException("Job not found"));

            jobContext = "\nYou are helping the candidate prepare for this specific job:\n" +
                    "Job Title: " + job.getTitle() + "\n" +
                    "Requirements: " + job.getRequirements() + "\n\n";

            List<ChatConversation> existing = chatConversationRepository.findByUserIdAndJobId(user.getId(), jobId);
            if (!existing.isEmpty()) {
                conversation = existing.get(0);
            } else {
                conversation = new ChatConversation();
                conversation.setUser(user);
                conversation.setJob(job);
                conversation.setMessages("[]");
            }
        } else {
            List<ChatConversation> existing = chatConversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());
            if (!existing.isEmpty() && existing.get(0).getJob() == null) {
                conversation = existing.get(0);
            } else {
                conversation = new ChatConversation();
                conversation.setUser(user);
                conversation.setMessages("[]");
            }
        }

        List<Map<String, String>> messages;
        try {
            messages = objectMapper.readValue(conversation.getMessages(), List.class);
        } catch (Exception e) {
            messages = new ArrayList<>();
        }

        StringBuilder conversationContext = new StringBuilder();
        conversationContext.append("You are an experienced career coach and interview expert. ");
        conversationContext.append("Help the candidate prepare for job interviews by:\n");
        conversationContext.append("- Asking relevant interview questions\n");
        conversationContext.append("- Providing constructive feedback on their answers\n");
        conversationContext.append("- Giving practical tips and examples\n");
        conversationContext.append(jobContext);
        conversationContext.append("\nConversation so far:\n");

        for (Map<String, String> msg : messages) {
            conversationContext.append(msg.get("role")).append(": ").append(msg.get("content")).append("\n");
        }

        conversationContext.append("user: ").append(message).append("\n");
        conversationContext.append("assistant:");

        String aiResponse;
        try {
            aiResponse = callCloudflareAI(conversationContext.toString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Chat failed: " + e.getMessage());
        }

        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);

        Map<String, String> aiMessage = new HashMap<>();
        aiMessage.put("role", "assistant");
        aiMessage.put("content", aiResponse);
        messages.add(aiMessage);

        if (messages.size() > 20) {
            messages = messages.subList(messages.size() - 20, messages.size());
        }

        try {
            conversation.setMessages(objectMapper.writeValueAsString(messages));
            chatConversationRepository.save(conversation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save conversation");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("message", aiResponse);
        result.put("conversationId", conversation.getId());

        return result;
    }

    /**
     * Get chat history
     */
    public List<Map<String, Object>> getChatHistory(String userEmail) {
        User user = userService.getCurrentUser(userEmail);
        List<ChatConversation> conversations = chatConversationRepository.findByUserIdOrderByUpdatedAtDesc(user.getId());

        List<Map<String, Object>> history = new ArrayList<>();
        for (ChatConversation conv : conversations) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", conv.getId());
            item.put("jobId", conv.getJob() != null ? conv.getJob().getId() : null);
            item.put("jobTitle", conv.getJob() != null ? conv.getJob().getTitle() : "General Interview Prep");
            item.put("createdAt", conv.getCreatedAt());
            item.put("updatedAt", conv.getUpdatedAt());

            try {
                List<Map<String, String>> messages = objectMapper.readValue(conv.getMessages(), List.class);
                item.put("messageCount", messages.size());
                if (!messages.isEmpty()) {
                    item.put("lastMessage", messages.get(messages.size() - 1).get("content"));
                }
            } catch (Exception e) {
                item.put("messageCount", 0);
            }

            history.add(item);
        }

        return history;
    }

    /**
     * Get analysis for application
     */
    public Map<String, Object> getAnalysis(Long applicationId, String userEmail) {
        User user = userService.getCurrentUser(userEmail);
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        boolean isApplicant = application.getApplicant().getId().equals(user.getId());
        boolean isCompany = application.getJob().getCompany().getId().equals(user.getId());

        if (!isApplicant && !isCompany) {
            throw new RuntimeException("Unauthorized access");
        }

        List<AIAnalysis> analyses = aiAnalysisRepository.findByApplicationId(applicationId);

        Map<String, Object> result = new HashMap<>();
        result.put("applicationId", applicationId);
        result.put("analyses", analyses);

        return result;
    }

    // ========== HELPER METHODS ==========

    /**
     * Call Cloudflare Workers AI using Java HttpClient
     */
    private String callCloudflareAI(String prompt) throws IOException, InterruptedException {
        String url = String.format(
                "https://api.cloudflare.com/client/v4/accounts/%s/ai/run/@cf/meta/llama-3.1-8b-instruct",
                cloudflareAccountId
        );

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", 1024);

        String jsonBody = objectMapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + cloudflareApiToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Cloudflare AI request failed: " + response.statusCode() + " - " + response.body());
        }

        JsonNode jsonNode = objectMapper.readTree(response.body());

        if (jsonNode.has("result") && jsonNode.get("result").has("response")) {
            return jsonNode.get("result").get("response").asText();
        } else {
            throw new IOException("Unexpected response format from Cloudflare AI: " + response.body());
        }
    }

    /**
     * Extract score from AI response
     */
    private int extractScoreFromResponse(String response) {
        String[] patterns = {
                "score[:\\s]+(\\d+)",
                "(\\d+)/100",
                "(\\d+)%",
                "match.*?(\\d+)",
                "rating[:\\s]+(\\d+)"
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.CASE_INSENSITIVE);
            java.util.regex.Matcher m = p.matcher(response);
            if (m.find()) {
                try {
                    int score = Integer.parseInt(m.group(1));
                    if (score >= 0 && score <= 100) {
                        return score;
                    }
                } catch (Exception e) {
                    // Continue to next pattern
                }
            }
        }

        return 75;
    }
}