package com.winwin.orbital.manager;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
public class ManagerDto {

    private long id;
    private String username;

    public ManagerDto(Manager manager) {
        this.id = manager.getId();
        this.username = manager.getUsername();
    }

}
