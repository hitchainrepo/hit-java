pragma solidity 0.5.6;

/**
 * @author Tyler Chen
 * @title HitRepository
 * @dev The contract to save the repository informations, last update: 2019-06-29, version: 0.0.3.
 */
contract HitRepository {
    /**
     * @dev the event for use to find out the transaction is success or fail.
     */
    event Success(bool value);
    /**
     * @dev the contract/repository owner.
     */
    address public owner;

    mapping(uint256 => uint256) public hash_index;
    mapping(uint256 => string ) public index_name;
    mapping(uint256 => string ) public index_url;
    mapping(uint256 => mapping(uint256 => mapping(address => bool))) public index_type_address;
    mapping(uint256 => mapping(uint256 => mapping(uint256 => address))) public index_type_count_address;
    mapping(uint256 => mapping(uint256 => mapping(uint256 => string))) public index_type_count_string;
    mapping(uint256 => mapping(uint256 => uint256)) public index_type_count;
    mapping(uint256 => mapping(uint256 => bool)) public index_type_enable;
    uint256 public count;

    uint256 public constant TYPE_DELEGATOR = uint256(1);
    uint256 public constant TYPE_MEMBER = uint256(2);
    uint256 public constant TYPE_PR_MEMBER = uint256(3);
    uint256 public constant TYPE_PR_AUTH = uint256(4);
    uint256 public constant TYPE_PR_COMM = uint256(5);

    /**
     * @dev create the contract with the contract owner.
     */
    constructor() public {
        owner = msg.sender;
        emit Success(true);
    }

    function addRepository(string memory _name) public {
        uint256 hash = uint256(keccak256(bytes(_name)));
        count = count + 1;
        hash_index[hash] = count;
        index_name[count] = _name;
    }

    function updateName(string memory _name, string memory _newName) public {
        uint256 hash = uint256(keccak256(bytes(_name)));
        uint256 index = hash_index[hash];
        uint256 hashNew = uint256(keccak256(bytes(_newName)));
        delete hash_index[hash];
        hash_index[hashNew] = index;
    }

    function updateUrl(string memory _name, string memory _url) public {
        uint256 hash = uint256(keccak256(bytes(_name)));
        uint256 index = hash_index[hash];
        index_url[index] = _url;
    }

    function updateUrlByIndex(uint256 _index, string memory _url) public {
        index_url[_index] = _url;
    }

    function addDelegator(string memory _name, address _address) public {
        addTypeAddress(_name, _address, TYPE_DELEGATOR);
    }

    function addDelegatorByIndex(uint256 _index, address _address) public {
        addTypeAddressByIndex(_index, _address, TYPE_DELEGATOR);
    }

    function removeDelegator(string memory _name, address _address) public {
        removeTypeAddress(_name, _address, TYPE_DELEGATOR);
    }

    function removeDelegatorByIndex(uint256 _index, address _address) public {
        removeTypeAddressByIndex(_index, _address, TYPE_DELEGATOR);
    }

    function addMember(string memory _name, address _address) public {
        addTypeAddress(_name, _address, TYPE_MEMBER);
    }

    function addMemberByIndex(uint256 _index, address _address) public {
        addTypeAddressByIndex(_index, _address, TYPE_MEMBER);
    }

    function removeMember(string memory _name, address _address) public {
        removeTypeAddress(_name, _address, TYPE_MEMBER);
    }

    function removeMemberByIndex(uint256 _index, address _address) public {
        removeTypeAddressByIndex(_index, _address, TYPE_MEMBER);
    }


    function addPrMember(string memory _name, address _address) public {
        addTypeAddress(_name, _address, TYPE_PR_MEMBER);
    }

    function addPrMemberByIndex(uint256 _index, address _address) public {
        addTypeAddressByIndex(_index, _address, TYPE_PR_MEMBER);
    }

    function removePrMember(string memory _name, address _address) public {
        removeTypeAddress(_name, _address, TYPE_PR_MEMBER);
    }

    function removePrMemberByIndex(uint256 _index, address _address) public {
        removeTypeAddressByIndex(_index, _address, TYPE_PR_MEMBER);
    }

    function addTypeAddress(string memory _name, address _address, uint256 _type) private {
        uint256 hash = uint256(keccak256(bytes(_name)));
        uint256 index = hash_index[hash];
        index_type_address[index][_type][_address] = true;
        uint256 typeCount = index_type_count[index][_type] + 1;
        index_type_count_address[index][_type][typeCount] = _address;
        index_type_count[index][_type] = typeCount;
    }

    function addTypeAddressByIndex(uint256 _index, address _address, uint256 _type) private {
        index_type_address[_index][_type][_address] = true;
        uint256 typeCount = index_type_count[_index][_type] + 1;
        index_type_count_address[_index][_type][typeCount] = _address;
        index_type_count[_index][_type] = typeCount;
    }

    function removeTypeAddress(string memory _name, address _address, uint256 _type) private {
        uint256 hash = uint256(keccak256(bytes(_name)));
        uint256 index = hash_index[hash];
        index_type_address[index][_type][_address] = false;
    }

    function removeTypeAddressByIndex(uint256 _index, address _address, uint256 _type) private {
        index_type_address[_index][_type][_address] = false;
    }

    function addTypeString(string memory _name, string memory _string, uint256 _type) private {
        uint256 hash = uint256(keccak256(bytes(_name)));
        uint256 index = hash_index[hash];
        uint256 typeCount = index_type_count[index][_type] + 1;
        index_type_count_string[index][_type][typeCount] = _string;
        index_type_count[index][_type] = typeCount;
    }

    function addTypeStringByIndex(uint256 _index, string memory _string, uint256 _type) private {
        uint256 typeCount = index_type_count[_index][_type] + 1;
        index_type_count_string[_index][_type][typeCount] = _string;
        index_type_count[_index][_type] = typeCount;
    }
}