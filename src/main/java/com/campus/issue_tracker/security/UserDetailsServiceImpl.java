package com.campus.issue_tracker.security;

import com.campus.issue_tracker.entity.Admin;
import com.campus.issue_tracker.entity.Student;
import com.campus.issue_tracker.repository.AdminRepository;
import com.campus.issue_tracker.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1️⃣ Check STUDENT
        Student student = studentRepository.findByEmail(username).orElse(null);
        if (student != null) {
            return User.builder()
                    .username(student.getEmail())
                    .password(student.getPassword())
                    .roles("STUDENT")
                    .build();
        }

        // 2️⃣ Check ADMIN
        Admin admin = adminRepository.findByEmail(username).orElse(null);
        if (admin != null) {
            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .roles("ADMIN")
                    .build();
        }

        throw new UsernameNotFoundException("User not found with email: " + username);
    }
}
