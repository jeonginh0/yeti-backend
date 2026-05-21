package itey.backend.domain.chat.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import itey.backend.domain.chat.entity.Message;
import itey.backend.domain.chat.entity.QMessage;
import itey.backend.domain.user.entity.QUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QMessage m = QMessage.message;
    private static final QMessage replyTo = new QMessage("replyTo");
    private static final QUser sender = new QUser("sender");

    @Override
    public List<Message> findBeforeWithSender(UUID roomId, LocalDateTime before, int size) {
        return queryFactory
                .selectFrom(m)
                .join(m.sender, sender).fetchJoin()
                .leftJoin(m.replyTo, replyTo).fetchJoin()
                .where(m.room.id.eq(roomId).and(m.createdAt.lt(before)))
                .orderBy(m.createdAt.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<Message> findLatestWithSender(UUID roomId, int size) {
        return queryFactory
                .selectFrom(m)
                .join(m.sender, sender).fetchJoin()
                .leftJoin(m.replyTo, replyTo).fetchJoin()
                .where(m.room.id.eq(roomId))
                .orderBy(m.createdAt.desc())
                .limit(size)
                .fetch();
    }
}
