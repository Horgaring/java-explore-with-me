package ru.practicum.ipinfo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ipinfo.model.IpInfo;
import ru.practicum.ipinfo.model.IpInfoId;
import ru.practicum.ipinfo.repository.IpInfoRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IpInfoServiceImpl implements IpInfoService {

    private final IpInfoRepository ipInfoRepository;

    @Override
    @Transactional
    public void create(String ip, String path) {
        IpInfoId id = new IpInfoId(ip, path);
        if (!ipInfoRepository.existsById(id)) {
            ipInfoRepository.save(new IpInfo(id));
        }
    }

    @Override
    public boolean existsByIpAndPath(String ip, String path) {
        return ipInfoRepository.existsById(new IpInfoId(ip, path));
    }
}
