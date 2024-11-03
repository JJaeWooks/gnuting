package gang.GNUtingBackend.board.controller;

import gang.GNUtingBackend.board.dto.ApplicationStatusResponseDto;
import gang.GNUtingBackend.board.dto.BoardResponseDto;
import gang.GNUtingBackend.board.dto.BoardShowAllResponseDto;
import gang.GNUtingBackend.board.service.ApplicationStatusService;
import gang.GNUtingBackend.response.ApiResponse;
import gang.GNUtingBackend.user.token.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ApplicationStatusController {

    private final ApplicationStatusService applicationStatusService;

    private final TokenProvider tokenProvider;


    //신청 받은 현황
    @GetMapping("/board/applications/receivedstate")
    @Operation(summary = "과팅 신청받은 현황 API", description = "내가 작성한 글에 과팅신청을 받은 현황을 표시합니다.")
    public ResponseEntity<?> receivedState(@RequestHeader("Authorization") String token) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        List<ApplicationStatusResponseDto> userSearchResponseDto = applicationStatusService.receiveState(email);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(userSearchResponseDto));
    }

    //내가 신청한 현황
    @GetMapping("/board/applications/applystate")
    @Operation(summary = "과팅을 신청한 현황 API", description = "내가 다른게시물에 신청한 현황을 표시합니다.")
    public ResponseEntity<?> applyState(@RequestHeader("Authorization") String token) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        List<ApplicationStatusResponseDto> userSearchResponseDto = applicationStatusService.applyState(email);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(userSearchResponseDto));
    }

    //내가쓴글
    @GetMapping("/board/myboard")
    @Operation(summary = "내가 쓴 글 API", description = "내가 작성한 글 보기")
    public ResponseEntity<?> myBoard(@RequestHeader("Authorization") String token) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        List<BoardShowAllResponseDto> myBoards = applicationStatusService.myBoard(email);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(myBoards));
    }

    //승인하기
    @PostMapping("/board/applications/accept/{id}")
    @Operation(summary = "과팅 승인하기 API", description = "과팅이 승인하고 채팅방이 만들어집니다.")
    public ResponseEntity<?> accept(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        String accept = applicationStatusService.accept(email, id);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(accept));
    }

    //거절하기
    @PatchMapping("/board/applications/refuse/{id}")
    @Operation(summary = "과팅 거절하기 API", description = "내 게시물에 신청한 과팅신청을 거절합니다.")
    public ResponseEntity<?> refuse(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        String refuse = applicationStatusService.refuse(id, email);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(refuse));
    }


    //취소하기
    @DeleteMapping("/board/applications/cancel/{id}")
    @Operation(summary = "과팅 신청 취소하기 API", description = "내가 신청했던 게시물의 과팅을 취소합니다")
    public ResponseEntity<?> cancel(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        String cancel = applicationStatusService.cancel(id, email);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(cancel));
    }

    @PatchMapping("/board/applications/applystate/{id}")
    @Operation(summary = "내가 신청한 현황 숨기기 API", description = "내가 신청한 현황을 숨김처리하는 API.")
    public ResponseEntity<?> applyStateHide(@RequestHeader("Authorization") String token,@PathVariable Long id) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        String userSearchResponseDto = applicationStatusService.applyStateHide(email,id);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(userSearchResponseDto));
    }

    @PatchMapping("/board/applications/receivedstate/{id}")
    @Operation(summary = "신청받은 현황 숨기기 API", description = "신청받은 현황을 숨김처리하는 API.")
    public ResponseEntity<?> receivedStateHide(@RequestHeader("Authorization") String token,@PathVariable Long id) {
        String email = tokenProvider.getUserEmail(token.substring(7));
        String userSearchResponseDto = applicationStatusService.receivedStateHide(email,id);
        return ResponseEntity.ok()
                .body(ApiResponse.onSuccess(userSearchResponseDto));
    }



//    //신청받은 현황 삭제
//    @DeleteMapping("/board/applications/receivedstate/{id}")
//    @Operation(summary = "신청받은현황 삭제 API", description = "내가 신청받은 현황이 수락되었을때 내역 삭제")
//    public ResponseEntity<?> deleteReceivedState(@RequestHeader("Authorization") String token, @PathVariable Long id) {
//        String email = tokenProvider.getUserEmail(token.substring(7));
//        String deletedReceivedState = applicationStatusService.deleteReceivedState(id, email);
//        return ResponseEntity.ok()
//                .body(ApiResponse.onSuccess(deletedReceivedState));
//    }


}
