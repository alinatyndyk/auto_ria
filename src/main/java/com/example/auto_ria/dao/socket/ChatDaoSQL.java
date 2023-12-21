package com.example.auto_ria.dao.socket;

import com.example.auto_ria.models.socket.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatDaoSQL extends JpaRepository<Chat, Integer> {

    Chat getByRoomKey(String roomKey);

    List<Chat> getBySellerId(int sellerId);
    Page<Chat> getBySellerId(int sellerId, Pageable pageable);

    List<Chat> getByCustomerId(int customerId);

    Page<Chat> getByCustomerId(int customerId, Pageable pageable);

}
