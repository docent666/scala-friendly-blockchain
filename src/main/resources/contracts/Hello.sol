pragma solidity ^0.4.18;

contract Hello {


    function Hello() public {
    }

    function () payable public {
    }

    function helloPayMe() external payable {
        msg.sender.transfer(address(this).balance);
    }

}
