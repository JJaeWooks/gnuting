package gang.GNUtingBackend.slack.service;

import static com.slack.api.model.block.Blocks.asBlocks;
import static com.slack.api.model.block.Blocks.divider;
import static com.slack.api.model.block.Blocks.header;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.chat.ChatPostMessageRequest;
import com.slack.api.model.block.composition.TextObject;
import gang.GNUtingBackend.board.entity.Board;
import gang.GNUtingBackend.board.repository.BoardRepository;
import gang.GNUtingBackend.exception.handler.BoardHandler;
import gang.GNUtingBackend.exception.handler.SlackHandler;
import gang.GNUtingBackend.exception.handler.UserHandler;
import gang.GNUtingBackend.response.code.status.ErrorStatus;
import gang.GNUtingBackend.slack.dto.BoardReportRequestDto;
import gang.GNUtingBackend.slack.dto.UserReportRequestDto;
import gang.GNUtingBackend.user.domain.User;
import gang.GNUtingBackend.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardReportService {

    @Value(value = "${slack.token}")
    private String token;
    @Value(value = "${slack.channel.monitor}")
    private String channel;

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    public void postReport(String email, BoardReportRequestDto boardReportRequestDto) throws IOException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        Board board = boardRepository.findById(boardReportRequestDto.getBoardId())
                .orElseThrow(() -> new BoardHandler(ErrorStatus.BOARD_NOT_FOUND));

        User boardUser = board.getUserId();

        // Slack 메세지 보내기
        try {
            List<TextObject> textObjects = new ArrayList<>();
            textObjects.add(markdownText("*신고 글 제목:*\n" + board.getTitle()));
            textObjects.add(markdownText("*신고 글 사용자 이름:*\n" + boardUser.getName()));
            textObjects.add(markdownText("*신고 글 사용자 닉네임:*\n" + boardUser.getNickname()));
            textObjects.add(markdownText(
                    "*신고 날짜:*\n" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            textObjects.add(markdownText("*신고 사유:*\n" + boardReportRequestDto.getReportCategory().getReportReason()));
            textObjects.add(markdownText("*신고 내용:*\n" + boardReportRequestDto.getReportReason()));

            MethodsClient methods = Slack.getInstance().methods(token);
            ChatPostMessageRequest request = ChatPostMessageRequest.builder()
                    .channel(channel)
                    .text("신고가 접수되었습니다: " + user.getName() + " - " + boardReportRequestDto.getReportReason())
                    .blocks(asBlocks(
                            header(header -> header.text(plainText(user.getName() + "님이 게시글을 신고하셨습니다!"))),
                            divider(),
                            section(section -> section.fields(textObjects)
                            ))).build();

            methods.chatPostMessage(request);
        } catch (SlackApiException | IOException e) {
            throw new SlackHandler(ErrorStatus.CANNOT_SEND_MESSAGE);
        }
    }
}
