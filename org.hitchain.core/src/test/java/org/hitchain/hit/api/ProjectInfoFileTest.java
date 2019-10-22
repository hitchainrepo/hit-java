package org.hitchain.hit.api;

import org.bouncycastle.util.encoders.Hex;
import org.hitchain.hit.util.ByteHelper;
import org.hitchain.hit.util.RSAHelper;
import org.hitchain.hit.util.WalletHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProjectInfoFileTest {

    public static final String password = "12345678";
    public static final String accountPub = "0xddeeb65353177969ed28b90352ce2335258a387b";
    public static final String accountPri = "ab6fcccdfa79a13523085909a3a0ccce60f181b19ce5930ff573a9406dae1d1486e78590ec44ad9a76623171cdc1ecaafab36bb84e103f29b75b95e0ebc2a134b4c95bfbbf55a8f9";
    public static final String rsaPub = "30819f300d06092a864886f70d010101050003818d0030818902818100b9eb1facdc793348e53fe19a00d2e9391acb65dd53f3861f5ea9e4c3e00d3032b63c3528de9923acd9bfd57ed5a7aa87bff069230af037789512599a33fdc8979986f05c38821e1b4c0230b0b7a0a12c8535ae7215a41f1571ee68bee4777aa921bd47fcf78666c4413a829f6d027f69d31f4ee3b5daf148b3a0bf4d8257980f0203010001";
    public static final String rsaPri = "94a117b8d9ed20b0409054ed6957640d7e714ffb7c070a4adaa455bb653594fe8e7e8357ca51c0fe9e25e28c71c3fe95017c58c5de85387e5120e2d9bdb44e075ddc8c36da403537da67026cf6aebd50b4c0e75606e9ae02beefa3edd47e91a87b3eda1518e3ccb2aa4a06cd8883973193ed132605d4a36a63b1fdd2e0df9bbee9bceaf05b68c62a507db63c7bd882903dc9d2216239d80bc7da79fbeecd56a1b91acf503ae699ebb9d9673006744c7ac1790d29bf187f9cd72ce1e986bc8dded15e74c17ab4ffbd70b765584639cfc0609bd89c63134c6208fdc689cfd5032f8c145d78e774013d4d27f7fe8b52a56724e331682dad6a8fd091b4961471a97389a83816f075db2148bc8fb6b9163f2b2f74c18411cd6e9573b690c84796ae8c93e8d839184ea8046a39791fb4719b1ae7ede9398db2e1925cd08467e03c68aa0746eb311c9b42a8bbd38641efd12432525794de105559b35d08e1bb77fef62ea536155960c87fd3145cd5ac6f3acaa3941c4b824c50737fb77c23a06740a7d9188058d4892c73f90bc130a1a285d7e7ca9216179c78bc4fbde9ad8a22660f5805dcab6c747d2656707630e2da0515b0d5600fc3be1cd8e7b31d838c8ffe59567f00ff336f9b2c48a51aaeb9bdc496037a208b2943c81b4d7f7c879684bed43eb5d2b0b374250c41c7a795712f7686d03947d8ce939baf39cf7d3f2c503ec9dee447d65898711a1fa6705e659f3e2b9dac356fc42a1f29a52559f98e3546a3a9fd7027a22665bb5fd1deee0e05c9fa25e11fed3488ba37d557449219b2158113e7c111277ceff1c106376e643d835b0ed5412edd3cffd9cc7ad569873757c4ceb2541894469ccd38c8518dba2feee3921f43d56ee247af52028680e95d27960e618c691b0ccade40404e38450142fce707091e3bd6b653f352bc095e61a2d8c0d829f846dd56ec6974f81e030d33c3df9c2385537f55713c5595631de3df9a28aeb8677e2905719788d4b1fa614aca3e1a4ba0dec6dcc01f1639f568c2525ac72850eb3787a04ab68b38ba30c3e655a6b565e05f365a1f090a391a018d2eb5fba993e52dca38dcc50cf54ab1957b10d2ec04f85f5158c3f195477a91c3e3fdd90b2a0ad0ce448da816413ae978c9f83b2b1c277fe0105946016956ad281a9a2d4d75400b99a38b1ec47f504b90db3a833f379a59a8cd8e936cb4092191d8d048287de4e1d31d99c2bdbf0fc61a859aff4e6c0108f4811e1ff9fb5aa6d6407d79da4da770d33f56fa2657157633946fd491844d865d475000376a65171354985e7f86715d7d6b7fd4c13bfdfed9796ffde42892e5125523daa162b40e9e88c59daad552334cd438abacd6127eecedf7002978eeec03ed321105e0f23db9b2497e3c351d44bced76ff27c4973dcfefee2a2895c375af11acf9f4f637da29f61719e32659abd01bd7e541fa4f451eaf5a09a77ada0b82c02c431428bbd09b6499d8e3373acc532938c48d054d883c6cf7d56d002a4d6384f03754d75d889e8ae55ce45d18d5d3f3981663bdf72c371feab42de71862cac221715075b1a806ad483908299fa5047be3fcf470e3a365d9174ee23c4947c988a09012ff023473634ee0af9715c1f6b2d6c93bc20a8014d078268c07d55c7030f8f6774daeb5ac0195be26b350e56c11e334941d653d98a64058de3505ef38c83ddfd5bc40d7e28b28bdc3ccef21cd9398a333e083fcf44b2f219e0d1dfdbe18f38fb96b5e796449494c68ed253786aacb76909b1e0cce8748b89cf89ba92fd3c039";
    public static final String repoPub = "04860eb8b1eec244c93036144b626e1b39a83486412bdc321e849d5c2e9add3f3017e3f8b2ae3d86f9bc71458eec79ab39845d6ef97da91748daac41bdfa3a659c";
    public static final String repoPri = "44ae8c188e2de181f5aec36294c9735e32e95db72b3d65cca76ba9fd97fa6718";
    public static final String memberPub = "0xbe39a3f5968aea347505573b750d417ea2c9caf2";
    public static final String memberPri = "23ea228907380569c447472aee5911b79bd09e60b4f81f116b8885829b6f3f98e9bf1943a0e340b20f2aba54a68e59a6337cb17b7b6e2386e76c764db56b2615f05ae7259d1f2424";
    public static final String memberRsaPub = "30819f300d06092a864886f70d010101050003818d0030818902818100d691e61a1e16114aba8c7ff5683ed687d7daeae3f777e3b927d326b0c4a44b8a8a394d5cd914d9e4d33aafc7d3f2d0b1b433d6978f9ccefe4b12c3f531f76506d2b4832034474fd92cf488c5f8a7fdb22081165942ee3b898fc8224eed1bcafbf58a19bd543d21dbcd2ebc35fdabbb4bb61c51d4f14c2306bd9e11caa90017570203010001";
    public static final String memberRsaPri = "569ee2e9530df4765a5aa07207c9e1a1278e9f2f119ad1f8662679a6dd7f8cd20485720a07f72dab8d97a1244380040a3e3591c2ae862f5b726a0324c38dbaacea743be28f603280f7c10c51e2bc60eab103647507b465bfe297ce750b067e72e58370993f76af19c0eaa81a14e3b29d28ca2f69f8cd1bb8b3f75219fabbe18d02d84f929e6c7f6bfa1bbc3d1fd4d8ec53e8831c78d8f83ec0edc7595db3170234be3c535b87a0ae66523f1644141d1fcdb2322a4a3478ca116b2d946b6a85d0212d7cadbfb25d2c22309b5fca5586b36e69123dfc3af8b96b898723aa3ab08614a91f1477cd8678e8e61bc8fe36c80fb12e4133302fd2af6a07256422d441bd7e6788bea4f907d1a51fd3d005b2a6ef34bbc268166ef24b329c6016eff0882ac5b2ebfff7d3c3d7bf036b4e51576b0b0e8b79cd69894186878af2d547d65e8fbdfdb31266b1ca8a6e4d24f6c5d4afaf18480b8cf90befa68350183e4e23103b5bb4d976701611cd6da153548d8ad310cbb695696f7fd2514d312c62c2671dd76f97d0e54355d25e8b3efbe09ebea21ecef48c32ff2ab94490c8b0c25919c8bc49e4a4b6ac5d5fc5f9ef842d338d67063a07bf549e28ff3dd74bfa3f2d1bf5a405326bad2e4d9e3f08a97b0df0b9979eee9faf4659ca21cab898cf0d7a20276250118748b045d22d7adac15b8f237eb45d13c9b2b54d94e05d24d1a1b20852b2cf4c5051c55890700b3297765d712dbc9db562e5d873d33d7eb49ed756d1f24cba186d010c33d023c71e8cfb41e766dd3275f0c2f9d3ed526d6cf9b3f544cf17f8157062de085ff5cc0f8d2977fdbb57d19ec0a6833950a0eb71817c73daf8eade870fb20c568fb4ab17d0e87ee06e6417940c9d57eb8f7c89e67ce4906bf4df4f72b5af09b928ee4d43ed53e033f82e65723f885e8047f0d202a57eb811d9b8437bb64977e6cb4a8a3fc9522bcda08198b99e690f1d9b466201ba029ad6b721f6837299ac8428b54d04af2c8aa35c030feeb97997ff780563a3abef96dd887b303281e06cf10e47e3c8910d016113b2cba1d75a876977470eb590f849cc4579e9fc7483b202129aab024fe0e51f8c11ca105b6d45e4bee2817bf10e95642e13ad5fdce7c34dae66232a23d741bcadac3d862ab7fc57c530696e700514866f32249e4ad49fe3bf9cf58d08b60ca8be703f3959b77903d9dcc7fa4e10d35ce9b2e3a7b218543bb252f82f8327ec660afd47bfbf14bb4d71870171011d15c7b2d6907986e51856782a37a4bd8b78299e21ed3ebc1ebb97ecdf9978ac4e209e04c515ac78cc8ffbf95758e05312d7d7ef08c32ba6d72e23262817401102eea625c2adba6aebbe80bc54751d7a325fa5ffa3f503a01074677e01d12c7b85d2563afc37046c98629e0338d1e29c29df77b9e408e3726e5ac0372ca3a2e048bf8a4b6d68c57d698da32283690ae5e780e2eb967a9f4c993e36bd867c0401fca4379622ecab6140dff4c2de7cd1811c17d35d8fe52d4e74842b6be801077718190aa5a5d2d73bd592c19ccd8f913ec1fe53f0644be534d6c91085756d16703d7ec2299d9446807ddc092dc652f08511df3e5a62f69bd8d6eccfb7b4b44c4418713ece2fe13cbd23d087b8768164c09dd65b78400be5e14f768be41f424e50261ef1404fd1b4cc52e89fda0877e1931113039776a58c84a42f2427641d55cda4882002a3356b178025a0acb15d7765cf8c2cc2efc3719a67389d8b923c461e6c642044a1bb2901bfc7ae12e16595f27b2c94e6336bbfd1f479468a52";

    public static final String PIF_CONTENT = "" +
            "7c8c7de9395403a42b1dd656d1624b637031fa37a7f72ddae98a7aadbed73728554858a19a6b993528e678d048d0745ba898d3af733d9365f067b49ccc77128919bf63134740c57ac40d62b288d6b1c902adc075853057a44994b12527a2812a26f41669d2acd4cca945ea30289ae6ac38a8a7921d2d6729663a14b29857d5c3\n" +
            "{\n" +
            "  \"version\": \"1\",\n" +
            "  \"ethereumUrl\": \"https://121.40.127.45:1443\",\n" +
            "  \"fileServerUrl\": \"121.40.127.45\",\n" +
            "  \"repoName\": \"spring-boot-seckill-20190829161143\",\n" +
            "  \"repoAddress\": \"0x48e154cb7040602163236df58a8cc3c0836425e1-6\",\n" +
            "  \"owner\": \"0xddeeb65353177969ed28b90352ce2335258a387b\",\n" +
            "  \"ownerPubKeyRsa\": \"30819f300d06092a864886f70d010101050003818d0030818902818100b9eb1facdc793348e53fe19a00d2e9391acb65dd53f3861f5ea9e4c3e00d3032b63c3528de9923acd9bfd57ed5a7aa87bff069230af037789512599a33fdc8979986f05c38821e1b4c0230b0b7a0a12c8535ae7215a41f1571ee68bee4777aa921bd47fcf78666c4413a829f6d027f69d31f4ee3b5daf148b3a0bf4d8257980f0203010001\",\n" +
            "  \"ownerAddressEcc\": \"0xddeeb65353177969ed28b90352ce2335258a387b\",\n" +
            "  \"members\": []\n" +
            "}";

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void createProjectInfoFile() throws Exception {
        ProjectInfoFile info = new ProjectInfoFile();
        {
            info.setVersion("1");
            info.setFileServerUrl("121.40.127.45");
            String accountPubKey = accountPub;
            String address = "0x48e154cb7040602163236df58a8cc3c0836425e1-6";
            info.setRepoName("spring-boot-seckill-20190829161143");
            info.setRepoAddress(address);
            info.setOwner(accountPubKey);
            info.setOwnerPubKeyRsa(rsaPub);
            info.setOwnerAddressEcc(accountPubKey);
        }
        //
        Assert.assertEquals(info.genSignedContent(WalletHelper.decryptWithPasswordHex(rsaPri, password)), PIF_CONTENT);
    }

    @Test
    public void fromFile() throws Exception {
        ProjectInfoFile pif = ProjectInfoFile.fromFile(new HashedFile.FileWrapper("projectinfo", new HashedFile.ByteArrayInputStreamCallback(ByteHelper.utf8(PIF_CONTENT))));
        Assert.assertEquals("1", pif.getVersion());
        Assert.assertEquals("121.40.127.45", pif.getFileServerUrl());
        Assert.assertEquals("0x48e154cb7040602163236df58a8cc3c0836425e1-6", pif.getRepoAddress());
        Assert.assertEquals("spring-boot-seckill-20190829161143", pif.getRepoName());
        Assert.assertEquals(rsaPub, pif.getOwnerPubKeyRsa());
        Assert.assertEquals(accountPub, pif.getOwnerAddressEcc());
        //
        Assert.assertTrue(pif.verify(pif));
        //
        Assert.assertFalse(pif.isPrivate());
    }

    @Test
    public void isPrivate() throws Exception {
        ProjectInfoFile pif = ProjectInfoFile.fromFile(new HashedFile.FileWrapper("projectinfo", new HashedFile.ByteArrayInputStreamCallback(ByteHelper.utf8(PIF_CONTENT))));
        pif.addMemberPublic(memberPub, memberRsaPub, memberPub);
        // Encrypt: private key -(hex decode)-> private key bytes -(encrypt with rsa public key)->  encrypt bytes -(hex encode)-> hex encrypt
        // Decrypt: hex encrypt -(hex decode)-> encrypt bytes     -(decrypt with rsa private key)-> private key bytes -(hex encode) private key
        String privateKeyEncryptByOwnerRsaPubKey = Hex.toHexString(
                RSAHelper.encrypt(
                        Hex.decode(repoPri),
                        RSAHelper.getPublicKeyFromHex(rsaPub)
                ));
        pif.setRepoPubKey(repoPub);
        pif.setRepoPriKey(privateKeyEncryptByOwnerRsaPubKey);
        for (ProjectInfoFile.TeamInfo ti : pif.getMembers()) {
            String memberPubKey = ti.getMemberPubKeyRsa();
            String privateKeyEncryptByMemberRsaPubKey = Hex.toHexString(
                    RSAHelper.encrypt(
                            Hex.decode(repoPri),
                            RSAHelper.getPublicKeyFromHex(memberPubKey)
                    ));
            ti.setMemberRepoPriKey(privateKeyEncryptByMemberRsaPubKey);
        }
        pif.genSignedContent(WalletHelper.decryptWithPasswordHex(rsaPri, password));
    }
}