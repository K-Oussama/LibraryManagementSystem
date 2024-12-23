package com.sos.lms.dao;

import com.sos.lms.entity.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Integer> {
    List<Borrow> findByUserId(Integer userId);
    List<Borrow> findByBookId(Integer bookId);
}
