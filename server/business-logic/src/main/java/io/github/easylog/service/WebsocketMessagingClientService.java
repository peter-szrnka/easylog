package io.github.easylog.service;

/**
 * @author Peter Szrnka
 */
public interface WebsocketMessagingClientService {

    void convertAndSend(String destination, Object payload) throws Exception;
}
