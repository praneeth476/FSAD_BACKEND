package com.workstudy.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.workstudy.backend.model.Student;
import com.workstudy.backend.repository.StudentRepository;
import com.workstudy.backend.repository.ApplicationRepository;
import com.workstudy.backend.repository.WorkHourRepository;
import com.workstudy.backend.service.EmailService;

import jakarta.transaction.Transactional;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = {"*"})
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

    // ✅ TEST ENDPOINT (VERY IMPORTANT)
    @GetMapping("/test")
    public String test() {
        return "✅ Student API is working!";
    }

    // ✅ REGISTER
    @PostMapping("/register")
    public Student register(@RequestBody Student student) {

        if (studentRepository.findByEmailIgnoreCase(student.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account already exists");
        }

        student.setPassword(encoder.encode(student.getPassword()));

        if (student.getRole() == null) {
            student.setRole("student");
        }

        return studentRepository.save(student);
    }

    // ✅ LOGIN
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

        // Generate OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        s.setMfaCode(otp);
        studentRepository.save(s);

        emailService.sendMfaCode(s.getEmail(), otp);

        Map<String, Object> res = new HashMap<>();
        res.put("mfaRequired", true);
        res.put("email", s.getEmail());

        return ResponseEntity.ok(res);
    }

    // ✅ VERIFY MFA
    @PostMapping("/verify-mfa")
    public ResponseEntity<Student> verifyMfa(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String code = req.get("code");

        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (s.getMfaCode() != null && s.getMfaCode().equals(code)) {
            s.setMfaCode(null);
            studentRepository.save(s);
            return ResponseEntity.ok(s);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }
    }

    // ✅ FORGOT PASSWORD OTP
    @PostMapping("/forgot-password-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> req) {

        String email = req.get("email");

        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        s.setMfaCode(otp);
        studentRepository.save(s);

        emailService.sendMfaCode(s.getEmail(), otp);

        Map<String, Object> res = new HashMap<>();
        res.put("message", "OTP sent to " + email);

        return ResponseEntity.ok(res);
    }

    // ✅ RESET PASSWORD
    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String code = req.get("code");
        String newPassword = req.get("newPassword");

        Student s = studentRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (s.getMfaCode() != null && s.getMfaCode().equals(code)) {

            s.setPassword(encoder.encode(newPassword));
            s.setMfaCode(null);
            studentRepository.save(s);

            return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }
    }

    // ✅ OAUTH LOGIN
    @PostMapping("/oauth-login")
    public Student oauthLogin(@RequestBody Map<String, String> req) {

        String email = req.get("email");
        String name = req.get("name");
        String provider = req.get("authProvider");
        String role = req.getOrDefault("role", "student");

        Student s = studentRepository.findByEmailIgnoreCase(email).orElse(null);

        if (s == null) {
            s = new Student();
            s.setEmail(email);
            s.setName(name);
            s.setAuthProvider(provider);
            s.setPassword(encoder.encode(java.util.UUID.randomUUID().toString()));
            s.setRole(role);
        } else {
            if (!role.equals(s.getRole())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Role mismatch");
            }
        }

        return studentRepository.save(s);
    }

    // ✅ GET ALL STUDENTS
    @GetMapping
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ✅ DELETE ACCOUNT
    @DeleteMapping("/{id}")
    @Transactional
    public void deleteAccount(@PathVariable Long id) {
        workHourRepository.deleteByStudentId(id);
        applicationRepository.deleteByStudentId(id);
        studentRepository.deleteById(id);
    }
}