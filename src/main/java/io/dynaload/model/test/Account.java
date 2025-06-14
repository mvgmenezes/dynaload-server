package io.dynaload.model.test;

import io.dynaload.annotations.Register;

@Register("v1/account")
public class Account {
    private Long id;
    private String owner;
}
