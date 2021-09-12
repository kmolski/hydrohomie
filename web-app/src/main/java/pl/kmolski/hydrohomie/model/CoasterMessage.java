package pl.kmolski.hydrohomie.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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

    sealed interface DeviceTopicMessage extends CoasterMessage {}
    record ListeningMessage(String device, float initTotal) implements DeviceTopicMessage {}
    record HeartbeatMessage(String device, float load, int inactiveSeconds) implements DeviceTopicMessage {}

    record BeginMessage(String device, float load) implements DeviceTopicMessage {}
    record EndMessage(String device, float volume) implements DeviceTopicMessage {}
    record DiscardMessage(String device) implements DeviceTopicMessage {}
}
