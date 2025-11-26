package glue.Gachi_Sanchaek.domain.stamp.service;

import glue.Gachi_Sanchaek.common.exception.StampNotFoundException;
import glue.Gachi_Sanchaek.domain.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.domain.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.domain.stamp.repository.StampRepository;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StampService {
    private final StampRepository stampRepository;
    private final UserService userService;

    private final JdbcTemplate jdbcTemplate;


    public List<StampResponseDto> getAllStampsWithUserStatus(Long userId){
        User user = userService.findById(userId);
        List<Stamp> stamps = stampRepository.findAll();

        return stamps.stream()
                .map(stamp -> {
                    StampResponseDto dto = new StampResponseDto(stamp);
                    dto.checkActivable(user.getTotalPoints());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Stamp findById(Long stampId) {
        return stampRepository.findById(stampId)
                .orElseThrow(() -> new StampNotFoundException("Stamp not Found. id = " + stampId));
    }

    @Transactional
    public void saveAllStamps(List<Stamp> stamps){
        String sql = "INSERT INTO stamps (id, name, image_url, price, created_at) VALUES (?,?,?,?,?)";
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Stamp stamp = stamps.get(i);
                ps.setLong(1, stamp.getId());
                ps.setString(2, stamp.getName());
                ps.setString(3, stamp.getImageUrl());
                ps.setLong(4, stamp.getPrice());
                ps.setTimestamp(5, Timestamp.valueOf(stamp.getCreatedAt()));
            }

            @Override
            public int getBatchSize() {
                return stamps.size();
            }
        });
    }

}
