package com.tkg.gscm.utils;

import com.cello.sclis.prp.util.StringUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class InjectUtil {

    private static final String[] injectPattern = { "\'", "--", "\\", "\r", "\n", "%" };

    public static Map<String, Object> checkInjection(Map<String, String> inParam)
    {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : inParam.keySet())
        {
            Object obj = inParam.get(key);

            if (obj instanceof String) {
                String sVal = inParam.get(key);
                if ( !StringUtil.isEmpty(sVal) ) {
                    map.put(key,getNewInParamVal(sVal));
                } else {
                    map.put(key,inParam.get(key));
                }
            } else {
                map.put(key,inParam.get(key));
            }
        }

        return map;
    }

    private static String getNewInParamVal(String inStr)
    {
        String outStr = inStr; //inStr.replace("CHR(1)", "''");
        String[] inWd = outStr.split("\'");

        boolean bNoMulti = true;
        boolean bComma   = false;
        if( inWd.length >= 2 && StringUtils.countMatches(outStr,"\'")%2 == 0 ) { //StringUtil.isEmpty(inWd[0])
            bNoMulti = false;
            for ( int i = 1 ; i < inWd.length ; i++ ) {
                if ( !bComma && inWd[i].trim().equals(",")) {
                    bComma   = true;
                } else if ( bComma && !inWd[i].trim().equals(",")) {
                    bComma   = false;
                } else if ( (!bComma && !inWd[i].trim().equals(","))
                        ||( bComma &&  inWd[i].trim().equals(",")) ) {
                    bNoMulti = true;
                    break;
                }
            }
        }

        if ( bNoMulti ) {
            for (String str : injectPattern) {
                if (outStr.contains(str))
                {
                    outStr = outStr.replace(str, "~");
                }
            }
        } else {
            outStr = "";
            StringBuffer buf = new StringBuffer();
            for ( int i = 0 ; i < inWd.length ; i++ ) {
                if ( !inWd[i].equals(",") || i < inWd.length - 1 ) {
                    buf.append("\'" + inWd[i]);
                }
            }
            outStr = buf.toString() + "\'";
        }

        return outStr;
    }


}
