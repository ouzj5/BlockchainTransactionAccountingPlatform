pragma solidity ^0.4.4;
contract Finance{

	struct Company {
		string name;
		string add;
		string property;
		StateType credit;
		address addr;
	}
	struct Receipt {
		string from;
		string to;
		address fromadd;
		address toadd;
		uint origin;
		uint mount;
		StateType status;
	}
	/*
	0 unknown
	1 crediable
	2 dissable
	3 loan
	4 payback
	5 bank
	*/

	enum StateType { Unknown, Crediable, Disable, Loan, Payback, Bank}

	mapping (address => uint) public companysMap;
	Company[] public companys;
    Receipt[] public receipts;
    uint public r_num;
    uint public c_num;
	event borrowE(string from, string to, uint mount);
	event transferE(string from, string to, uint mount);
	event loanE(string a, uint mount);
	event paybackE(string from, string to, uint mount);

	address public master;
	function Finance() {
		r_num = 0;
		c_num = 2;
		master = msg.sender;
		companysMap[msg.sender] = 1;
		companys.push(Company({name:"flag", add:"flag", property:"flag",credit:StateType.Disable, addr:msg.sender}));
		companys.push(Company({name:"masterbank", add:"bank address", property:"bank",credit:StateType.Bank, addr:msg.sender}));
	}
	function strcom(string str1, string str2) returns (bool) {
		for (uint i = 0; i < bytes(str1).length; i ++ ) {
			if (bytes(str1)[i] != bytes(str2)[i])
				return false;
		}
		return true;
	}

	function addCrediableCompany(address fa, address a, string n, string ad, string p, StateType state) public returns (bool){
		if  (companys[companysMap[fa]].credit != StateType.Bank)
			return false;
		if  (companys[companysMap[msg.sender]].credit != StateType.Bank)
			return false;
        uint len = companys.length;
        c_num = c_num + 1;
        companysMap[a] = len;
        companys.push(Company({name:n,add:ad, property:p, credit:state, addr:a}));
        return true;
	}
	function addUnknowCompany(address a, string n, string ad, string p) public{
        uint len = companys.length;
        c_num = c_num + 1;
        companysMap[a] = len;
        companys.push(Company({name:n,add:ad, property:p,credit:StateType.Unknown, addr:a}));
	}
	function modifyCompany(address fa, address a) public returns (bool){
		if  (companys[companysMap[fa]].credit != StateType.Bank)
			return false;
		if  (companys[companysMap[msg.sender]].credit != StateType.Bank)
			return false;
		if (companys[companysMap[a]].credit == StateType.Unknown) {
			companys[companysMap[a]].credit = StateType.Crediable;
		} else {
			companys[companysMap[a]].credit = StateType.Unknown;
		}
		for (uint i = 0; i < receipts.length; i ++ ) {
			if (strcom(receipts[i].to, companys[companysMap[a]].name)) {
				receipts[i].status = companys[companysMap[a]].credit;
			}
		}
		return true;
	}
	function delCompany(address fa, address a) public returns (bool) {
		if (a == master)
			return false;
		if  (companys[companysMap[fa]].credit != StateType.Bank)
			return false;
		if  (companys[companysMap[msg.sender]].credit != StateType.Bank)
			return false;
		c_num = c_num - 1;
		uint del_index = companysMap[a];
		companys[del_index] = companys[c_num];
		companysMap[companys[c_num].addr] = del_index;
		companysMap[a] = 0;
		companys.length = c_num;
		return true;
	}
	function brow(address from, address to, uint mount) public {
		StateType status = StateType.Unknown;
		if (companys[companysMap[to]].credit == StateType.Crediable && companys[companysMap[msg.sender]].credit == StateType.Bank)
			status = StateType.Crediable;
		addReceipt(from, to, mount, status);
		emit borrowE(companys[companysMap[from]].name, companys[companysMap[to]].name, mount);
	}
	function addReceipt(address from, address to, uint mount, StateType status) {
		r_num = r_num + 1;
		receipts.push(Receipt({
						from: companys[companysMap[from]].name,
						to:  companys[companysMap[to]].name,
						fromadd: from,
						toadd: to,
						origin: mount,
						mount: mount,
						status: status
					}));
	}

	function payBack(address from, address to, uint mount) public returns (uint){
		uint tem = mount;
		for (uint i = 0; i < receipts.length; i ++ ) {
			if (strcom(receipts[i].to, companys[companysMap[to]].name)
			    &&strcom(receipts[i].from, companys[companysMap[from]].name)
			    &&(receipts[i].status == StateType.Crediable
				|| receipts[i].status == StateType.Unknown)) {
				if (receipts[i].mount <= tem) {
					tem -= receipts[i].mount;
					receipts[i].status = StateType.Payback;
					receipts[i].mount = 0;
				} else {
					receipts[i].mount -= tem;
					tem = 0;
				}
			}
			if (tem == 0)
				break;
		}
		emit paybackE(companys[companysMap[from]].name, companys[companysMap[to]].name, mount - tem);
		return mount - tem;
	}

	function transferMount(address from, address to, uint mount) public returns (uint){
		uint tem = mount;
		for (uint i = 0; i < receipts.length; i ++ ) {
			if (strcom(receipts[i].from, companys[companysMap[from]].name)
				&&(receipts[i].status == StateType.Crediable
				|| receipts[i].status == StateType.Unknown)){
				if (receipts[i].mount <= tem) {
					tem -= receipts[i].mount;
					addReceipt(companys[companysMap[to]].addr, receipts[i].toadd, receipts[i].mount, receipts[i].status);
					receipts[i].status = StateType.Disable;
					receipts[i].mount = 0;
				} else {
					addReceipt(companys[companysMap[to]].addr, receipts[i].toadd, tem, receipts[i].status);
					receipts[i].mount -= tem;
					tem = 0;
				}
			}
			if (tem == 0)
				break;
		}
		emit transferE(companys[companysMap[from]].name, companys[companysMap[to]].name, mount - tem);
		return mount - tem;
	}

	function getLoan(address to) public returns (uint){
		uint mount = 0;
		for (uint i = 0; i < receipts.length; i ++ ) {
			if (strcom(receipts[i].from, companys[companysMap[to]].name)
				&& receipts[i].status == StateType.Crediable) {
					mount += receipts[i].mount;
			}
		}
		return mount;
	}

	function loan(address to, uint mount) public returns (uint){
		uint origin = mount;
		for (uint i = 0; i < receipts.length; i ++ ) {
			if (strcom(receipts[i].from, companys[companysMap[to]].name)
				&& receipts[i].status == StateType.Crediable) {
				if (mount >= receipts[i].mount){
					receipts[i].status = StateType.Disable;
					mount -= receipts[i].mount;
				}
				else {
					receipts[i].mount -= mount;
					mount = 0;
				}
			}
			if (mount == 0)
				break;
		}
		addReceipt(master, companys[companysMap[to]].addr, origin - mount, StateType.Loan);
		emit loanE(companys[companysMap[to]].name, origin - mount);
		return origin - mount;
	}
}
