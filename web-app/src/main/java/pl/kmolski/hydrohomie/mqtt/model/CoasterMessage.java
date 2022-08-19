package pl.kmolski.hydrohomie.mqtt.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.kmolski.hydrohomie.coaster.model.Coaster;
import pl.kmolski.hydrohomie.coaster.model.Measurement;
import pl.kmolski.hydrohomie.coaster.service.CoasterService;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.*;

/**
 * Message produced/consumed by the coaster. Handled by the
 * {@link pl.kmolski.hydrohomie.mqtt.handler.RootTopicHandler RootTopicHandler}
 * and {@link pl.kmolski.hydrohomie.mqtt.handler.DeviceTopicHandler DeviceTopicHandler}
 */
@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes({
        @Type(value = ConnectedMessage.class, name = "connected"),
        @Type(value = ListeningMessage.class, name = "listening"),
        @Type(value = HeartbeatMessage.class, name = "heartbeat"),
        @Type(value = BeginMessage.class, name = "begin"),
        @Type(value = EndMessage.class, name = "end"),
        @Type(value = DiscardMessage.class, name = "discard")
})
public sealed interface CoasterMessage {

    /**
     * Sent by the coaster after connecting to the MQTT broker.
     * Results in a {@link ListeningMessage} response from the backend app.
     *
     * @param device the device ID
     */
    record ConnectedMessage(String device) implements CoasterMessage {}

    /**
     * Sent by the backend, includes the initial load value and total volume.
     *
     * @param device the device ID
     * @param initLoad the initial load value (in grams)
     * @param initTotal the initial total volume (in ml)
     */
    record ListeningMessage(String device, Float initLoad, float initTotal) implements CoasterMessage {}

    /**
     * Common interface for messages on the device topic, sent by the device to the backend application.
     */
    sealed interface IncomingDeviceTopicMessage extends CoasterMessage {
        /**
         * Handle the incoming message using the provided state management service.
         *
         * @param coasterService the coaster state management service
         * @param now the current time
         * @return the coaster entity
         */
        Mono<Coaster> handle(CoasterService coasterService, Instant now);
    }

    /**
     * Send periodically by the coaster with information about the inactivity time.
     *
     * @param device the device ID
     * @param inactiveSeconds the amount of seconds since last activity
     */
    record HeartbeatMessage(String device, int inactiveSeconds) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.updateCoasterInactivity(device, inactiveSeconds, now);
        }
    }

    /**
     * Sent after the first weight measurement is made by the coaster.
     *
     * @param device the device ID
     * @param load the load value in grams
     */
    record BeginMessage(String device, float load) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.updateCoasterInitLoad(device, load, now);
        }
    }

    /**
     * Sent after the final volume measurement is made by the coaster.
     *
     * @param device the device ID
     * @param volume the volume value in milliliters
     */
    record EndMessage(String device, float volume) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            var measurement = new Measurement(null, device, volume, now);
            return coasterService.createMeasurement(device, measurement, now);
        }
    }

    /**
     * Sent after the initial weight measurement is discarded by the coaster.
     *
     * @param device the device ID
     */
    record DiscardMessage(String device) implements IncomingDeviceTopicMessage {
        @Override
        public Mono<Coaster> handle(CoasterService coasterService, Instant now) {
            return coasterService.resetCoasterState(device, now);
        }
    }
}
