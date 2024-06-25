package com.example.auto_ria.dao.socket;

import com.example.auto_ria.models.socket.MessageClass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageDaoSQL extends JpaRepository<MessageClass, Integer> {
    Page<MessageClass> getByChatId(int chatId, Pageable pageable);
    MessageClass getById(int id);
}
