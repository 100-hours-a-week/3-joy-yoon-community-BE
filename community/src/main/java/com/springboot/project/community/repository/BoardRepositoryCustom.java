package com.springboot.project.community.repository;

import com.springboot.project.community.entity.Board;
import java.util.List;

public interface BoardRepositoryCustom extends JpaRepository<Board, Long>, BoardRepositoryCustom {

}