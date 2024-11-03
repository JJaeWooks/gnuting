package gang.GNUtingBackend.board.service;

import gang.GNUtingBackend.board.dto.ApplicationStatusResponseDto;
import gang.GNUtingBackend.board.dto.BoardShowAllResponseDto;
import gang.GNUtingBackend.board.dto.ChatMemberDto;
import gang.GNUtingBackend.board.entity.ApplyUsers;
import gang.GNUtingBackend.board.entity.Board;
import gang.GNUtingBackend.board.entity.BoardApplyLeader;
import gang.GNUtingBackend.board.entity.BoardParticipant;
import gang.GNUtingBackend.board.entity.enums.ApplyShowStatus;
import gang.GNUtingBackend.board.entity.enums.ApplyStatus;
import gang.GNUtingBackend.board.entity.enums.Status;
import gang.GNUtingBackend.board.repository.BoardApplyLeaderRepository;
import gang.GNUtingBackend.board.repository.BoardParticipantRepository;
import gang.GNUtingBackend.board.repository.BoardRepository;
import gang.GNUtingBackend.chat.domain.ChatRoom;
import gang.GNUtingBackend.chat.dto.ChatRoomResponseDto;
import gang.GNUtingBackend.chat.service.ChatRoomService;
import gang.GNUtingBackend.exception.handler.BoardHandler;
import gang.GNUtingBackend.exception.handler.UserHandler;
//import gang.GNUtingBackend.meeting.entity.Meeting;
//import gang.GNUtingBackend.meeting.entity.MeetingApplyLeader;
//import gang.GNUtingBackend.meeting.repository.MeetingApplyLeaderRepository;
import gang.GNUtingBackend.notification.service.FCMService;
import gang.GNUtingBackend.response.code.status.ErrorStatus;
import gang.GNUtingBackend.user.domain.User;
import gang.GNUtingBackend.user.dto.UserSearchResponseDto;
import gang.GNUtingBackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationStatusService {

    private final BoardRepository boardRepository;
    private final BoardParticipantRepository boardParticipantRepository;
    private final UserRepository userRepository;
    private final BoardService boardService;
    private final BoardApplyLeaderRepository boardApplyLeaderRepository;
    private final FCMService fcmService;
    private final ChatRoomService chatRoomService;
//    private final MeetingApplyLeaderRepository meetingApplyLeaderRepository;


    /**
     * 내글에 신청한 현황보기
     * 1. 유저가 쓴글들을 가져오고
     * 2. 쓴글들의 참여자목록을 가져온다
     * 3. 글에 신청한 유저들을 가져온다
     * 4. 게시글에 대표로 신청한 리더를 찾아서 그 리더들을 기준으로 게시글에 신청한 유저들의 리스트를 만든다
     * 5. 참여자와 게시글에 신청한 유저들을 리스트에 합쳐서 반환한다
     *
     * @param email
     * @return
     */
    public List<ApplicationStatusResponseDto> receiveState(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        List<Board> boardList = boardRepository.findByUserId(user);
        List<ApplicationStatusResponseDto> allUsersByLeader = new ArrayList<>();
        String participantDepartment = user.getDepartment();


        for (Board boards : boardList) {  //내가작성한 글에서 참여자와 신청자 가져오기


            List<BoardParticipant> boardParticipantList = boardParticipantRepository.findByBoardId(boards);
            List<BoardApplyLeader> boardApplyLeaderList = boardApplyLeaderRepository.findByBoardIdAndNotHide(boards);
            for (BoardApplyLeader boardApplyLeader : boardApplyLeaderList) { //게시판에 신청한 리더 가져오기
                List<ApplyUsers> applyUsersList = boardApplyLeader.getApplyUsers();
                List<User> userList = new ArrayList<>();
                for (ApplyUsers applyUsers : applyUsersList) {  //리더안에 유저들 가져오기
                    userList.add(applyUsers.getUserId());
                }
                List<UserSearchResponseDto> participantsUsers = boardParticipantList.stream()
                        .map(BoardParticipant::getUserId)
                        .map(UserSearchResponseDto::toDto)
                        .collect(Collectors.toList());

                List<UserSearchResponseDto> applyUsers = userList.stream()
                        .map(UserSearchResponseDto::toDto)
                        .collect(Collectors.toList());

                ApplicationStatusResponseDto savedResponseDto =
                        ApplicationStatusResponseDto.toDto(boardApplyLeader.getId(), participantsUsers, applyUsers,
                                boardApplyLeader.getLeaderId().getDepartment(), participantDepartment,
                                boardApplyLeader.getStatus(), boardApplyLeader.getCreatedDate(), boardApplyLeader.getModifiedDate());
                allUsersByLeader.add(savedResponseDto);
            }
        }

//        //1:1 미팅 내역 조회
//        List<MeetingApplyLeader> meetingApplyLeaderList=meetingApplyLeaderRepository.findByMeetingUserId(user);
//        for (MeetingApplyLeader meetingApplyLeader:meetingApplyLeaderList) {
//            List<UserSearchResponseDto> meetingParticipantsUserList=new ArrayList<>();
//            List<UserSearchResponseDto> meetingApplyUserList=new ArrayList<>();
//            UserSearchResponseDto meetingParticipantsUser=UserSearchResponseDto.toDto(meetingApplyLeader.getMeetingUserId());
//            UserSearchResponseDto meetingApplyUser=UserSearchResponseDto.toDto(meetingApplyLeader.getLeaderId());
//            meetingParticipantsUserList.add(meetingParticipantsUser);
//            meetingApplyUserList.add(meetingApplyUser);
//
//            ApplicationStatusResponseDto savedResponseDto =
//                    ApplicationStatusResponseDto.toDto(meetingApplyLeader.getId(), meetingParticipantsUserList, meetingApplyUserList,
//                            meetingApplyLeader.getLeaderId().getDepartment(), participantDepartment,
//                            meetingApplyLeader.getStatus(), meetingApplyLeader.getCreatedDate(), meetingApplyLeader.getModifiedDate());
//            allUsersByLeader.add(savedResponseDto);
//
//        }

        allUsersByLeader.sort(ApplicationStatusResponseDto::compareTo);
        Collections.reverse(allUsersByLeader);
        return allUsersByLeader;
    }

    /**
     * 신청현황
     *
     * @param email
     * @return
     */
    public List<ApplicationStatusResponseDto> applyState(String email) {

        List<ApplicationStatusResponseDto> allUsersByLeader = new ArrayList<>();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        List<BoardApplyLeader> boardApplyLeaderList = boardApplyLeaderRepository.findByLeaderIdOrderByModifiedDateDescCreatedDateDesc(user);

        for (BoardApplyLeader boardApplyLeaders : boardApplyLeaderList) {
            List<BoardParticipant> boardParticipantList = boardParticipantRepository.findByBoardId(
                    boardApplyLeaders.getBoardId());
            List<ApplyUsers> applyUsersList = boardApplyLeaders.getApplyUsers();
            List<User> userList = new ArrayList<>();
            for (ApplyUsers applyUsers : applyUsersList) {
                userList.add(applyUsers.getUserId());
            }
            List<UserSearchResponseDto> participantsUsers = boardParticipantList.stream()
                    .map(BoardParticipant::getUserId)
                    .map(UserSearchResponseDto::toDto)
                    .collect(Collectors.toList());
            List<UserSearchResponseDto> applyUsers = userList.stream()
                    .map(UserSearchResponseDto::toDto)
                    .collect(Collectors.toList());
            ApplicationStatusResponseDto savedResponseDto =
                    ApplicationStatusResponseDto.toDto
                            (boardApplyLeaders.getId(), participantsUsers, applyUsers,
                                    boardApplyLeaders.getLeaderId().getDepartment(),
                                    boardApplyLeaders.getBoardId().getUserId().getDepartment(),
                                    boardApplyLeaders.getStatus(), boardApplyLeaders.getCreatedDate(), boardApplyLeaders.getModifiedDate());
            allUsersByLeader.add(savedResponseDto);
        }

//        //1:1 매칭 조회
//        List<MeetingApplyLeader> meetingApplyLeaderList=meetingApplyLeaderRepository.findByLeaderId(user);
//        for (MeetingApplyLeader meetingApplyLeader:meetingApplyLeaderList) {
//            List<UserSearchResponseDto> meetingParticipantsUserList=new ArrayList<>();
//            List<UserSearchResponseDto> meetingApplyUserList=new ArrayList<>();
//            UserSearchResponseDto meetingParticipantsUser=UserSearchResponseDto.toDto(meetingApplyLeader.getMeetingUserId());
//            UserSearchResponseDto meetingApplyUser=UserSearchResponseDto.toDto(meetingApplyLeader.getLeaderId());
//            meetingParticipantsUserList.add(meetingParticipantsUser);
//            meetingApplyUserList.add(meetingApplyUser);
//
//            ApplicationStatusResponseDto savedResponseDto =
//                    ApplicationStatusResponseDto.toDto(meetingApplyLeader.getId(), meetingParticipantsUserList, meetingApplyUserList,
//                            meetingApplyLeader.getLeaderId().getDepartment(), meetingApplyLeader.getLeaderId().getDepartment(),
//                            meetingApplyLeader.getStatus(), meetingApplyLeader.getCreatedDate(), meetingApplyLeader.getModifiedDate());
//            allUsersByLeader.add(savedResponseDto);
//
//        }
            allUsersByLeader.sort(ApplicationStatusResponseDto::compareTo);
            Collections.reverse(allUsersByLeader);

            return allUsersByLeader;
    }


    /**
     * 내가 쓴 글
     *
     * @param email
     * @return
     */

    // 현재 BoardResponserDto에
    public List<BoardShowAllResponseDto> myBoard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
       // List<Board> board = boardRepository.findByUserId(user);

        List<Board> board = boardRepository.findByUserIdMyboard(user);


        return board.stream()
                .map(BoardShowAllResponseDto::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 거절하기
     *
     * @param id
     * @param email
     * @return String
     */
    @Transactional
    public String refuse(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        BoardApplyLeader boardApplyLeader = boardApplyLeaderRepository.findById(id)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_APPLY));
        if (boardApplyLeader.getBoardId().getUserId() != user) {
            throw new UserHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        boardApplyLeader.setStatus(ApplyStatus.거절);
        boardApplyLeaderRepository.save(boardApplyLeader);
        fcmService.sendMessageTo(boardApplyLeader.getLeaderId(), "과팅신청이 거절되었습니다", user.getDepartment() + " " + user.getNickname() + "님이 과팅을 거절했습니다.","refuse",boardApplyLeader.getId());

        return boardApplyLeader.getId() + "번 신청이 거절되었습니다.";
    }

    @Transactional
    public String cancel(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        BoardApplyLeader boardApplyLeader = boardApplyLeaderRepository.findById(id)
                .orElseThrow();
        if (boardApplyLeader == null) {
            throw new BoardHandler(ErrorStatus.USER_NOT_APPLY);
        }
        if (boardApplyLeader.getLeaderId() != user) {
            throw new BoardHandler(ErrorStatus.USER_NOT_APPLY);
        }
        boardApplyLeaderRepository.delete(boardApplyLeader);
        fcmService.sendMessageTo(boardApplyLeader.getBoardId().getUserId(), "과팅신청자가 과팅을 취소했습니다.", user.getDepartment() + user.getNickname() + "님이 과팅을 취소했습니다.","cancel",id);
        return boardApplyLeader.getBoardId().getUserId().getDepartment() + "학과 신청이 취소되었습니다.";
    }

    @Transactional
    public String accept(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        BoardApplyLeader boardApplyLeader = boardApplyLeaderRepository.findById(id)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_APPLY));
        if (boardApplyLeader.getBoardId().getUserId() != user) {
            throw new BoardHandler(ErrorStatus.USER_NOT_AUTHORITY);
        }
        if (boardApplyLeader.getStatus() == ApplyStatus.승인) {
            throw new BoardHandler(ErrorStatus.ALREADY_SUCCESS_APPLY);
        }
        List<User> applyUserList = boardApplyLeader.getApplyUsers().stream()
                .map(ApplyUsers::getUserId)
                .collect(Collectors.toList());

        List<User> participantUserList = boardApplyLeader.getBoardId().getBoardParticipant().stream()
                .map(BoardParticipant::getUserId)
                .collect(Collectors.toList());
        String applyUserDepartment = boardApplyLeader.getLeaderId().getDepartment();
        String participantUserDepartment = boardApplyLeader.getBoardId().getUserId().getDepartment();
        ChatMemberDto chatMemberDto = ChatMemberDto.toDto(applyUserList.size()+" : "+participantUserList.size(), applyUserDepartment, participantUserDepartment, applyUserList,
                participantUserList);

        ChatRoomResponseDto chatRoomResponseDto=chatRoomService.createChatRoom(chatMemberDto);

        //알림보내기 전체보내기 확인필요
        List<User> notificationUser = new ArrayList<>();
        notificationUser.addAll(chatMemberDto.getApplyUser());
        notificationUser.addAll(chatMemberDto.getParticipantUser());

        fcmService.sendAllMessage(notificationUser, "과팅이 성사되었습니다", chatMemberDto.getApplyUserDepartment() + "와 " + chatMemberDto.getParticipantUserDepartment() + "의 과팅이 성사되어 채팅방이 만들어졌습니다.","chat",chatRoomResponseDto.getId());
        boardApplyLeader.setStatus(ApplyStatus.승인);
        Board board = boardRepository.findById(boardApplyLeader.getBoardId().getId())
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));
        board.closeState();
        boardRepository.save(board);
        boardApplyLeaderRepository.save(boardApplyLeader);

        //다른 과팅 신청자는 거절로 처
        List<BoardApplyLeader> cancelApplyList = boardApplyLeaderRepository.findByBoardIdAndWaiting(boardApplyLeader.getBoardId());
        for (BoardApplyLeader cancelApply:cancelApplyList) {
            if(cancelApply.getId()==boardApplyLeader.getId()){
                continue;
            }
            cancelApply.setStatus(ApplyStatus.거절);
            boardApplyLeaderRepository.save(cancelApply);
            fcmService.sendMessageTo(cancelApply.getLeaderId(), "과팅신청이 거절되었습니다", user.getDepartment() + " " + user.getNickname() + "님이 과팅을 거절했습니다.","cancel",id);
        }

        return "과팅이 성사되었습니다.";
    }

    @Transactional
    public String applyStateHide(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        BoardApplyLeader boardApplyLeader = boardApplyLeaderRepository.findById(id)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_APPLY));
        if (boardApplyLeader.getLeaderId() != user) {
            throw new BoardHandler(ErrorStatus.NOT_HAVE_PERMISSION);
        }
        if (boardApplyLeader.getStatus() == ApplyStatus.대기중) {
            throw new BoardHandler(ErrorStatus.STATUS_VALUE_IS_STRANGE);
        }
        if(boardApplyLeader.getReceiveShowStatus()==ApplyShowStatus.HIDE){
            boardApplyLeaderRepository.delete(boardApplyLeader);
            return "신청한내역이 삭제되었습니다.";
        }
        boardApplyLeaderRepository.updateApplyStateHide(boardApplyLeader);

        return "신청한내역이 삭제되었습니다.";
    }

    @Transactional
    public String receivedStateHide(String email, Long id) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));
        BoardApplyLeader boardApplyLeader = boardApplyLeaderRepository.findById(id)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_APPLY));
        if (boardApplyLeader.getBoardId().getUserId() != user) {
            throw new BoardHandler(ErrorStatus.NOT_HAVE_PERMISSION);
        }
        if (boardApplyLeader.getStatus() == ApplyStatus.대기중) {
            throw new BoardHandler(ErrorStatus.STATUS_VALUE_IS_STRANGE);
        }
        if(boardApplyLeader.getApplyShowStatus()==ApplyShowStatus.HIDE){
            boardApplyLeaderRepository.delete(boardApplyLeader);
            return "신청받은내역이 삭제되었습니다.";
        }
        boardApplyLeaderRepository.updateReceivedStateHide(boardApplyLeader);
        return "신청받은내역이 삭제되었습니다.";
    }
}

