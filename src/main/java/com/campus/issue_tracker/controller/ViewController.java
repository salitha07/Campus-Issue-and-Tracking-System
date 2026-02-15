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
import com.campus.issue_tracker.entity.IssueCategory;
import com.campus.issue_tracker.entity.IssueStatus;
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
    private com.campus.issue_tracker.service.EmailService emailService;

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
        // ✅ Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "Email is already registered!");
            return "signup";
        }

        // Create and save new user with encoded password
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        // Generate OTP
        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));
        user.setVerified(false);

        userRepository.save(user);

        // Send OTP Email
        emailService.sendEmail(email, "Campus Issue Tracker - Verify Email", "Your OTP is: " + otp);

        // Redirect to verify otp page
        return "redirect:/verify-otp?email=" + email;
    }

    @GetMapping("/verify-otp")
    public String verifyOtpPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otp, Model model) {
        java.util.Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getOtp() != null && user.getOtp().equals(otp)
                    && user.getOtpExpiry().isAfter(java.time.LocalDateTime.now())) {
                user.setVerified(true);
                user.setOtp(null);
                user.setOtpExpiry(null);
                userRepository.save(user);
                return "redirect:/login?verified";
            } else {
                model.addAttribute("error", "Invalid or expired OTP");
                model.addAttribute("email", email);
                return "verify-otp";
            }
        }
        model.addAttribute("error", "User not found");
        return "verify-otp";
    }

    @GetMapping("/report-options")
    public String reportOptions() {
        return "report-options";
    }

    @GetMapping("/select-category")
    public String selectCategory(@RequestParam(name = "anonymous", defaultValue = "false") boolean anonymous,
            Model model) {
        model.addAttribute("anonymous", anonymous);
        return "select-category";
    }

    @GetMapping("/report/academic")
    public String reportAcademic(@RequestParam(name = "anonymous", defaultValue = "false") boolean anonymous,
            Model model) {
        model.addAttribute("anonymous", anonymous);
        return "report-academic";
    }

    @GetMapping("/report/payments")
    public String reportPayments(@RequestParam(name = "anonymous", defaultValue = "false") boolean anonymous,
            Model model) {
        model.addAttribute("anonymous", anonymous);
        return "report-payments";
    }

    @GetMapping("/report/hostel")
    public String reportHostel(@RequestParam(name = "anonymous", defaultValue = "false") boolean anonymous,
            Model model) {
        model.addAttribute("anonymous", anonymous);
        return "report-hostel";
    }

    @GetMapping("/report/other")
    public String reportOther(@RequestParam(name = "anonymous", defaultValue = "false") boolean anonymous,
            Model model) {
        model.addAttribute("anonymous", anonymous);
        return "report-other";
    }

    @PostMapping("/report")
    @PreAuthorize("hasRole('STUDENT')")
    public String reportIssueWithImage(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "anonymous", defaultValue = "false") boolean anonymous,
            @RequestParam(value = "category", required = false) com.campus.issue_tracker.entity.IssueCategory category,
            @RequestParam(value = "courseUnit", required = false) String courseUnit,
            @RequestParam(value = "paymentId", required = false) String paymentId,
            @RequestParam(value = "hostelBlock", required = false) String hostelBlock,
            @RequestParam(value = "roomNumber", required = false) String roomNumber,
            @RequestParam(value = "latitude", required = false) String latitudeStr,
            @RequestParam(value = "longitude", required = false) String longitudeStr,
            @RequestParam(value = "file", required = false) org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {

        Double latitude = null;
        if (latitudeStr != null && !latitudeStr.trim().isEmpty()) {
            try {
                latitude = Double.parseDouble(latitudeStr);
            } catch (NumberFormatException e) {
                // Log or ignore, treat as null
            }
        }

        Double longitude = null;
        if (longitudeStr != null && !longitudeStr.trim().isEmpty()) {
            try {
                longitude = Double.parseDouble(longitudeStr);
            } catch (NumberFormatException e) {
                // Log or ignore, treat as null
            }
        }

        IssueRequest request = new IssueRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setLocation(location);
        request.setLatitude(latitude);
        request.setLongitude(longitude);
        request.setAnonymous(anonymous);
        request.setCategory(category);
        request.setCourseUnit(courseUnit);
        request.setPaymentId(paymentId);
        request.setHostelBlock(hostelBlock);
        request.setRoomNumber(roomNumber);

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
    public String dashboard(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) IssueCategory category,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) String dateRange) {

        // Fetch filtered issues
        var issuePage = issueService.getIssuesWithFilters(page, 10, "createdAt", "desc", q, category, status,
                dateRange);

        model.addAttribute("issues", issuePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", issuePage.getTotalPages());

        // Pass filter values back to view
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        model.addAttribute("dateRange", dateRange);

        return "dashboard";
    }
}
