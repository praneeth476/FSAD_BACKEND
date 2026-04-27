package com.workstudy.backend.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class MatchingEngine {

    public String parsePdfToText(String filePath) {
        if (filePath == null || filePath.isEmpty()) return "";
        File f = new File(filePath);
        if (!f.exists()) return "";
        try (PDDocument document = PDDocument.load(f)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document).toLowerCase();
        } catch (Exception e) {
            System.err.println("Failed to parse PDF: " + e.getMessage());
            return "";
        }
    }

    public MatchResult calculateMatch(String resumeText, String requiredSkills) {
        MatchResult result = new MatchResult();
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            result.score = 100;
            result.missingSkills = "";
            return result;
        }
        
        String[] skills = requiredSkills.split(",");
        int found = 0;
        List<String> missing = new ArrayList<>();
        
        String lowerResume = resumeText == null ? "" : resumeText.toLowerCase();

        for (String skill : skills) {
            String s = skill.trim().toLowerCase();
            if (s.isEmpty()) continue;
            
            if (lowerResume.contains(s)) {
                found++;
            } else {
                missing.add(skill.trim());
            }
        }
        
        int total = skills.length;
        if (total == 0) {
            result.score = 100;
            result.missingSkills = "";
            return result;
        }
        
        result.score = (int) Math.round((double) found / total * 100);
        result.missingSkills = String.join(", ", missing);
        return result;
    }

    public static class MatchResult {
        public int score;
        public String missingSkills;
    }
}
