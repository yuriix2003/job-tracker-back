package com.yashfi.job.job_apps.service;

import com.yashfi.job.job_apps.dto.AuthResponse;
import com.yashfi.job.job_apps.dto.LoginRequest;
import com.yashfi.job.job_apps.dto.RegisterRequest;
import com.yashfi.job.job_apps.exception.ResourceNotFoundException;
import com.yashfi.job.job_apps.model.User;
import com.yashfi.job.job_apps.model.ApplicantProfile;
import com.yashfi.job.job_apps.model.CompanyProfile;
import com.yashfi.job.job_apps.repository.UserRepository;
import com.yashfi.job.job_apps.repository.ApplicantProfileRepository;
import com.yashfi.job.job_apps.repository.CompanyProfileRepository;
import com.yashfi.job.job_apps.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicantProfileRepository applicantProfileRepository;

    @Autowired
    private CompanyProfileRepository companyProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole(User.UserRole.valueOf(request.getRole()));

        User savedUser = userRepository.save(user);

        if (savedUser.getRole() == User.UserRole.APPLICANT) {
            ApplicantProfile profile = new ApplicantProfile();
            profile.setUser(savedUser);
            applicantProfileRepository.save(profile);
        } else if (savedUser.getRole() == User.UserRole.COMPANY) {
            CompanyProfile profile = new CompanyProfile();
            profile.setUser(savedUser);
            companyProfileRepository.save(profile);
        }

        String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

        return new AuthResponse(token, savedUser.getEmail(), savedUser.getName(), savedUser.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(token, user.getEmail(), user.getName(), user.getRole().name());
    }

    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}