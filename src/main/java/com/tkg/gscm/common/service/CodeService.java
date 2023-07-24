package com.tkg.gscm.common.service;

import com.cello.sclis.framework.util.SessionUtil;
import com.cello.sclis.prp.common.vo.CommonInVo;
import com.cello.sclis.prp.common.vo.CommonOutVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
@Service("codeService")
public class CodeService {

    public String getRoleString() {
        CommonInVo inVo = new CommonInVo();
        CommonOutVo outVo = this.getRoleList(inVo);
        List<?> roleList = outVo.getOutList("dsRole");
        List<String> outList = new ArrayList();

        for(int i = 0; i < roleList.size(); ++i) {
            Map<String, Object> rowMap = new HashMap();
            rowMap.putAll((Map)roleList.get(i));
            outList.add(rowMap.get("CD").toString());
        }

        if (outList.isEmpty()) {
            outList.add("");
        }

        StringBuffer roleString = new StringBuffer();
        Iterator<String> iter = outList.iterator();
        Boolean isFirst = true;

        while(iter.hasNext()) {
            if (isFirst) {
                isFirst = false;
            } else {
                roleString.append(",");
            }

            roleString.append("'");
            roleString.append((String)iter.next());
            roleString.append("'");
        }

        return roleString.toString();
    }

    public String getLssId() {
        String outString = "";

        try {
            outString = SessionUtil.getLssId();
        } catch (Exception var3) {
            outString = "LSS_SYS";
//            this.logger.error("CodeService SessionUtil occurred!! : getLssId ", var3);
        }

        return outString;
    }


    public CommonOutVo getRoleList(CommonInVo inVo) {
        CommonOutVo outVo = new CommonOutVo();
        List<Map<String, Object>> outList = new ArrayList();

        try {
            List<?> roleCdList = SessionUtil.getRoleCdList();

            for(int i = 0; i < roleCdList.size(); ++i) {
                Map<String, String> map = (Map)roleCdList.get(i);
                Map<String, Object> outMap = new HashMap();
                Set<Map.Entry<String, String>> entries = map.entrySet();
                Iterator i$ = entries.iterator();

                while(i$.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)i$.next();
                    if (StringUtils.equals("CD", (String)entry.getKey())) {
                        outMap.put("CD", entry.getValue());
                    } else if (StringUtils.equals("NM", (String)entry.getKey())) {
                        outMap.put("NM", entry.getValue());
                    } else if (StringUtils.equals("ALIAS", (String)entry.getKey())) {
                        outMap.put("ALIAS", entry.getValue());
                    }
                }

                outList.add(outMap);
            }
        } catch (Exception var11) {
//            this.logger.error("CodeService exception occurred!! : getRoleList ", var11);
        }

        outVo.putOutList("dsRole", outList);
        return outVo;
    }
}
