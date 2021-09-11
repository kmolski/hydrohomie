package pl.kmolski.hydrohomie.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;

@JsonTypeInfo(use = NAME, property = "type")
@JsonSubTypes({
        @Type(value = Message.ConnectedMessage.class, name = "connected"),
        @Type(value = Message.HeartbeatMessage.class, name = "heartbeat"),
        @Type(value = Message.ListeningMessage.class, name = "listening"),
        @Type(value = Message.BeginMessage.class, name = "begin"),
        @Type(value = Message.EndMessage.class, name = "end"),
        @Type(value = Message.CancelMessage.class, name = "cancel")
})
public sealed interface Message {

    record ConnectedMessage(String device) implements Message {}
    record ListeningMessage(String device) implements Message {}
    record HeartbeatMessage(String device, float load) implements Message {}

    record BeginMessage(String device, float load) implements Message {}
    record EndMessage(String device, float volume) implements Message {}
    record CancelMessage(String device) implements Message {}
}
