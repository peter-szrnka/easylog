package hu.peterszrnka.easylog.service;

import hu.peterszrnka.easylog.model.SaveLogRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class LogController {

    private final LogService logService;

    @PostMapping
    public void save(@RequestBody SaveLogRequest request) {
        logService.save(request);
    }
}