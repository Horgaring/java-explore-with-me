package ru.practicum.ipinfo.service;

public interface IpInfoService {

    void create(String ip, String path);

    boolean existsByIpAndPath(String ip, String path);
}
