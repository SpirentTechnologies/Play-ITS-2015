package codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.etsi.ttcn.tci.FloatValue;
import org.etsi.ttcn.tci.TciTypeClass;
import org.etsi.ttcn.tci.Type;
import org.etsi.ttcn.tci.Value;
import org.etsi.ttcn.tri.TriMessage;

import com.testingtech.ttcn.tri.AbstractCodecPlugin;

public class StringToFloatCodec extends AbstractCodecPlugin {

	@Override
	/**
	 * string to float
	 */
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
			tciErrorReq("Error! Unexpected type class.");
			return null;
		}
	}

	@Override
	/**
	 * float to string
	 */
	public TriMessage encode(Value value) {
		return null;
	}

}
