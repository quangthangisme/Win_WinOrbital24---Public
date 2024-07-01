package com.winwin.orbital.manager;

import lombok.*;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class ManagerDto {

    private long id;
    private String username;

    public ManagerDto(Manager manager) {
        this.id = manager.getId();
        this.username = manager.getUsername();
    }

}
