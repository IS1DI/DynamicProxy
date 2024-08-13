package com.is1di;

import com.is1di.annotations.Auth;
import com.is1di.annotations.EntityId;
import com.is1di.annotations.Locale;
import com.is1di.annotations.Log;

@Log(start = true, end = true)
public class TestImpl implements Test {
    @Override
    public void test() {
        System.out.println("test method executed");
    }

    @Auth(value = "readSubs", errors = {403, 404, 500})
    @Override
    public void test2(@EntityId Long subsId, @Locale String contentLanguage, @Locale String acceptLanguage) {
        System.out.println("test2 method executed");
    }
}
