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

import com.testingtech.ttcn.tri.PortFilter;
import com.testingtech.ttcn.tri.extension.PortFilterProvider;

public class CANFilterProvider extends PortFilter implements PortFilterProvider {

	public PortFilter getPortFilter() {
		return this;
	}

	public TriStatus triSAReset() {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triSAReset();
	}

	public TriStatus triExecuteTestcase(TriTestCaseId testCaseId,
			TriPortIdList tsiPorts) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triExecuteTestcase(testCaseId, tsiPorts);
	}

	public TriStatus triMap(TriPortId compPortId, TriPortId tsiPortId) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triMap(compPortId, tsiPortId);
	}

	public TriStatus triMapParam(TriPortId compPortId, TriPortId tsiPortId,
			TriParameterList paramList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triMapParam(compPortId, tsiPortId, paramList);
	}

	public TriStatus triUnmap(TriPortId compPortId, TriPortId tsiPortId) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triUnmap(compPortId, tsiPortId);
	}

	public TriStatus triUnmapParam(TriPortId compPortId, TriPortId tsiPortId,
			TriParameterList paramList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triUnmapParam(compPortId, tsiPortId, paramList);
	}

	public TriStatus triEndTestCase() {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triEndTestCase();
	}

	public TriStatus triSend(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress address, TriMessage sendMessage) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triSend(componentId, tsiPortId, address, sendMessage);
	}

	public TriStatus triSendBC(TriComponentId componentId, TriPortId tsiPortId,
			TriMessage sendMessage) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triSendBC(componentId, tsiPortId, sendMessage);
	}

	public TriStatus triSendMC(TriComponentId componentId, TriPortId tsiPortId,
			TriAddressList addresses, TriMessage sendMessage) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triSendMC(componentId, tsiPortId, addresses, sendMessage);
	}

	public TriStatus triCall(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triCall(componentId, tsiPortId, sutAddress, signatureId,
				parameterList);
	}

	public TriStatus triCallBC(TriComponentId componentId, TriPortId tsiPortId,
			TriSignatureId signatureId, TriParameterList parameterList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triCallBC(componentId, tsiPortId, signatureId,
				parameterList);
	}

	public TriStatus triCallMC(TriComponentId componentId, TriPortId tsiPortId,
			TriAddressList sutAddresses, TriSignatureId signatureId,
			TriParameterList parameterList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triCallMC(componentId, tsiPortId, sutAddresses,
				signatureId, parameterList);
	}

	public TriStatus triRaise(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriException exception) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triRaise(componentId, tsiPortId, sutAddress, signatureId,
				exception);
	}

	public TriStatus triRaiseBC(TriComponentId componentId,
			TriPortId tsiPortId, TriSignatureId signatureId, TriException exc) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triRaiseBC(componentId, tsiPortId, signatureId, exc);
	}

	public TriStatus triRaiseMC(TriComponentId componentId,
			TriPortId tsiPortId, TriAddressList sutAddresses,
			TriSignatureId signatureId, TriException exc) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triRaiseMC(componentId, tsiPortId, sutAddresses,
				signatureId, exc);
	}

	public TriStatus triReply(TriComponentId componentId, TriPortId tsiPortId,
			TriAddress sutAddress, TriSignatureId signatureId,
			TriParameterList parameterList, TriParameter returnValue) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triReply(componentId, tsiPortId, sutAddress, signatureId,
				parameterList, returnValue);
	}

	public TriStatus triReplyBC(TriComponentId componentId,
			TriPortId tsiPortId, TriSignatureId signatureId,
			TriParameterList parameterList, TriParameter returnValue) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triReplyBC(componentId, tsiPortId, signatureId,
				parameterList, returnValue);
	}

	public TriStatus triReplyMC(TriComponentId componentId,
			TriPortId tsiPortId, TriAddressList sutAddresses,
			TriSignatureId signatureId, TriParameterList parameterList,
			TriParameter returnValue) {
		// Contract: ONLY call super if the filter should forward this message/call.
		return super.triReplyMC(componentId, tsiPortId, sutAddresses,
				signatureId, parameterList, returnValue);
	}

	public void triEnqueueCall(TriPortId tsiPortId, TriAddress SUTaddress,
			TriComponentId componentId, TriSignatureId signatureId,
			TriParameterList parameterList) {
		// Contract: ONLY call super if the filter should forward this message/call.
		super.triEnqueueCall(tsiPortId, SUTaddress, componentId, signatureId,
				parameterList);
	}

	public void triEnqueueException(TriPortId tsiPortId, TriAddress sutAddress,
			TriComponentId componentId, TriSignatureId signatureId,
			TriException exception) {
		// Contract: ONLY call super if the filter should forward this message/call.
		super.triEnqueueException(tsiPortId, sutAddress, componentId,
				signatureId, exception);
	}

	public void triEnqueueMsg(TriPortId tsiPortId, TriAddress sutAddress,
			TriComponentId componentId, TriMessage receivedMessage) {
		// Contract: ONLY call super if the filter should forward this message/call.
		super.triEnqueueMsg(tsiPortId, sutAddress, componentId, receivedMessage);
	}

	public void triEnqueueReply(TriPortId tsiPortId, TriAddress address,
			TriComponentId componentId, TriSignatureId signatureId,
			TriParameterList parameterList, TriParameter returnValue) {
		// Contract: ONLY call super if the filter should forward this message/call.
		super.triEnqueueReply(tsiPortId, address, componentId, signatureId,
				parameterList, returnValue);
	}

}
