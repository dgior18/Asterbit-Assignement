package ge.asterbit.assignment.repository;

import ge.asterbit.assignment.entity.Project;
import ge.asterbit.assignment.entity.Task;
import ge.asterbit.assignment.entity.TaskPriority;
import ge.asterbit.assignment.entity.TaskStatus;
import ge.asterbit.assignment.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    Page<Task> findByProject(Project project, Pageable pageable);
    Page<Task> findByAssignedUser(User assignedUser, Pageable pageable);
    Page<Task> findByProjectAndStatus(Project project, TaskStatus status, Pageable pageable);
    Page<Task> findByProjectAndPriority(Project project, TaskPriority priority, Pageable pageable);
    Page<Task> findByAssignedUserAndStatus(User assignedUser, TaskStatus status, Pageable pageable);
    Optional<Task> findByIdAndProject(Long id, Project project);
    Optional<Task> findByIdAndAssignedUser(Long id, User assignedUser);
    Optional<Task> findByIdAndProjectOwner(Long id, User owner);
    List<Task> findByProject(Project project);
    boolean existsByIdAndAssignedUser(Long id, User assignedUser);
} 