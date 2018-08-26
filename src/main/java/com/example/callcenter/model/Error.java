package com.example.callcenter.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Error {

    String message;

    String cause;
}
