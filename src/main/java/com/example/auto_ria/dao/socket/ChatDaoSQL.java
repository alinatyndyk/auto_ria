package com.example.auto_ria.dao.socket;

import com.example.auto_ria.models.socket.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatDaoSQL extends JpaRepository<Chat, Integer> {

    Chat getByRoomKey(String roomKey);

    // List<Chat> getBySellerId(int sellerId);
    // Page<Chat> getBySellerId(int sellerId, Pageable pageable);

    // List<Chat> getByCustomerId(int customerId);

    // List<Chat> getByUserId(int userId);

    // Page<Chat> getByUserId(int userId, Pageable pageable);

    @Query("SELECT c FROM Chat c JOIN c.users u WHERE :userId IN (u)")
    List<Chat> findAllChatsByUserId(@Param("userId") int userId);

    @Query("SELECT c FROM Chat c JOIN c.users u WHERE :userId IN (u)")
    Page<Chat> findAllChatsByUserId(@Param("userId") int userId, Pageable pageable);

    // Chat getByUser1IdandUser2Id(int user1Id, int user2Id); 111
    // Chat getByUser1IdAndUser2Id(@Param("user1Id") int user1Id, @Param("user2Id")
    // int user2Id);

    @Query("SELECT c FROM Chat c JOIN c.users u WHERE :user1Id IN (u) AND :user2Id IN (u)")
    Chat findByUsers(@Param("user1Id") int user1Id, @Param("user2Id") int user2Id);

    // Page<Chat> getByCustomerId(int customerId, Pageable pageable);

}
