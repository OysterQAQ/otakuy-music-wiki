package com.otakuy.otakuymusic.controller;

import com.otakuy.otakuymusic.model.Album;
import com.otakuy.otakuymusic.model.Result;
import com.otakuy.otakuymusic.model.douban.AlbumSuggestion;
import com.otakuy.otakuymusic.service.AlbumService;
import com.otakuy.otakuymusic.service.UserService;
import com.otakuy.otakuymusic.util.AlbumUtil;
import com.otakuy.otakuymusic.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

@Log4j2
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AlbumController {
    private final AlbumService albumService;
    private final UserService userService;
    private final AlbumUtil albumUtil;
    private final JWTUtil jwtUtil;

    //按照条件拉取活跃状态专辑列表
    @GetMapping("/albums")
    public Mono<ResponseEntity<Result<List<Album>>>> getAlbumList(@RequestParam String filter, @RequestParam String param, @RequestParam Integer page) {
        switch (filter) {
            case "byTime":
                return albumService.findAllByStatus("active", PageRequest.of(page, 16, Sort.by(Sort.Direction.DESC, "id"))).collectList().map(albums -> ResponseEntity.ok().body(new Result<>("拉取专辑列表成功", albums)));
            case "byTitle":
                return albumService.findAllByTitleAndStatusActive(param, PageRequest.of(page, 16, Sort.by(Sort.Direction.DESC, "id"))).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张专辑", albums)));
            case "byTag":
                return albumService.findAllByTagAndStatusActive(param, PageRequest.of(page, 16, Sort.by(Sort.Direction.DESC, "id"))).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张专辑", albums)));
            case "byArtist":
                return albumService.findAllByArtistAndStatusActive(param, PageRequest.of(page, 16, Sort.by(Sort.Direction.DESC, "id"))).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张专辑", albums)));
        }
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result<>("过滤条件不存在")));
    }

    //增加新的专辑
    @PostMapping("/albums")
    public Mono<ResponseEntity<Result<Album>>> create(@RequestHeader("Authorization") String token, @Validated @RequestBody Album album) {
        return userService.findById(jwtUtil.getId(token)).flatMap(user -> albumService.create(albumUtil.initNew(user, album)).map(newAlbum -> ResponseEntity.status(HttpStatus.CREATED).body(new Result<>("新的维护创建成功,等待审核", newAlbum))));
    }

    //删除专辑(审核不通过专辑也可以删除)
    @DeleteMapping("/albums/{album_id}")
    public Mono<ResponseEntity<Result<String>>> delete(@RequestHeader("Authorization") String token, @PathVariable("album_id") String album_id) {
        return albumService.findById(album_id).flatMap(album -> {
            albumUtil.checkAuthority(token, album);
            return albumService.delete(album).map(x -> ResponseEntity.status(HttpStatus.OK).body(new Result<String>("删除成功")));
        }).defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result<>("专辑不存在")));
    }

    //修改专辑(审核不通过专辑也可以修改)
    @PutMapping("/albums/{album_id}")
    public Mono<ResponseEntity<Result<Album>>> update(@RequestHeader("Authorization") String token, @PathVariable("album_id") String album_id, @Validated @RequestBody Album album) {
        return albumService.findById(album_id).flatMap(oldAlbum -> {
            albumUtil.checkAuthority(token, oldAlbum);
            return albumService.save(albumUtil.update(oldAlbum, album)).map(newAlbum -> ResponseEntity.status(HttpStatus.OK).body(new Result<>("更新成功", newAlbum)));
        }).defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result<>("专辑不存在")));
    }

    //上传指定专辑的封面
    @PutMapping(value = "/albums/{album_id}/covers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Result<String>>> uploadCover(@RequestHeader("Authorization") String token, @PathVariable("album_id") String album_id, @RequestPart("file") FilePart filePart) throws IOException {
        return albumService.findById(album_id).flatMap(album -> {
            albumUtil.checkAuthority(token, album);
            try {
                return albumService.uploadCover(album_id, filePart).map(url -> ResponseEntity.ok(new Result<>("上传专辑封面成功", url)));
            } catch (IOException e) {
                e.printStackTrace();
                return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result<>("上传专辑封面失败")));
            }
        });
    }

    //查看专辑详细
    @GetMapping("/albums/{album_id}")
    public Mono<ResponseEntity<Result<Album>>> findById(@RequestHeader("Authorization") String token, @PathVariable("album_id") String album_id) {
        return albumService.findById(album_id).flatMap(album -> albumService.checkPermission(token, album).map(result -> {
                    if (!result)
                        album.setDownloadRes(null);
                    return ResponseEntity.status(HttpStatus.OK).body(new Result<>("拉取专辑详细成功", album));
                })
        ).defaultIfEmpty(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Result<>("专辑不存在")));
    }

    //查找指定用户的所有维护的专辑(包含通过与没通过)
    @GetMapping("/uers/{owner}/albums")
    public Mono<ResponseEntity<Result<List<Album>>>> findAllByOwner(@PathVariable("owner") String owner, @RequestParam Integer page) {
        return albumService.findAllByOwner(owner).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张维护专辑", albums))).defaultIfEmpty(ResponseEntity.ok(new Result<>("该用户不存在或者没有专辑", null)));
    }

    //查找指定用户的所有维护的专辑(包含通过)
    @GetMapping("/uers/{owner}/albums/active")
    public Mono<ResponseEntity<Result<List<Album>>>> findAllByOwnerAndStatusActive(@PathVariable("owner") String owner, @RequestParam Integer page) {
        return albumService.findAllByOwner(owner, PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "id"))).collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张维护专辑", albums))).defaultIfEmpty(ResponseEntity.ok(new Result<>("该用户不存在或者没有专辑", null)));
    }

    //获取首页轮播展示专辑 只返回专辑cover title intro
    @GetMapping("/albums/recommendAlbum")
    public Mono<ResponseEntity<Result<List<Album>>>> findAllByIsRecommend() {
        return albumService.findAllByIsRecommend().collectList().map(albums -> ResponseEntity.ok(new Result<>("共有" + albums.size() + "张置顶专辑", albums)));
    }

    //依赖豆瓣api根据指定专辑名匹配专辑
    @GetMapping("/douban")
    public Mono<ResponseEntity<Result<List<AlbumSuggestion>>>> getAlbumSuggestionByDouban(@RequestParam String title) throws UnsupportedEncodingException {
        return albumService.getAlbumSuggestionByDouban(title).map(albumSuggestions -> ResponseEntity.ok(new Result<>("以下是搜索建议", albumSuggestions)));
    }

    //依赖豆瓣api获取专辑详细信息
    @GetMapping("/douban/{douban_id}")
    public Mono<ResponseEntity<Result<Album>>> getAlbumDetailByDouban(@PathVariable("douban_id") String douban_id) throws IOException {
        return albumService.getAlbumDetailByDouban(douban_id).map(album -> ResponseEntity.ok(new Result<>("拉取成功", album)));
    }

}
