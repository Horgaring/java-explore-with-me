package ru.practicum.ipinfo.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IpInfoId implements Serializable {

    private String ip;
    private String path;

    public IpInfoId() {}

    public IpInfoId(String ip, String path) {
        this.ip = ip;
        this.path = path;
    }

    public String getIp() {
        return ip;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpInfoId that = (IpInfoId) o;
        return Objects.equals(ip, that.ip) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, path);
    }
}
