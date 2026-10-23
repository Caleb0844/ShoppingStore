package com.caleb.Creator_store.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.caleb.Creator_store.Entities.User;


public interface UserRepo extends  JpaRepository<User, Long>{

}
