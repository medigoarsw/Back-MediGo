package edu.escuelaing.arsw.medigo.auction.infrastructure.adapter.out;

import edu.escuelaing.arsw.medigo.auction.domain.model.AuctionEvent;
import edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto.AuctionPriceUpdateMessage;
import edu.escuelaing.arsw.medigo.auction.infrastructure.websocket.dto.BidPlacedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebSocketAuctionEventPublisher - Publicación STOMP multi-topic")
class WebSocketAuctionEventPublisherTest {

    @Mock
    SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    WebSocketAuctionEventPublisher publisher;

    private static final Long AUCTION_ID = 42L;
    private static final LocalDateTime NOW = LocalDateTime.now();

    // ── BID_PLACED ────────────────────────────────────────────────────────

    @Test
    @DisplayName("BID_PLACED publica en los tres topics correctos")
    void bidPlaced_publishesToThreeTopics() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.BID_PLACED, BigDecimal.valueOf(7000));

        publisher.publish(AUCTION_ID, event);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auctions"), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID + "/bids"), any(BidPlacedMessage.class));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("BID_PLACED: AuctionPriceUpdateMessage contiene currentPrice correcto")
    void bidPlaced_priceUpdatePayload_isCorrect() {
        BigDecimal amount = BigDecimal.valueOf(8500);
        AuctionEvent event = buildEvent(AuctionEvent.EventType.BID_PLACED, amount);

        publisher.publish(AUCTION_ID, event);

        ArgumentCaptor<AuctionPriceUpdateMessage> captor =
                ArgumentCaptor.forClass(AuctionPriceUpdateMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID), captor.capture());

        AuctionPriceUpdateMessage msg = captor.getValue();
        assertThat(msg.eventType()).isEqualTo("BID_PLACED");
        assertThat(msg.auctionId()).isEqualTo(AUCTION_ID);
        assertThat(msg.currentPrice()).isEqualByComparingTo(amount);
        assertThat(msg.leaderName()).isEqualTo("Maria");
        assertThat(msg.leaderId()).isEqualTo(5L);
        assertThat(msg.timestamp()).isEqualTo(NOW.toString());
    }

    @Test
    @DisplayName("BID_PLACED: BidPlacedMessage contiene datos de la puja")
    void bidPlaced_bidPayload_isCorrect() {
        BigDecimal amount = BigDecimal.valueOf(9000);
        AuctionEvent event = buildEvent(AuctionEvent.EventType.BID_PLACED, amount);

        publisher.publish(AUCTION_ID, event);

        ArgumentCaptor<BidPlacedMessage> captor = ArgumentCaptor.forClass(BidPlacedMessage.class);
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID + "/bids"), captor.capture());

        BidPlacedMessage msg = captor.getValue();
        assertThat(msg.auctionId()).isEqualTo(AUCTION_ID);
        assertThat(msg.amount()).isEqualByComparingTo(amount);
        assertThat(msg.bidderName()).isEqualTo("Maria");
        assertThat(msg.bidderId()).isEqualTo(5L);
        assertThat(msg.placedAt()).isEqualTo(NOW.toString());
    }

    // ── Otros tipos de evento ─────────────────────────────────────────────

    @Test
    @DisplayName("AUCTION_CLOSED publica solo en dos topics (no en /bids)")
    void auctionClosed_publishesToTwoTopicsOnly() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.AUCTION_CLOSED, BigDecimal.valueOf(12000));

        publisher.publish(AUCTION_ID, event);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auctions"), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate, never()).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID + "/bids"), any(BidPlacedMessage.class));
        verifyNoMoreInteractions(messagingTemplate);
    }

    @Test
    @DisplayName("AUCTION_STARTED publica solo en dos topics (no en /bids)")
    void auctionStarted_publishesToTwoTopicsOnly() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.AUCTION_STARTED, null);

        publisher.publish(AUCTION_ID, event);

        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(BidPlacedMessage.class));
    }

    @Test
    @DisplayName("WINNER_ADJUDICATED publica solo en dos topics (no en /bids)")
    void winnerAdjudicated_publishesToTwoTopicsOnly() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.WINNER_ADJUDICATED, BigDecimal.valueOf(15000));

        publisher.publish(AUCTION_ID, event);

        verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate, never()).convertAndSend(anyString(), any(BidPlacedMessage.class));
    }

    // ── Resiliencia ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Excepción en messagingTemplate NO se propaga al llamador")
    void publish_messagingTemplateThrows_doesNotPropagate() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.BID_PLACED, BigDecimal.valueOf(6000));
        doThrow(new RuntimeException("Broker caído")).when(messagingTemplate)
                .convertAndSend(anyString(), any(Object.class));

        assertThatCode(() -> publisher.publish(AUCTION_ID, event))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Si falla un topic, los demás se siguen intentando publicar")
    void publish_firstTopicFails_subsequentTopicsStillAttempted() {
        AuctionEvent event = buildEvent(AuctionEvent.EventType.BID_PLACED, BigDecimal.valueOf(6000));

        doThrow(new RuntimeException("error-primer-topic"))
                .when(messagingTemplate)
                .convertAndSend(eq("/topic/auction/" + AUCTION_ID), any(AuctionPriceUpdateMessage.class));

        publisher.publish(AUCTION_ID, event);

        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auctions"), any(AuctionPriceUpdateMessage.class));
        verify(messagingTemplate).convertAndSend(
                eq("/topic/auction/" + AUCTION_ID + "/bids"), any(BidPlacedMessage.class));
    }

    // ── Helper ───────────────────────────────────────────────────────────

    private AuctionEvent buildEvent(AuctionEvent.EventType type, BigDecimal amount) {
        return AuctionEvent.builder()
                .type(type)
                .auctionId(AUCTION_ID)
                .currentAmount(amount)
                .leaderName("Maria")
                .leaderId(5L)
                .timestamp(NOW)
                .message("Puja de prueba")
                .build();
    }
}
