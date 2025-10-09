package glue.Gachi_Sanchaek.stamp.service;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.repository.StampRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.userStamp.entity.UserStamp;
import glue.Gachi_Sanchaek.userStamp.repository.UserStampRepository;
import glue.Gachi_Sanchaek.userStamp.service.UserStampService;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StampService {
    private final StampRepository stampRepository;

    private final JdbcTemplate jdbcTemplate;

    public List<Stamp> findAll(){
        return (List<Stamp>) stampRepository.findAll();
    }

    public Stamp findById(Long stampId){
        return stampRepository.findById(stampId)
                .orElseThrow(()->new IllegalArgumentException("Stamp not Found. id = "+stampId));
    }

    public void saveStamps(List<Stamp> stamps){
        String sql = "INSERT INTO stamps (id, name, image_url, price, created_at) VALUES (?,?,?,?,?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Stamp stamp = stamps.get(i);
                ps.setLong(1, stamp.getId());
                ps.setString(2, stamp.getName());
                ps.setString(3, stamp.getImageUrl());
                ps.setLong(4, stamp.getPrice());
                ps.setTimestamp(5, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return stamps.size();
            }
        });
    }

}
