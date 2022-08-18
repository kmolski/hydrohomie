/**
 * MQTT message handlers that facilitate communication with the smart coaster.
 * <p>
 *     The communication flow works like this:
 * <ol>
 *     <li>
 *         Coaster starts, sends {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ConnectedMessage ConnectedMessage}
 *         on the root MQTT topic (e.g. "hydrohomie"), Spring Integration channel "root". This causes the device to be
 *         registered with the application if it isn't already.
 *     </li>
 *     <li>
 *         Web app receives {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ConnectedMessage ConnectedMessage},
 *         sends {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.ListeningMessage ListeningMessage} on the device topic
 *         (e.g. "hydrohomie/device/esp32-000000"), Spring Integration channel "device-in". The coaster uses this information
 *         to set the initial state: base measurement and total volume for the day.
 *     </li>
 *     <li>
 *         Coaster sends {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.BeginMessage BeginMessage},
 *         {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.EndMessage EndMessage} and
 *         {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.DiscardMessage DiscardMessage} on the device topic.
 *         These messages carry information about the base/final measurements and discard actions.
 *     </li>
 *     <li>
 *         Coaster periodically sends {@link pl.kmolski.hydrohomie.mqtt.model.CoasterMessage.HeartbeatMessage HeartbeatMessage} on the device topic.
 *         This is used by the application to update the inactivity information for the coaster.
 *     </li>
 *     <li>
 *         Web app listens for the device topic messages sent by the coaster and executes actions to handle them.
 *         For example, the base measurements can be reset, measurements can be saved to the database, etc.
 *     </li>
 *     <p>
 *         All inbound & outbound messages are encoded as JSON/UTF-8.
 *     </p>
 * </ol>
 * </p>
 */
package pl.kmolski.hydrohomie.mqtt.handler;
