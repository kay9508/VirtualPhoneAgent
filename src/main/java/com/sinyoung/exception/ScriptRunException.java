package com.sinyoung.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.nio.charset.Charset;

public class ScriptRunException extends HttpClientErrorException {
    public ScriptRunException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, null, null, null, Charset.forName("UTF-8"));
    }
}
