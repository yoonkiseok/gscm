package com.tkg.gscm.utils;

import com.cello.sclis.framework.util.SessionUtil;
import org.springframework.stereotype.Component;

@Component
public class CommonUtil {

    public String getUserId() {
        String sUserId = "";

        try {
            sUserId = SessionUtil.getUserId();
        } catch (Exception e) {
//            throw new CelloException(messageSource.getResource("SessionUtil userId empty error."));
        }

        return sUserId;
    }


}
