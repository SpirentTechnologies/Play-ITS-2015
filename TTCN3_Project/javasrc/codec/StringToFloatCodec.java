package codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriMessage;

import com.testingtech.ttcn.tri.AbstractCodecPlugin;

/**
 * @author Christian Damm, Sascha Kretzschmann
 * 
 *         This is a simple codec to cast charstring to float. It is used as
 *         coding-rule at {@link Car2X_Templates.interval} to realize a template
 *         matching. The reason of this class is that the information we receive
 *         from the CAN filter are all of type charstring. Thus, we can simply
 *         cast the received charstrings to float and match them with an
 *         interval template with the help of this codec.
 *         
 * @see ttcn3/Car2X_Templates
 */
public class StringToFloatCodec extends AbstractCodecPlugin {

	/**
	 * Override the decode() method from class AbstractCodecPlugin. The decode()
	 * method is invoked when the receive() method is executed at a port. It
	 * encodes a charstring.
	 * 
	 * @see com.testingtech.ttcn.tri.AbstractCodecPlugin
	 */
	@Override
	public Value decode(TriMessage message, Type decodingHypothesis) {
		// store type class of encoded message
		int typeClass = decodingHypothesis.getTypeClass();

		// check whether incoming message is of type charstring
		if (typeClass == (int) TciTypeClass.CHARSTRING) {
			// store encoded message as byte array
			byte[] bytes = message.getEncodedMessage();

			// convert byte array to float value
			float floatValue = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).getFloat();

			// prepare the retrun value
			FloatValue result = (FloatValue) decodingHypothesis.newInstance();
			result.setFloat(floatValue);

			return result;
		} else {
			// should not be reached, signal error
			tciErrorReq("Error! Unexpected type class while encoding.");
			return null;
		}
	}

	/**
	 * Override the encode() method from class AbstractCodecPlugin. The encode()
	 * method is invoked when the send() method is executed at a port. It
	 * encodes a float.
	 * 
	 * @see com.testingtech.ttcn.tri.AbstractCodecPlugin
	 */
	@Override
	public TriMessage encode(Value value) {
		float floatValue;

		// get the type class of the received value
		int typeClass = value.getType().getTypeClass();

		// received type class must be float
		if (typeClass == (int) TciTypeClass.FLOAT) {
			// get the actual value as float
			floatValue = ((FloatValue) value).getFloat();
			// convert float to byte array that we want to send
			byte[] bytes = ByteBuffer.allocate(4).putFloat(floatValue).array();

			return buildTriMessage(value, bytes);
		} else {
			// should not be reached, signal error
			tciErrorReq("Error! Unexpected type class while decoding.");
			return null;
		}
	}

	/**
	 * Auxiliary method to wrap a byte array into a TriMessage.
	 * 
	 * @see org.etsi.ttcn.tri.TriMessage
	 * @param value
	 *            The value to encode.
	 * @param bytes
	 *            The bytes that should wrap into a TriMessage.
	 * @return triMessage
	 */
	private TriMessage buildTriMessage(Value value, byte[] bytes) {
		TriMessage triMessage = encode(value);
		triMessage.setEncodedMessage(bytes);
		return triMessage;
	}
}
