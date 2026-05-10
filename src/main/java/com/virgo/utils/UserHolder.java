package com.virgo.utils;

import com.virgo.domain.dto.auth.CurrentUser;

public class UserHolder {
    private static final ThreadLocal<CurrentUser> tl = new ThreadLocal<>();

    public static void saveUser(CurrentUser user) {
        tl.set(user);
    }

    public static CurrentUser getUser() {
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
