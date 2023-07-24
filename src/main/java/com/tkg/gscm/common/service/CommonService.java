package com.tkg.gscm.common.service;

import com.cello.sclis.framework.util.SessionUtil;
import com.tkg.gscm.common.vo.CommonInVo;
import com.tkg.gscm.utils.CommonUtil;
import com.tkg.gscm.utils.InjectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class CommonService {

    @Autowired
    private CodeService codeService;

    @Autowired
    CommonUtil commonUtil;

    public Map<String, Object> setCommonCondition(CommonInVo inVo, Map<String, Object> paramMap) {

        String keyCheck = String.valueOf(inVo.getInParam("keyCheck"));

        if(keyCheck != null && keyCheck.equals("N")){
            paramMap.putAll(inVo.getInParamMap());
        }else {
            paramMap.putAll(InjectUtil.checkInjection(inVo.getInParamMap()));
        }

        Iterator<String> iterator = inVo.getInListIterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            paramMap.put(key, inVo.getInList(key));
        }

        paramMap.put("clntCd", 		codeService.getLssId());
        paramMap.put("userId", 		commonUtil.getUserId());
        paramMap.put("userRole",	codeService.getRoleString());

        try {
            paramMap.put("langCd", 			SessionUtil.getLangCd());
            paramMap.put("dateFormat", 		SessionUtil.getDateFmtCd());
            paramMap.put("dateTimeFormat",	SessionUtil.getDateFmtCd() + " HH24:MI");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return paramMap;
    }


}
