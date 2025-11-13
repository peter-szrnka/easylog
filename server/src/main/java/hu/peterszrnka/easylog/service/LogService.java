package hu.peterszrnka.easylog.service;

import hu.peterszrnka.easylog.model.SaveLogRequest;

public interface LogService {

    void save(SaveLogRequest request);
}
