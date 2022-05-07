package pl.kmolski.hydrohomie.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.kmolski.hydrohomie.service.CoasterService;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static pl.kmolski.hydrohomie.model.CoasterMessage.*;

@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes({
        @Type(value = ConnectedMessage.class, name = "connected"),
        @Type(value = HeartbeatMessage.class, name = "heartbeat"),
        @Type(value = ListeningMessage.class, name = "listening"),
        @Type(value = BeginMessage.class, name = "begin"),
        @Type(value = EndMessage.class, name = "end"),
        @Type(value = DiscardMessage.class, name = "discard")
})
public sealed interface CoasterMessage {

    record ConnectedMessage(String device) implements CoasterMessage {}
    record ListeningMessage(String device, Float initLoad, float initTotal) implements CoasterMessage {}

    sealed interface IncomingDeviceTopicMessage extends CoasterMessage {
        Mono<Coaster> handle(CoasterService coasterService, Instant now);
    }

    record HeartbeatMessage(String device, int inactiveSeconds) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.updateCoasterInactivity(device, inactiveSeconds, now);
        }
    }

    record BeginMessage(String device, float load) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.updateCoasterInitLoad(device, load, now);
        }
    }

    record EndMessage(String device, float volume) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            var measurement = new Measurement(null, device, volume, now);
            return coasterService.createMeasurement(device, measurement, now);
        }
    }

    record DiscardMessage(String device) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.resetCoasterState(device, now);
        }
    }
}
