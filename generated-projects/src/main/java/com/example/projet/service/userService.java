package com.example.projet.service;

import java.util.List;
import java.util.Optional;

import com.example.projet.model.user;
import com.example.projet.repository.userRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class userService {

    @Autowired
    private userRepository userRepository;

    public List<user> findAll() {
        return userRepository.findAll();
    }

    public Optional<user> findById(integer userid) {
        return userRepository.findById(userid);
    }

    public user save(user entity) {
        return userRepository.save(entity);
    }

    public void deleteById(integer userid) {
        userRepository.deleteById(userid);
    }
}


