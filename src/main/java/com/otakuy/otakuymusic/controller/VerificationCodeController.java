package com.otakuy.otakuymusic.controller;

import com.otakuy.otakuymusic.model.Result;
import com.otakuy.otakuymusic.service.VerificationCodeService;
import com.otakuy.otakuymusic.util.VerificationCodeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class VerificationCodeController {
    private final VerificationCodeService verificationCodeService;

    //获取验证码
    @GetMapping("/verificationCode")
    public Mono<ResponseEntity<Result<VerificationCodeUtil.VerificationCode>>> getVerificationCode() {
        Mono<VerificationCodeUtil.VerificationCode> verificationCode = verificationCodeService.getVerificationCode();
        return verificationCode.map(code -> ResponseEntity.ok(new Result<>("ok", code)));
    }
}
