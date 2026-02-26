package com.m10.intergration.test.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public abstract class CommonIT {

    @Autowired
    protected ObjectMapper objectMapper;

}
