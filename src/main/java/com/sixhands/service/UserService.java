package com.sixhands.service;

import com.sixhands.SixHandsApplication;
import com.sixhands.domain.Project;
import com.sixhands.domain.User;
import com.sixhands.domain.UserProjectExp;
import com.sixhands.exception.UserAlreadyExistsException;
import com.sixhands.misc.GenericUtils;
import com.sixhands.repository.UserProjectExpRepository;
import com.sixhands.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private MailSender mailSender;
    @Autowired
    public UserProjectExpRepository userProjectExpRepo;
    @Autowired
    private UserRepository userRepo;
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        UsernameNotFoundException notFoundException = new UsernameNotFoundException("Unable to find user "+username);
        if(StringUtils.isEmpty(username)) throw notFoundException;
        return userRepo.findByEmail(username).orElseThrow(()->notFoundException);
    }
    public User registerUser(String email) throws UserAlreadyExistsException {
        return registerUser(email, GenericUtils.randomAlphaNumString(8));
    }
    public User registerUser(String email, boolean isProjectMember) throws UserAlreadyExistsException {
        return registerUser(email, GenericUtils.randomAlphaNumString(8), isProjectMember);
    }
    public User registerUser(String email, String plainPassword) throws UserAlreadyExistsException{
        return registerUser(email,plainPassword,false);
    }
    public User registerUser(String email, @NotNull String plainPassword, boolean isProjectMember) throws UserAlreadyExistsException {
        if(plainPassword == null) return registerUser(email,isProjectMember);
        User user = null;
        try { user = loadUserByUsername(email); } catch(Exception ignored){}
        if(user != null) throw new UserAlreadyExistsException(email);
        user = new User();
        if(SixHandsApplication.requireVerification())
            user.setActivationCode(UUID.randomUUID().toString());
        user.setRole("ROLE_USER");
        user.setPassword(passwordEncoder.encode(plainPassword));
        user.setEmail(email);
        user = userRepo.save(user);
        if(SixHandsApplication.requireVerification()) {
            if(isProjectMember) sendMemberVerificationMail(user, plainPassword);
            else sendVerificationMail(user);
        }
        return user;
    }
    @Value("${6hands.domain}")
    private String domain;

    private void sendMemberVerificationMail(User user, String plainPassword) {
        if(StringUtils.isEmpty(user.getEmail())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User email is null or empty");
        String emailText = String.format(
                "Hello, %s! \n" +
                "You are invited as a member of a project. \n" +
                "Your password is %s\n" +
                "Please, visit this link to verify your account: http://%s/activation/%s",
                user.getEmail(),
                plainPassword,
                domain,
                user.getActivationCode()
        );
        mailSender.send(user.getEmail(), "Activate your profile", emailText);
    }

    private void sendVerificationMail(User user){
        if(StringUtils.isEmpty(user.getEmail())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"User email is null or empty");
        String emailText = String.format(
            "Hello, %s! \n" +
            "Welcome to 6hands. Please, visit link: http://%s/activation/%s",
            user.getEmail(),
            domain,
            user.getActivationCode()
        );
        mailSender.send(user.getEmail(), "Activate your profile", emailText);
    }

    public List<UserProjectExp> getProjectExpForUser(User user){
        if(user == null || user.getUuid() == null) return new ArrayList<>();
        return userProjectExpRepo.findAll().stream()
                .filter((exp)-> exp.getUser_uuid().equals(user.getUuid()))
                .collect(Collectors.toList());
    }

    public static Optional<String> getCurrentUsername() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) return Optional.empty();

        String username = null;
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails springSecurityUser = (UserDetails) authentication.getPrincipal();
            username = springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof String) {
            username = (String) authentication.getPrincipal();
        }
        username = username == null || username.length() == 0 || username.equals("anonymousUser") ? null : username;
        return Optional.ofNullable(username);
    }

    public boolean sendRecoverMail(User user){
        if (!StringUtils.isEmpty(user.getEmail())) {
            String emailText = String.format(
                "Hello, %s! \n" +
                        "You want to recover your password. Please, visit link: http://%s/recover_password",
                user.getEmail(),
                domain
            );
            mailSender.send(user.getEmail(), "Recover password", emailText);
        }
        return true;
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActivationCode(null);
        userRepo.save(user);

        return true;
    }
}
