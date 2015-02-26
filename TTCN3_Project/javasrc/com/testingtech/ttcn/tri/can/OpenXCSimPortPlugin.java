package com.testingtech.ttcn.tri.can;

import org.etsi.ttcn.tri.TriAddress;
import org.etsi.ttcn.tri.TriAddressList;
import org.etsi.ttcn.tri.TriComponentId;
import org.etsi.ttcn.tri.TriException;
import org.etsi.ttcn.tri.TriMessage;
import org.etsi.ttcn.tri.TriParameter;
import org.etsi.ttcn.tri.TriParameterList;
import org.etsi.ttcn.tri.TriPortId;
import org.etsi.ttcn.tri.TriPortIdList;
import org.etsi.ttcn.tri.TriSignatureId;
import org.etsi.ttcn.tri.TriStatus;
import org.etsi.ttcn.tri.TriTestCaseId;

import com.testingtech.ttcn.tri.AbstractSA;
import com.testingtech.ttcn.tri.ISAPlugin;
import com.testingtech.ttcn.tri.TriStatusImpl;
import com.testingtech.ttcn.tri.extension.PortPluginProvider;

public class OpenXCSimPortPlugin extends AbstractSA implements
		PortPluginProvider {

	public ISAPlugin getPortPlugin() {
		return this;
	}

	@Override
	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress address, TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triSendBC(TriComponentId componentId, TriPortId tsiPortId,
			TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triSendMC(TriComponentId componentId, TriPortId tsiPortId,
			TriAddressList addresses, TriMessage sendMessage) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triCallBC(TriComponentId componentId, TriPortId tsiPortId,
			TriSignatureId signatureId, TriParameterList parameterList) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triCallMC(TriComponentId componentId, TriPortId tsiPortId,
			TriAddressList sutAddresses, TriSignatureId signatureId,
			TriParameterList parameterList) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triReply(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList, TriParameter returnValue) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triReplyBC(TriComponentId componentId,
			TriPortId tsiPortId, TriSignatureId signatureId,
			TriParameterList parameterList, TriParameter returnValue) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triReplyMC(TriComponentId componentId,
			TriPortId tsiPortId, TriAddressList sutAddresses,
			TriSignatureId signatureId, TriParameterList parameterList,
			TriParameter returnValue) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triRaise(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriException exception) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triRaiseBC(TriComponentId componentId,
			TriPortId tsiPortId, TriSignatureId signatureId, TriException exc) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triRaiseMC(TriComponentId componentId,
			TriPortId tsiPortId, TriAddressList sutAddresses,
			TriSignatureId signatureId, TriException exc) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triSAReset() {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triExecuteTestcase(TriTestCaseId testCaseId,
			TriPortIdList tsiPorts) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triMap(TriPortId compPortId, TriPortId tsiPortId) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triUnmap(TriPortId compPortId, TriPortId tsiPortId) {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

	@Override
	public TriStatus triEndTestCase() {
		// TODO Auto-generated method stub
		return TriStatusImpl.OK;
	}

}
