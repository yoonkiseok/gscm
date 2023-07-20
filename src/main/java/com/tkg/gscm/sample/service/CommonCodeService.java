package com.tkg.gscm.sample.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tkg.gscm.common.db.CommonDao;

import jakarta.annotation.PostConstruct;

@Service
public class CommonCodeService {

	
    private final CommonDao commonDao;

    @Autowired
    public CommonCodeService(CommonDao commonDao){
        this.commonDao = commonDao;
    }

	private static List<HashMap<String, Object>> codeGroup = new ArrayList<HashMap<String, Object>>();
	private static List<HashMap<String, Object>> code = new ArrayList<HashMap<String, Object>>();	

	
	
	public List<HashMap<String, Object>> selectCommonGroupCodeList(HashMap<String, Object> paramMap) throws Exception {
//		return commonDao.selectCommonGroupCodeList(paramMap);
        return commonDao.selectList("com.tkg.gscm.sample.dao.CommonCodeDao.selectCommonGroupCodeList");
	}
	
	public List<HashMap<String, Object>> selectCommonCodeList(HashMap<String, Object> paramMap) throws Exception {
//		return commonDao.selectCommonCodeList(paramMap);
        return commonDao.selectList("com.tkg.gscm.sample.dao.CommonCodeDao.selectCommonCodeList");
	}
	
	@PostConstruct
	public void resetCodeList() throws Exception {		
				
		if (codeGroup.isEmpty()) {
			synchronized (codeGroup) {
				if (codeGroup.isEmpty()) {		
					
					List<HashMap<String, Object>> groupCdList;
					// 코드 그룹
//					groupCdList = (ArrayList<HashMap<String, Object>>)commonDao.selectListCodeGroup();	// GROUP_CD,  CD, CD_NM
					groupCdList = commonDao.selectList("com.tkg.gscm.sample.dao.CommonCodeDao.selectListCodeGroup");	// GROUP_CD,  CD, CD_NM
					
					codeGroup.clear();
					codeGroup.addAll(groupCdList);
					
					// 상세코드
//					groupCdList = (ArrayList<HashMap<String, Object>>)commonDao.selectListCode();	// codeId, code, codeNm
					groupCdList = commonDao.selectList("com.tkg.gscm.sample.dao.CommonCodeDao.selectListCode");	// codeId, code, codeNm
					code.clear();
					code.addAll(groupCdList);
					
				}				
			}			
		}	
	}	
	
	
	public void clear() throws Exception { 				
		codeGroup.clear();
		code.clear();
	}
	
	public String getCodeGroupNm(String grCode) throws Exception {
		
		String returnVal = "";
		Iterator<HashMap<String, Object>> iterator = codeGroup.iterator();
		
		while (iterator.hasNext()) {
			
			HashMap<String, Object> codeListMap = (HashMap<String, Object>) iterator.next();
			
			if(grCode.equals((String)codeListMap.get("GROUP_CD"))) {				
				returnVal = (String)codeListMap.get("GROUP_CD_NM");
				break;
			}
			
		}
		
		return returnVal;
	}
	
	public String getCodeNm(String grCode, String sCode) throws Exception { 
		
		String returnVal = "";
		Iterator<HashMap<String, Object>> iterator = code.iterator();
		
		while (iterator.hasNext()) {
			
			HashMap<String, Object> codeListMap = (HashMap<String, Object>) iterator.next();
			
			if(grCode.equals((String)codeListMap.get("GROUP_CD")) && sCode.equals((String)codeListMap.get("SUB_CD")) ) {				
				returnVal = (String)codeListMap.get("SUB_CD_NM");
				break;
			}
			
		}
		
		return returnVal;
	}	
	
	public List<HashMap<String, Object>> getCode(String groupCd) throws Exception {		
		
		List<HashMap<String, Object>> returnVal = new ArrayList<HashMap<String, Object>>();
		
		Iterator<HashMap<String, Object>> iterator = code.iterator();
		
		while (iterator.hasNext()) {
			
			HashMap<String, Object> egovMap = (HashMap<String, Object>) iterator.next();
			
			if(groupCd.equals((String)egovMap.get("GROUP_CD"))) {				
				returnVal.add(egovMap);
			}
			
		}
		
		return returnVal;
	}
	

	public List<HashMap<String, Object>> getCode() throws Exception { 		
		return code;
	}
	
}
