package com.dsw.trabalho.minimalList.controller.api;

import com.dsw.trabalho.minimalList.dto.ProfileRequestDTO;
import com.dsw.trabalho.minimalList.dto.UserRegisterDTO;
import com.dsw.trabalho.minimalList.dto.UserSignInDTO;
import com.dsw.trabalho.minimalList.model.User;
import com.dsw.trabalho.minimalList.repository.UserRepository;
import com.dsw.trabalho.minimalList.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository repository;
    private String pathUpload = "src/main/resources/static/images";

    @GetMapping("/profile/{id}")
    public ResponseEntity<Object> getProfile(@PathVariable Integer id) {
        Optional<User> user = repository.findById(id);

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        return ResponseEntity.status(HttpStatus.OK).body(user.get());
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestBody UserRegisterDTO registerDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        Optional<User> user = repository.findByEmail(registerDTO.getEmail());

        if (user.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already exists!");
        }

        User newUser = User.builder().email(registerDTO.getEmail()).password(registerDTO.getPassword()).build();

        return ResponseEntity.ok(newUser);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> signIn(@RequestBody UserSignInDTO userDto) {
        Optional<User> user = repository.findByEmail(userDto.getEmail());

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email or password incorrect");
        }

        if (!user.get().getPassword().equals(userDto.getPassword())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email or password incorrect");
        }

        User userLogged = user.get();

        return ResponseEntity.ok(userLogged);
    }

    @PutMapping("/profile/{id}")
    public ResponseEntity<Object> updateProfile(
            @PathVariable(value = "id") Integer id, @RequestBody ProfileRequestDTO profileDto) {
        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User user = userOptional.get();

        BeanUtils.copyProperties(profileDto, user);
        user.setId(id);
        repository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/profile/image/{id}")
    public ResponseEntity<Object> updateProfileImage(
            @PathVariable Integer id, @RequestParam("image") MultipartFile file) {
        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User user = userOptional.get();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String randomID = UUID.randomUUID().toString();
        fileName = randomID.concat(fileName.substring(fileName.lastIndexOf(".")));

        String uploadDir = pathUpload + "/profile/" + user.getId();

        try {

            FileService.saveFile(uploadDir, fileName, file);

            user.setImage(fileName);
            user.setImagePath(uploadDir);
            repository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Could not save image: " + fileName);
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @PutMapping("/profile/background/{id}")
    public ResponseEntity<Object> updateProfileBackground(
            @PathVariable Integer id, @RequestParam("image") MultipartFile file) {
        Optional<User> userOptional = repository.findById(id);

        if (!userOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User user = userOptional.get();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        String randomID = UUID.randomUUID().toString();
        fileName = randomID.concat(fileName.substring(fileName.lastIndexOf(".")));

        String uploadDir = pathUpload + "/profile/" + user.getId();

        try {

            FileService.saveFile(uploadDir, fileName, file);

            user.setImage(fileName);
            user.setImagePath(uploadDir);
            repository.save(user);
        } catch (IOException e) {
            throw new RuntimeException("Could not save image: " + fileName);
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/profile/{id}")
    public ResponseEntity<Object> deleteProfile(@PathVariable(value = "id") Integer id) {
        Optional<User> user = repository.findById(id);
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        repository.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK).body("User deleted!");
    }

    @GetMapping("/profile/image/{id}")
    public ResponseEntity<Object> getProfileImages(@PathVariable int id) {
        Optional<User> user = repository.findById(id);

        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        User userLogged = user.get();

        System.out.println(userLogged);
        Resource imageResource = new ClassPathResource(userLogged.getImagePath() + "/"+ userLogged.getImage());
        if (!imageResource.exists()) {
            imageResource = new ClassPathResource("src/main/resources/static/images/profile/default.jpeg");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.IMAGE_JPEG)
                .body(imageResource);
    }
}
