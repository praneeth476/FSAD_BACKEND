package com.workstudy.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.workstudy.backend.model.Student;
import com.workstudy.backend.repository.StudentRepository;

import jakarta.transaction.Transactional;

import com.workstudy.backend.repository.ApplicationRepository;
import com.workstudy.backend.repository.WorkHourRepository;
import com.workstudy.backend.service.EmailService;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private WorkHourRepository workHourRepository;

    @Autowired
    private EmailService emailService;

    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public Student register(@RequestBody Student student) {

        if (studentRepository.findByEmailIgnoreCase(student.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists with this email address.");
        }

        student.setPassword(encoder.encode(student.getPassword()));

        if (student.getRole() == null) {
            student.setRole("student");
        }

        return studentRepository.save(student);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Student req) {

        Student s = studentRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not registered"
                ));

        if (!encoder.matches(req.getPassword(), s.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid password"
            );
        }

        // --- MFA IMPLEMENTATION: Instead of completing login, generate OTP ---
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        s.setMfaCode(otp);
        studentRepository.save(s);

        // Dispatch Email (will fallback to console printing if SMTP is unconfigured)
        emailService.sendMfaCode(s.getEmail(), otp);

        Map<String, Object> res = new HashMap<>();
        res.put("mfaRequired", true);
        res.put("email", s.getEmail());
        
        return ResponseEntity.ok(res);
    }
    
    @PostMapping("/verify-mfa")
    public ResponseEntity<Student> verifyMfa(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String code = req.get("code");
        
        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
                
        if (s.getMfaCode() != null && s.getMfaCode().equals(code)) {
            // Success: clear the code and return the final user token
            s.setMfaCode(null);
            studentRepository.save(s);
            return ResponseEntity.ok(s);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired MFA Code");
        }
    }

    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> sendForgotPasswordOtp(@RequestBody java.util.Map<String, String> req) {
        String email = req.get("email");
        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        s.setMfaCode(otp);
        studentRepository.save(s);

        emailService.sendMfaCode(s.getEmail(), otp);

        java.util.Map<String, Object> res = new java.util.HashMap<>();
        res.put("message", "OTP sent successfully to " + email);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody java.util.Map<String, String> req) {
        String email = req.get("email");
        String code = req.get("code");
        String newPassword = req.get("newPassword");

        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (s.getMfaCode() != null && s.getMfaCode().equals(code)) {
            s.setPassword(encoder.encode(newPassword));
            s.setMfaCode(null);
            studentRepository.save(s);
            java.util.Map<String, Object> res = new java.util.HashMap<>();
            res.put("message", "Password updated successfully");
            return ResponseEntity.ok(res);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired MFA Code");
        }
    }

    @PostMapping("/oauth-login")
    public Student oauthLogin(@RequestBody java.util.Map<String, String> req) {
        String email = req.get("email");
        String name = req.get("name");
        String provider = req.get("authProvider");
        String requestedRole = req.get("role");

        if (requestedRole == null) {
            requestedRole = "student";
        }

        // Find existing student or create a new one
        Student s = studentRepository.findByEmailIgnoreCase(email).orElse(null);
        if (s == null) {
            s = new Student();
            s.setEmail(email);
            s.setName(name);
            s.setAuthProvider(provider);
            s.setPassword(encoder.encode(java.util.UUID.randomUUID().toString()));
            s.setRole(requestedRole);
        } else {
            // Restrict login if the role does not match
            if (!requestedRole.equals(s.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account already exists with a different role.");
            }
        }
        
        return studentRepository.save(s);
    }

    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/{id}/master-resume")
    public Student uploadMasterResume(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        Student s = studentRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        try {
            java.io.File dir = new java.io.File("uploads");
            if(!dir.exists()) dir.mkdirs();
            String path = "uploads/master_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            file.transferTo(new java.io.File(path).getAbsoluteFile());
            s.setMasterResumePath(path);
            return studentRepository.save(s);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload master resume");
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public void deleteAccount(@PathVariable Long id) {
        workHourRepository.deleteByStudentId(id);
        applicationRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
    }

}
