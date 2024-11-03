package gang.GNUtingBackend.board.repository;

import gang.GNUtingBackend.board.dto.ApplyUsersDto;
import gang.GNUtingBackend.board.entity.ApplyUsers;
import gang.GNUtingBackend.board.entity.Board;
import gang.GNUtingBackend.board.entity.BoardApplyLeader;
import gang.GNUtingBackend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplyUsersRepository extends JpaRepository<ApplyUsers, Long> {


   // List<ApplyUsers> findByBoardApplyLeaderId(List<BoardApplyLeader> boardApplyUsers);
}
