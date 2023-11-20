package com.example.auto_ria.dao.socket;

import com.example.auto_ria.models.socket.MessageClass;
import com.example.auto_ria.models.socket.Session;
import org.springframework.data.jpa.repository.JpaRepository;

//todo get messages by page

public interface SessionDaoSQL extends JpaRepository<Session, Integer> {
    Session getBySessionId(String sessionId);
}
