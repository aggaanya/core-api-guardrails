package com.aanya.coreapi.repository;

import com.aanya.coreapi.entity.Bot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BotRepository extends JpaRepository<Bot, Long> {

    Optional<Bot> findByName(String name);

}