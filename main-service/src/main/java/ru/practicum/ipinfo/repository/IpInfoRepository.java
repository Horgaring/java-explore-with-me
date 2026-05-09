package ru.practicum.ipinfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ipinfo.model.IpInfo;
import ru.practicum.ipinfo.model.IpInfoId;

public interface IpInfoRepository extends JpaRepository<IpInfo, IpInfoId> {

    boolean existsById(IpInfoId id);
}
