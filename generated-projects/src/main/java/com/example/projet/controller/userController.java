package com.example.projet.controller;

import org.springframework.web.bind.annotation.*;
import com.example.projet.model.user;
import java.util.*;

@RestController
@RequestMapping("/users")
public class userController {

    @GetMapping
    public List<user> getAll() {
        return new ArrayList<>();
    }

    @PostMapping
    public user create(@RequestBody user obj) {
        return obj;
    }

    @GetMapping("/{userid}")
    public user getById(@PathVariable integer userid) {
        return new user();
    }

    @PutMapping("/{userid}")
    public user update(@PathVariable integer userid, @RequestBody user obj) {
        obj.setUserid(userid);
        return obj;
    }

    @DeleteMapping("/{userid}")
    public void delete(@PathVariable integer userid) {
    }
}
