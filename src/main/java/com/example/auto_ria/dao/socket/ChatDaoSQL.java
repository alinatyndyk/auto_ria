package com.example.auto_ria.dao.socket;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.auto_ria.models.socket.Chat;

public interface ChatDaoSQL extends JpaRepository<Chat, Integer> {

    Chat getByRoomKey(String roomKey);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chat c WHERE c.id IN (SELECT c1.id FROM Chat c1 JOIN c1.users u WHERE :userId IN (u))")
    void deleteChatsByUserId(@Param("userId") int userId);

    @Query("SELECT c FROM Chat c JOIN c.users u WHERE :userId IN (u)")
    List<Chat> findAllChatsByUserId(@Param("userId") int userId);

    @Query("SELECT c FROM Chat c JOIN c.users u WHERE :userId IN (u)")
    Page<Chat> findAllChatsByUserId(@Param("userId") int userId, Pageable pageable);

}
