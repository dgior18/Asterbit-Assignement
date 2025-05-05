package ge.asterbit.assignment.repository;

import ge.asterbit.assignment.entity.Project;
import ge.asterbit.assignment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByOwner(User owner, Pageable pageable);
    Optional<Project> findByIdAndOwner(Long id, User owner);
    List<Project> findByOwner(User owner);
    boolean existsByIdAndOwner(Long id, User owner);
} 