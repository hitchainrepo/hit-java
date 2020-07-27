package org.hitchain.ipfs.cbor;

public interface Cborable {

    CborObject toCbor();

    default byte[] serialize() {
        return toCbor().toByteArray();
    }
}
