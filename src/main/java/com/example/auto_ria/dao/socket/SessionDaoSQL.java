package com.example.auto_ria.dao.socket;

import com.example.auto_ria.models.socket.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface SessionDaoSQL extends JpaRepository<Session, Integer> {
    Session getBySessionId(String sessionId);

    @Transactional
    Session deleteAllByUserId(int userId);

    @Query("SELECT s FROM Session s WHERE s.userId = :userId ORDER BY s.createdAt DESC")
    Session findLatestSessionByUserId(@Param("userId") int userId);
}
