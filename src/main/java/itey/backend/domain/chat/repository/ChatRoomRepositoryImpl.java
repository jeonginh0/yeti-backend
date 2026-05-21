package itey.backend.domain.chat.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.chat.dto.ChatRoomSummary;
import itey.backend.domain.chat.entity.QChatRoom;
import itey.backend.domain.chat.entity.QChatRoomMember;
import itey.backend.domain.chat.entity.QMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QChatRoom r = QChatRoom.chatRoom;
    private static final QChatRoomMember allM = new QChatRoomMember("allM");
    private static final QChatRoomMember myM = new QChatRoomMember("myM");
    private static final QMessage msg = new QMessage("msg");

    @Override
    public List<ChatRoomSummary> findRoomSummariesByUserId(UUID userId) {
        return queryFactory
                .select(Projections.constructor(ChatRoomSummary.class,
                        r,
                        JPAExpressions.select(allM.count())
                                .from(allM)
                                .where(allM.room.eq(r)),
                        JPAExpressions.select(msg.count())
                                .from(msg)
                                .where(msg.room.eq(r)
                                        .and(myM.lastReadAt.isNotNull())
                                        .and(msg.createdAt.gt(myM.lastReadAt)))))
                .from(r)
                .join(myM).on(myM.room.eq(r).and(myM.user.id.eq(userId)))
                .where(r.active.isTrue())
                .orderBy(r.createdAt.desc())
                .fetch();
    }
}
