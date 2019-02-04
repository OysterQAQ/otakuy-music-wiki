package com.otakuy.otakuymusic.controller;

import com.otakuy.otakuymusic.exception.CheckException;
import com.otakuy.otakuymusic.model.Album;
import com.otakuy.otakuymusic.model.Result;
import com.otakuy.otakuymusic.service.AlbumService;
import com.otakuy.otakuymusic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/check")
public class CheckController {
    private final UserService userService;
    private final AlbumService albumService;

    @Autowired
    public CheckController(UserService userService, AlbumService albumService) {
        this.userService = userService;
        this.albumService = albumService;
    }

    @GetMapping("/usernames")
    public Mono<ResponseEntity<Result<String>>> checkUserByUsername(@RequestParam String username) {
        return userService.findByUsername(username).hasElement().map(exit -> {
            if (exit)
                throw new CheckException(new Result<>(HttpStatus.BAD_REQUEST, "该用户名已被注册"));
            return ResponseEntity.ok(new Result<>("该用户名可以被注册"));
        });
    }

    @GetMapping("/emails")
    public Mono<ResponseEntity<Result<String>>> checkUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email).hasElement().map(exit -> {
            if (exit)
                throw new CheckException(new Result<>(HttpStatus.BAD_REQUEST, "该邮箱已被注册"));
            return ResponseEntity.ok(new Result<>("该邮箱可以被注册"));
        });
    }

    @GetMapping("/albums")
    public Mono<ResponseEntity<Result<List<Album>>>> checkAlbumByTitle(@RequestParam String title) {
        return albumService.findAllByFilter("title", title).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "", albums)));
    }
}