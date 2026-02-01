package com.campus.issue_tracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.campus.issue_tracker.dto.IssueRequest;
import com.campus.issue_tracker.entity.Issue;
import com.campus.issue_tracker.entity.Role;
import com.campus.issue_tracker.entity.User;
import com.campus.issue_tracker.repository.UserRepository;
import com.campus.issue_tracker.service.IssueService;
import com.campus.issue_tracker.service.FileStorageService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@Controller
public class ViewController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup";
    }

    @PostMapping("/signup")
    public String processSignup(@RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam(defaultValue = "ROLE_STUDENT") Role role,
                                Model model) {
        // Check if username already exists
        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "Username is already taken!");
            return "signup";
        }

        // Create and save new user with encoded password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        // Redirect to login page after successful signup
        return "redirect:/login?registered";
    }

    @GetMapping("/report")
    public String report() {
        return "report";
    }

    @PostMapping("/report")
    @PreAuthorize("hasRole('STUDENT')")
    public String reportIssueWithImage(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("location") String location,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {

        IssueRequest request = new IssueRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setLocation(location);
        request.setLatitude(latitude);
        request.setLongitude(longitude);


        Issue issue = issueService.createIssue(request, authentication.getName());

        if (file != null && !file.isEmpty()) {
            String fileName = fileStorageService.save(file);
            issue.setAttachmentPath(fileName);
            issueService.saveDirectly(issue);
        }

        // Redirect to dashboard so the user sees their new issue in the list
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @RequestParam(defaultValue = "0") int page) {
        // We fetch page 0, 10 items per page
        var issuePage = issueService.getIssuesPaged(page, 10, "createdAt", "desc");

        model.addAttribute("issues", issuePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuePage.getTotalPages());

        return "dashboard";
    }
}
