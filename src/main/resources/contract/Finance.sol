pragma solidity ^0.4.4;
contract Finance{

	struct Company {
		bytes32 company;
		bytes32 add;
		bytes32 property;
		StateType credit;
	}
	struct Receipt {
		bytes32 from;
		bytes32 to;
		uint mount;
		StateType status;
	}
	/*
	0 unknown
	1 crediable
	2 dissable
	3 loan
	4 payback
	*/

	enum StateType { Unknown, Crediable, Disable, Loan, Payback }

	mapping (address => Company) public companys;
	mapping (bytes32 => address) public caddress;
	mapping (bytes32 => Receipt[]) public receipts;

	event borrowE(string from, string to, uint mount);
	event transferE(string from, string to, uint mount);
	event loanE(string a, uint mount);
	event paybackE(string from, string to, uint mount);

	address public master;
	function Finance() {
		master = msg.sender;
		bytes32 n = "masterbank";
		bytes32 a = "bank address";
		bytes32 p = "bank";
		caddress[n] = msg.sender;
		
		companys[msg.sender] = Company({company:n, add:a, property:p,credit:StateType.Crediable});
	}

	function strcom(bytes32 str1, bytes32 str2) returns (bool) {
		for (uint i = 0; i < str1.length; i ++ ) {
			if (str1[i] != str2[i])
				return false;
		}
		return true;
	}
	function toBytes32(string str) returns (bytes32 result) {
		bytes memory tempEmptyStringTest = bytes(str);
    	if (tempEmptyStringTest.length == 0) {
        	return 0x0;
    	}
	    assembly {
	        result := mload(add(str, 32))
	    }
	}

	function addCompany(address a, string n, string ad, string p) {
		require (companys[msg.sender].credit == StateType.Crediable, "you don't have the right");
		
		caddress[toBytes32(n)] = a;
		companys[a] = Company({company:toBytes32(n), add:toBytes32(ad), property:toBytes32(p), StateType.Crediable});
	}

	function addReceipt(string fromstr, string tostr, uint mount) public {
		bytes32 from = toBytes32(fromstr);
		bytes32 to = toBytes32(tostr);
		addReceipt32(from, to, mount, StateType.Unknown);
		emit borrowE(fromstr, tostr, mount);
	}

	function payBack(string fromstr, string tostr, uint mount) public {
		bytes32 from = toBytes32(fromstr);
		bytes32 to = toBytes32(tostr);
		payBack32(from, to, mount);
		emit paybackE(fromstr, tostr, mount);
	}

	function transferMount(string fromstr, string tostr, uint mount) public {
		bytes32 from = toBytes32(fromstr);
		bytes32 to = toBytes32(tostr);
		transferMount32(from, to, mount);
		emit transferE(fromstr, tostr, mount);
	}
	function loan(string tostr) public {
		bytes32 to = toBytes32(tostr);
		emit loanE(tostr, loan32(to));
	}

	function addReceipt32(bytes32 from, bytes32 to, uint mount, StateType status) {
		if (companys[caddress[to]].credit == StateType.Crediable && companys[msg.sender].credit == StateType.Crediable)
			status = StateType.Crediable;
		receipts[from].push(Receipt({
						from: from,
						to:  to,
						mount: mount,
						status: status
					}));
	}

	function transferMount32(bytes32 from, bytes32 to, uint mount) {
		uint tem = mount;
		for (uint i = 0; i < receipts[from].length; i ++ ) {
			if (receipts[from][i].mount <= tem && (receipts[from][i].status == StateType.Crediable
				|| receipts[from][i].status == StateType.Unknown)) {
				tem -= receipts[from][i].mount;
				addReceipt32(to, receipts[from][i].to, receipts[from][i].mount, receipts[from][i].status);
				receipts[from][i].status = StateType.Disable;
				receipts[from][i].mount = 0;
			} else {
				addReceipt32(to, receipts[from][i].to, tem, receipts[from][i].status);
				receipts[from][i].mount -= tem;
				tem = 0;
			}
			if (tem == 0)
				break;
		}
	}
	
	function loan32(bytes32 to) returns (uint){
		bytes32 from = "bank";
		uint mount = 0;
		for (uint i = 0; i < receipts[to].length; i ++ ) {
			if (receipts[to][i].status == StateType.Crediable) {
				receipts[to][i].status = StateType.Disable;
				mount += receipts[to][i].mount;
				addReceipt32(from, to, mount, StateType.Loan);
			}
		}
		return mount;
	}
	function payBack32(bytes32 from, bytes32 to, uint mount) {
		uint tem = mount;
		for (uint i = 0; i < receipts[from].length; i ++ ) {
			if (strcom(receipts[from][i].to, to) && (receipts[from][i].status == StateType.Crediable 
				|| receipts[from][i].status == StateType.Unknown)) {
				if (receipts[from][i].mount <= tem) {
					tem -= receipts[from][i].mount;
					receipts[from][i].status = StateType.Payback;
					receipts[from][i].mount = 0;
				} else {
					receipts[from][i].mount -= tem;
					tem = 0;
				}
			}
			if (tem == 0)
				break;
		}
	}
}