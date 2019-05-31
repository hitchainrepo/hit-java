pragma solidity 0.5.6;

/**
 * @author Tyler Chen
 * @title PullRequest
 * @dev The contract to save the pull request informations, last update: 2019-05-28, version: 0.0.1.
 */
contract PullRequest {
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
     * @dev the community commitor pull request.
     */
    mapping(uint256 => string) public communityPullRequest;
    /**
     * @dev the community commitor pull request count.
     */
    uint256 public communityPullRequestCount = uint256(0);
    /**
     * @dev the authored commitor pull request.
     */
    mapping(uint256 => string) public authedPullRequest;
    /**
     * @dev the authored commitor pull request count.
     */
    uint256 public authedPullRequestCount = uint256(0);
    /**
     * @dev the authored addresses mapping for pull request commitors.
     */
    mapping(address => bool) public authedAccount;
    /**
     * @dev the authored addresses mapping for query.
     */
    mapping(uint256 => address) public authedAccountList;
    /**
     * @dev the authored addresses count.
     */
    uint256 public authedAccountCount = uint256(0);
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
     * @dev add pull request.
     * @param _pullRequestAddress the pull request address.
     */
    function addPullRequest(string memory _pullRequestAddress) public {
        bytes memory newPR = bytes(_pullRequestAddress);
        require(newPR.length > 0);
        if(authedAccount[msg.sender] == true || msg.sender == owner){
            authedPullRequestCount = authedPullRequestCount + 1;
            authedPullRequest[authedPullRequestCount] = _pullRequestAddress;
        } else {
            communityPullRequestCount = communityPullRequestCount + 1;
            communityPullRequest[communityPullRequestCount] = _pullRequestAddress;
        }
        emit Success(true);
    }
    /**
     * @dev add the authored pull request account.
     * @param _accountAddress the account address.
     */
    function addAuthedAccount(address _accountAddress) public hasAuthority {
        require(_accountAddress != address(0) && authedAccount[_accountAddress] != true);
        authedAccount[_accountAddress] = true;
        authedAccountCount = authedAccountCount + 1;
        authedAccountList[authedAccountCount] = _accountAddress;
        emit Success(true);
    }

    /**
     * @dev remove the authored pull request account.
     * @param _accountAddress the account address.
     */
    function removeAuthedAccount(address _accountAddress) public hasAuthority {
        require(_accountAddress != address(0) && authedAccount[_accountAddress] == true);
        delete authedAccount[_accountAddress];
        emit Success(true);
    }

    /**avoid mis-transfer*/
    function() external payable{
        revert();
    }
}
