package ru.practicum.ipinfo.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ip_info")
public class IpInfo {

    @EmbeddedId
    private IpInfoId id;

    public IpInfo() {}

    public IpInfo(IpInfoId id) {
        this.id = id;
    }

    public IpInfoId getId() {
        return id;
    }

    public void setId(IpInfoId id) {
        this.id = id;
    }
}
