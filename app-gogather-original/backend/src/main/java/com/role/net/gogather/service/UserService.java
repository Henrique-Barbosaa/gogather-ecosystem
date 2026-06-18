package com.role.net.gogather.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.role.net.gogather.dto.user.RegisterPixKeyRequest;
import com.role.net.gogather.entity.PixInfo;
import com.role.net.gogather.entity.User;
import com.role.net.gogather.exception.DataDoesntMatchException;
import com.role.net.gogather.exception.ResourceNotFoundException;
import com.role.net.gogather.exception.UniqueDataAlreadyInUseException;
import com.role.net.gogather.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.encoder = passwordEncoder;
    }

    private User findById(String id) {
        UUID uuid = UUID.fromString(id);
        User user = this.userRepository.findByExternalId(uuid)
            .orElseThrow(() -> new ResourceNotFoundException("User id " + uuid + " not found."));
        return user;
    }

    public User findByInternalId(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User " + id + " not found."));
    }

    public List<User> findBySearch(String search) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(search);
        if(users.isEmpty()) throw new ResourceNotFoundException("Nenhum usuário com username contendo " + search);
        return users;
    }

    @Transactional
    public User update(
        String id,
        String username,
        String displayName,
        String email,
        String password,
        String newPassword,
        LocalDate birthDate
    ) {
        User user = this.findById(id);

        if(!this.encoder.matches(password, user.getPassword()))
            throw new DataDoesntMatchException("A senha está incorreta.");

        if(newPassword != null && !newPassword.isBlank())
            user.setPassword(this.encoder.encode(newPassword));

        if(username != null && !username.isBlank() && !username.equals(user.getUsername())){
            if(this.userRepository.existsByUsername(username))
                throw new UniqueDataAlreadyInUseException("Username " + username + " already in use.");
            user.setUsername(username);
        }

        if(email != null && !email.isBlank() && !email.equals(user.getEmail())){
            if(this.userRepository.existsByEmail(email))
                throw new UniqueDataAlreadyInUseException("Email " + email + " already in use.");
            user.setEmail(email);
        }

        if(displayName != null && !displayName.isBlank())
            user.setDisplayName(displayName);

        if(birthDate != null)
            user.setBirthDate(birthDate);

        return user;
    }

    @Transactional
    public void delete(String id) {
        User userToDelete = this.findById(id);
        this.userRepository.delete(userToDelete);
    }

    @Transactional
    public void registerPix(Long loggedUserId, RegisterPixKeyRequest request) {
        User user = userRepository.findById(loggedUserId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        user.setPixInfo(
            new PixInfo(
                request.pixKey(),
                request.merchantName(),
                request.merchantCity(),
                user
            )
        );
    }

}
