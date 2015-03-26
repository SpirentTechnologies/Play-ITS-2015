package codec;

import org.etsi.ttcn.tci.TciCDProvided;

import com.testingtech.ttcn.extension.CodecProvider;
import com.testingtech.util.plugin.PluginInitException;

import de.tu_berlin.cs.uebb.muttcn.runtime.RB;

public class StringToFloatCodecProvider implements CodecProvider {

	@Override
	public TciCDProvided getCodec(RB RB, String encodingRule)
			throws PluginInitException {
		// TODO Auto-generated method stub
		TciCDProvided codec = new StringToFloatCodec();
		return codec;
	}

}
