pragma solidity 0.5.6;

/**
 * @author Tyler Chen
 * @title RepositoryName
 * @dev The contract to save the repository informations, last update: 2019-01-01, version: 0.0.1.
 */
contract RepositoryName {
    /**
     * @dev the event for use to find out the transaction is success or fail.
     */
    event Success(bool value);
    /**
     * @dev the contract/repository owner.
     */
    address public owner;
    /**
     * @dev the delegator can do many transaction for the contract/repository.
     */
    address public delegator;
    /**
     * @dev the repository name.
     */
    string public repositoryName;
    /**
     * @dev the team member addresses mapping.
     */
    mapping(address => bool) public authedAccounts;
    /**
     * @dev the team member addresses list.
     */
    address[] public authedAccountList;
    /**
     * @dev the team member size.
     */
    uint256 public authedAccountSize = uint256(0);
    /**
     * @dev the repository address.
     */
    string public repositoryAddress;

    /**
     * @dev create the contract with the contract owner.
     * @param _owner The address to be the contract owner.
     */
    constructor(address _owner) public {
        owner = _owner == address(0) ? msg.sender : _owner;
        emit Success(true);
    }

    /**
     * @dev Throws if called by any account other than the owner or the delegator.
     */
    modifier hasAuthority() {
        require(msg.sender == owner || msg.sender == delegator);
        _;
    }

    /**
     * @dev Throws if called by any account other than the owner.
     */
    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }

    /**
     * @dev init the contract with the new owner and repository name.
     * @param _newOwner The address to be the contract owner.
     * @param _repositoryName The repository name.
     */
    function init(address _newOwner, string memory _repositoryName) public onlyOwner {
        require(_newOwner != address(0));
        owner = _newOwner;
        repositoryName = _repositoryName;
        emit Success(true);
    }

    /**
     * @dev init the contract with the new owner and repository name and setting the delegator.
     * @param _newOwner The address to be the contract owner.
     * @param _repositoryName The repository name.
     * @param _delegator The delegator address.
     */
    function initWithDelegator(address _newOwner, string memory _repositoryName, address _delegator) public onlyOwner {
        require(_newOwner != address(0) && _delegator != address(0));
        owner = _newOwner;
        delegator = _delegator;
        repositoryName = _repositoryName;
        emit Success(true);
    }

    /**
     * @dev update repository name.
     * @param _repositoryName The new repository name.
     */
    function updateRepositoryName(string memory _repositoryName) public hasAuthority {
        repositoryName = _repositoryName;
        emit Success(true);
    }

    /**
     * @dev update repository address.
     * @param _oldRepositoryAddress The old repository address to verify, if the repository address length < 2 will considered to empty just only call for the first time.
     * @param _newRepositoryAddress The new repository address.
     */
    function updateRepositoryAddress(string memory _oldRepositoryAddress, string memory _newRepositoryAddress) public {
        require(msg.sender == owner || msg.sender == delegator || authedAccounts[msg.sender] == true);
        bytes memory nra = bytes(_newRepositoryAddress);
        require(nra.length > 1);
        bytes memory ora = bytes(_oldRepositoryAddress);
        bytes memory ra = bytes(repositoryAddress);
        // if length < 2 considered to null.
        require(ora.length < 2 && ra.length < 2 || keccak256(ora) == keccak256(ra));
        repositoryAddress = _newRepositoryAddress;
        emit Success(true);
    }

    /**
     * @dev add the repository team member, those members can only update the updateRepositoryAddress.
     * @param _member the member address.
     */
    function addTeamMember(address _member) public hasAuthority {
        require(_member != address(0) && authedAccounts[_member] != true);
        authedAccounts[_member] = true;
        authedAccountList.push(_member);
        authedAccountSize = authedAccountSize + 1;
        emit Success(true);
    }

    /**
     * @dev remove the repository team member.
     * @param _member the member address.
     */
    function removeTeamMember(address _member) public hasAuthority {
        require(_member != address(0) && authedAccounts[_member] == true);
        delete authedAccounts[_member];
        for (uint i=0; i<authedAccountList.length; i++) {
            if(authedAccountList[i] == _member){
                delete authedAccountList[i];
                break;
            }
        }
        authedAccountSize = authedAccountSize - 1;
        emit Success(true);
    }

    /**
     * @dev change the repository owner.
     * @param _newOwner the new owner for the contract.
     */
    function changeOwner(address _newOwner) public onlyOwner {
        require(msg.sender == owner && _newOwner != address(0));
        owner = _newOwner;
        emit Success(true);
    }

    /**
     * @dev setting the delegator for the repository.
     * @param _delegator the delegator address.
     */
    function delegateTo(address _delegator) public hasAuthority {
        delegator = _delegator;
        emit Success(true);
    }

    /**
     * @dev check the address if is the team member.
     * @param _member the address to check in team member.
     */
    function hasTeamMember(address _member) public view returns (bool) {
        return authedAccounts[_member];
    }

    /**
     * @dev get the team member by index.
     * @param _index the index from 0.
     */
    function teamMemberAtIndex(uint256 _index) public view returns (address) {
        return authedAccountList[_index];
    }
}